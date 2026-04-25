/**
 * 知识图谱与诗人行迹 ECharts 展示
 */
(function () {
  const API_BASE = '';

  let knowledgeGraphChart = null;
  let trajectoryBarChart = null;
  let trajectoryPieChart = null;
  let poetTagChart = null;

  const colorMap = {
    诗人: '#8b4513',
    朝代: '#4a7c59',
    诗词: '#c9a227',
    题材: '#8b2500'
  };

  function getGraphChart() {
    const el = document.getElementById('knowledge-graph-chart');
    if (!el) return null;
    if (knowledgeGraphChart) return knowledgeGraphChart;
    knowledgeGraphChart = echarts.init(el);
    return knowledgeGraphChart;
  }

  function getBarChart() {
    const el = document.getElementById('trajectory-bar-chart');
    if (!el) return null;
    if (trajectoryBarChart) return trajectoryBarChart;
    trajectoryBarChart = echarts.init(el);
    return trajectoryBarChart;
  }

  function getPieChart() {
    const el = document.getElementById('trajectory-pie-chart');
    if (!el) return null;
    if (trajectoryPieChart) return trajectoryPieChart;
    trajectoryPieChart = echarts.init(el);
    return trajectoryPieChart;
  }

  function getPoetTagChart() {
    const el = document.getElementById('poet-tag-chart');
    if (!el) return null;
    if (poetTagChart) return poetTagChart;
    poetTagChart = echarts.init(el);
    return poetTagChart;
  }

  /** 加载并渲染知识图谱 */
  function renderKnowledgeGraph() {
    fetch(API_BASE + '/api/graph/knowledge?limit=180')
      .then(function (res) { return res.json(); })
      .then(function (json) {
        if (!json.success || !json.data) return;
        const data = json.data;
        const chart = getGraphChart();
        if (!chart) return;

        const categories = (data.categories || []).map(function (c, i) {
          return {
            name: c.name,
            itemStyle: { color: colorMap[c.name] || '#666' }
          };
        });

        const option = {
          title: { text: '', left: 'center' },
          tooltip: {
            formatter: function (params) {
              if (params.dataType === 'edge') {
                return params.data.source + ' → ' + params.data.target;
              }
              return params.data.name + (params.data.value ? ' (' + params.data.value + ')' : '');
            }
          },
          legend: {
            data: categories.map(function (c) { return c.name; }),
            bottom: 10,
            textStyle: { fontFamily: 'Noto Serif SC, serif' }
          },
          series: [{
            type: 'graph',
            layout: 'force',
            data: (data.nodes || []).map(function (n) {
              return {
                name: n.name,
                category: n.category,
                value: n.value,
                symbolSize: n.symbolSize || 14,
                label: { show: true, position: 'right', formatter: '{b}' },
                itemStyle: { color: colorMap[categories[n.category].name] || '#666' }
              };
            }),
            links: (data.links || []).map(function (l) {
              return { source: l.source, target: l.target };
            }),
            categories: categories,
            roam: true,
            label: { show: true, position: 'right', fontSize: 11 },
            emphasis: { focus: 'adjacency', lineStyle: { width: 3 } },
            force: {
              repulsion: 200,
              edgeLength: [60, 120],
              layoutAnimation: true
            },
            lineStyle: { color: 'source', curveness: 0.2, opacity: 0.6 }
          }]
        };
        chart.setOption(option, true);
      })
      .catch(function () {
        var el = document.getElementById('knowledge-graph-chart');
        if (el) el.innerHTML = '<p class="empty" style="padding:2rem;text-align:center;color:#888">图谱加载失败</p>';
      });
  }

  /** 诗人行迹：柱状图 + 饼图 + 诗人题材分布 */
  function renderTrajectory(author) {
    var url = API_BASE + '/api/poet/trajectory';
    if (author) url += '?author=' + encodeURIComponent(author);

    fetch(url)
      .then(function (res) { return res.json(); })
      .then(function (json) {
        if (!json.success || !json.data) return;
        var data = json.data;

        var barChart = getBarChart();
        if (barChart && data.authorRank && data.authorRank.length) {
          barChart.setOption({
            title: { text: '诗人作品数量 Top20', left: 'center', textStyle: { fontSize: 14 } },
            tooltip: { trigger: 'axis' },
            grid: { left: '12%', right: '8%', top: '15%', bottom: '15%' },
            xAxis: {
              type: 'category',
              data: data.authorRank.map(function (x) { return x.name; }),
              axisLabel: { rotate: 45, fontSize: 11 }
            },
            yAxis: { type: 'value', name: '作品数' },
            series: [{
              type: 'bar',
              data: data.authorRank.map(function (x) { return x.value; }),
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#c9a227' },
                  { offset: 1, color: '#8b4513' }
                ])
              }
            }]
          }, true);
        }

        var pieChart = getPieChart();
        if (pieChart && data.dynastyDist && data.dynastyDist.length) {
          pieChart.setOption({
            title: { text: '各朝代诗词分布', left: 'center', textStyle: { fontSize: 14 } },
            tooltip: { trigger: 'item' },
            legend: { orient: 'vertical', right: 10, top: 'center' },
            series: [{
              type: 'pie',
              radius: ['40%', '65%'],
              center: ['40%', '50%'],
              data: data.dynastyDist,
              itemStyle: {
                borderColor: '#faf6f0',
                borderWidth: 2
              },
              label: { fontSize: 11 }
            }]
          }, true);
        }

        var tagChart = getPoetTagChart();
        if (data.poetTagDist && data.poetTagDist.tagDist && data.poetTagDist.tagDist.length) {
          if (!tagChart) tagChart = getPoetTagChart();
          tagChart.setOption({
            title: {
              text: data.poetTagDist.author + ' · 创作题材分布（' + data.poetTagDist.total + ' 首）',
              left: 'center',
              textStyle: { fontSize: 14 }
            },
            tooltip: { trigger: 'axis' },
            grid: { left: '12%', right: '8%', top: '18%', bottom: '12%' },
            xAxis: {
              type: 'category',
              data: data.poetTagDist.tagDist.map(function (x) { return x.name; })
            },
            yAxis: { type: 'value', name: '数量' },
            series: [{
              type: 'bar',
              data: data.poetTagDist.tagDist.map(function (x) { return x.value; }),
              itemStyle: { color: '#4a7c59' }
            }]
          }, true);
          document.getElementById('poet-tag-chart').style.display = 'block';
        } else {
          var poetTagEl = document.getElementById('poet-tag-chart');
          if (poetTagEl) {
            poetTagEl.style.display = author ? 'block' : 'none';
            if (tagChart) {
              tagChart.clear();
              if (author) {
                tagChart.setOption({
                  title: { text: author + ' · 暂无题材分布数据', left: 'center', top: 'middle', textStyle: { color: '#888', fontSize: 14 } }
                }, true);
              }
            }
          }
        }
      })
      .catch(function () {
        var el = document.getElementById('trajectory-bar-chart');
        if (el) el.innerHTML = '<p class="empty" style="padding:2rem;text-align:center;color:#888">数据加载失败</p>';
      });
  }

  /** 填充诗人下拉并绑定切换 */
  function initTrajectoryPoetSelect() {
    fetch(API_BASE + '/api/poet/trajectory')
      .then(function (res) { return res.json(); })
      .then(function (json) {
        if (!json.success || !json.data || !json.data.authorRank) return;
        var select = document.getElementById('trajectory-poet');
        if (!select) return;
        var first = select.innerHTML;
        select.innerHTML = first;
        json.data.authorRank.forEach(function (item) {
          var opt = document.createElement('option');
          opt.value = item.name;
          opt.textContent = item.name + ' (' + item.value + '首)';
          select.appendChild(opt);
        });
        select.addEventListener('change', function () {
          renderTrajectory(select.value);
        });
      });
  }

  /** 切换到对应 section 时初始化/重绘 */
  function onSectionShow(sectionId) {
    if (sectionId === 'graph') {
      setTimeout(function () {
        if (knowledgeGraphChart) knowledgeGraphChart.resize();
        renderKnowledgeGraph();
      }, 100);
    } else if (sectionId === 'trajectory') {
      setTimeout(function () {
        if (trajectoryBarChart) trajectoryBarChart.resize();
        if (trajectoryPieChart) trajectoryPieChart.resize();
        if (poetTagChart) poetTagChart.resize();
        var sel = document.getElementById('trajectory-poet');
        renderTrajectory(sel ? sel.value : '');
        initTrajectoryPoetSelect();
      }, 100);
    }
  }

  document.getElementById('graph-refresh') && document.getElementById('graph-refresh').addEventListener('click', function () {
    renderKnowledgeGraph();
  });

  document.querySelectorAll('.nav-link').forEach(function (link) {
    link.addEventListener('click', function () {
      setTimeout(function () {
        onSectionShow(link.getAttribute('data-section'));
      }, 200);
    });
  });

  window.addEventListener('resize', function () {
    [knowledgeGraphChart, trajectoryBarChart, trajectoryPieChart, poetTagChart].forEach(function (ch) {
      if (ch) ch.resize();
    });
  });

  if (document.getElementById('trajectory') && document.getElementById('trajectory').classList.contains('active')) {
    initTrajectoryPoetSelect();
    renderTrajectory();
  }
  if (document.getElementById('graph') && document.getElementById('graph').classList.contains('active')) {
    renderKnowledgeGraph();
  }
})();
