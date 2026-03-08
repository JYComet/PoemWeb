/**
 * 诗人行迹地图 - 地点变动、时间、该地创作的诗词
 */
(function () {
  const API_BASE = '';
  const CHINA_MAP_URLS = [
    'https://echarts.apache.org/examples/data/asset/geo/china.json',
    'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json'
  ];

  let trajectoryMapChart = null;
  let chinaMapRegistered = false;

  function getMapChart() {
    var el = document.getElementById('poet-trajectory-map');
    if (!el) return null;
    if (trajectoryMapChart) return trajectoryMapChart;
    trajectoryMapChart = echarts.init(el);
    return trajectoryMapChart;
  }

  function registerChinaMap() {
    if (chinaMapRegistered) return Promise.resolve();
    function tryLoad(i) {
      if (i >= CHINA_MAP_URLS.length) return Promise.reject(new Error('map load failed'));
      return fetch(CHINA_MAP_URLS[i])
        .then(function (res) { return res.json(); })
        .then(function (geoJson) {
          echarts.registerMap('china', geoJson);
          chinaMapRegistered = true;
        })
        .catch(function () { return tryLoad(i + 1); });
    }
    return tryLoad(0);
  }

  function renderTrajectoryMap(author) {
    var mapEl = document.getElementById('poet-trajectory-map');
    var detailPanel = document.getElementById('trajectory-detail-panel');
    if (!mapEl || !author) {
      if (detailPanel) detailPanel.classList.remove('active');
      return;
    }

    fetch(API_BASE + '/api/poet/trajectory/map?author=' + encodeURIComponent(author))
      .then(function (res) { return res.json(); })
      .then(function (json) {
        if (!json.success || !json.data || !json.data.trajectory) {
          if (detailPanel) {
            detailPanel.innerHTML = '<p class="empty">' + (json.data && json.data.message ? json.data.message : '暂无数据') + '</p>';
            detailPanel.classList.add('active');
          }
          return;
        }

        var data = json.data;
        var points = data.trajectory;
        var lines = data.lines || [];

        registerChinaMap()
          .then(function () {
            var chart = getMapChart();
            if (!chart) return;

            var scatterData = points.map(function (p, i) {
            return {
              name: p.place,
              value: [p.lng, p.lat, p.place],
              year: p.year,
              desc: p.desc,
              poems: p.poemsDetail || [],
              place: p.place,
              symbolSize: 18,
              itemStyle: {
                color: i === 0 ? '#8b2500' : (i === points.length - 1 ? '#4a7c59' : '#c9a227'),
                borderColor: '#fff',
                borderWidth: 2
              }
            };
          });

          var lineData = lines.map(function (l) {
            return {
              coords: [l.from, l.to],
              lineStyle: { color: '#8b4513', width: 2, type: 'dashed' }
            };
          });

          var option = {
            title: {
              text: author + ' · 行迹图',
              left: 'center',
              textStyle: { fontSize: 16 }
            },
            tooltip: {
              trigger: 'item',
              formatter: function (params) {
                if (params.data && params.data.year !== undefined) {
                  var d = params.data;
                  var html = '<strong>' + d.place + '</strong> (' + d.year + '年)<br>' + (d.desc || '') + '<br>';
                  if (d.poems && d.poems.length) {
                    html += '<br>该地创作：<br>' + d.poems.map(function (p) { return '· ' + p.title; }).join('<br>');
                  }
                  return html;
                }
                return params.name;
              }
            },
            geo: {
              map: 'china',
              roam: true,
              zoom: 1.2,
              label: { show: false },
              itemStyle: {
                areaColor: '#f0e8dc',
                borderColor: '#8b4513',
                borderWidth: 1
              },
              emphasis: {
                itemStyle: { areaColor: '#e8dcc8' }
              }
            },
            series: [
              {
                name: '路线',
                type: 'lines',
                coordinateSystem: 'geo',
                data: lineData,
                lineStyle: { color: '#8b4513', width: 2, type: 'dashed' },
                effect: { show: false }
              },
              {
                name: '地点',
                type: 'scatter',
                coordinateSystem: 'geo',
                data: scatterData,
                symbol: 'pin',
                symbolSize: 28,
                label: {
                  show: true,
                  formatter: '{b}',
                  position: 'bottom',
                  fontSize: 11
                },
                emphasis: {
                  scale: 1.2,
                  label: { show: true }
                }
              }
            ]
          };

            chart.setOption(option, true);

            chart.off('click');
          chart.on('click', function (params) {
            if (params.componentType === 'series' && params.seriesName === '地点' && params.data) {
              var d = params.data;
              if (!detailPanel) return;
              var html = '<div class="place-title">' + d.place + '</div>';
              html += '<div class="place-meta">' + d.year + '年 · ' + (d.desc || '') + '</div>';
              if (d.poems && d.poems.length) {
                html += '<div class="poem-list">';
                d.poems.forEach(function (p) {
                  html += '<div class="poem-item" data-title="' + (p.title || '').replace(/"/g, '&quot;') + '">';
                  html += '<div class="poem-title">《' + (p.title || '') + '》</div>';
                  if (p.content) html += '<div class="poem-preview">' + (p.content || '').slice(0, 80) + '...</div>';
                  html += '</div>';
                });
                html += '</div>';
              } else {
                html += '<p class="empty">该地点暂无诗词记录</p>';
              }
              detailPanel.innerHTML = html;
              detailPanel.classList.add('active');
              detailPanel.querySelectorAll('.poem-item').forEach(function (el) {
                el.addEventListener('click', function () {
                  var title = el.getAttribute('data-title');
                  if (title && typeof window.showPoemDetail === 'function') {
                    window.showPoemDetail(title);
                  } else if (title) {
                    window.location.hash = 'search';
                    var kw = document.getElementById('search-keyword');
                    if (kw) kw.value = title;
                  }
                });
              });
            }
          });
          })
          .catch(function () {
            if (detailPanel) {
              detailPanel.innerHTML = '<p class="empty">中国地图加载失败，请检查网络后重试</p>';
              detailPanel.classList.add('active');
            }
          });
      })
      .catch(function () {
        if (detailPanel) {
          detailPanel.innerHTML = '<p class="empty">地图数据加载失败</p>';
          detailPanel.classList.add('active');
        }
      });
  }

  var mapPoetSelectInited = false;
  function initMapPoetSelect() {
    fetch(API_BASE + '/api/poet/trajectory/map')
      .then(function (res) { return res.json(); })
      .then(function (json) {
        if (!json.success || !json.data || !json.data.poets) return;
        var select = document.getElementById('trajectory-map-poet');
        if (!select) return;
        select.innerHTML = '<option value="">-- 请选择诗人 --</option>';
        json.data.poets.forEach(function (name) {
          var opt = document.createElement('option');
          opt.value = name;
          opt.textContent = name;
          select.appendChild(opt);
        });
        if (!mapPoetSelectInited) {
          mapPoetSelectInited = true;
          select.addEventListener('change', function () {
          var author = select.value;
          if (author) renderTrajectoryMap(author);
          else {
            var dp = document.getElementById('trajectory-detail-panel');
            if (dp) { dp.classList.remove('active'); dp.innerHTML = ''; }
            if (trajectoryMapChart) trajectoryMapChart.clear();
          }
        });
        }
      });
  }

  function showMapPanel() {
    document.getElementById('trajectory-stats').style.display = 'none';
    document.getElementById('trajectory-map').style.display = 'block';
    initMapPoetSelect();
    setTimeout(function () {
      if (trajectoryMapChart) trajectoryMapChart.resize();
      var author = document.getElementById('trajectory-map-poet') && document.getElementById('trajectory-map-poet').value;
      if (author) renderTrajectoryMap(author);
    }, 100);
  }

  function showStatsPanel() {
    document.getElementById('trajectory-stats').style.display = 'block';
    document.getElementById('trajectory-map').style.display = 'none';
  }

  document.querySelectorAll('[data-trajectory-tab]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      document.querySelectorAll('[data-trajectory-tab]').forEach(function (b) { b.classList.remove('active'); });
      this.classList.add('active');
      if (btn.getAttribute('data-trajectory-tab') === 'map') {
        showMapPanel();
      } else {
        showStatsPanel();
      }
    });
  });

  window.addEventListener('resize', function () {
    if (trajectoryMapChart) trajectoryMapChart.resize();
  });

  window.poetMapRender = renderTrajectoryMap;
})();
