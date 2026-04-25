
// ============================================
// 知识图谱页面相关功能
// ============================================

// 知识图谱全局变量
let knowledgeGraphChart = null;
let currentGraphType = ''; // 当前查看的类型
let currentGraphNode = ''; // 当前查看的节点

// 知识图谱筛选功能
document.addEventListener('DOMContentLoaded', function() {
  const categorySelect = document.getElementById('graph-category');
  const nodeSelect = document.getElementById('graph-node');
  const viewBtn = document.getElementById('graph-view-btn');
  const refreshBtn = document.getElementById('graph-refresh');

  // 当选择分类时，加载对应的节点列表或显示输入框
  if (categorySelect) {
    categorySelect.addEventListener('change', function() {
      const category = this.value;
      nodeSelect.innerHTML = '<option value="">-- 加载中... --</option>';
      nodeSelect.disabled = true;

      if (category) {
        if (category === 'poet') {
          // 诗人分类支持手动输入和下拉选择
          createPoetInputWithDropdown(nodeSelect);
        } else {
          loadNodesByCategory(category, nodeSelect);
        }
      } else {
        nodeSelect.innerHTML = '<option value="">-- 请选择 --</option>';
      }
    });
  }

  // 查看图谱按钮
  if (viewBtn) {
    viewBtn.addEventListener('click', function() {
      const category = categorySelect.value;
      let nodeName = '';

      if (category === 'poet') {
        // 诗人分类从输入框获取值
        const input = document.getElementById('graph-node-input');
        nodeName = input ? input.value.trim() : '';
      } else {
        nodeName = nodeSelect.value;
      }

      if (!category || !nodeName) {
        showErrorChart('请选择或输入分类和节点名称');
        return;
      }

      loadSubGraph(category, nodeName);
    });
  }

  // 显示全部图谱按钮
  if (refreshBtn) {
    refreshBtn.addEventListener('click', function() {
      loadFullGraph();
    });
  }

  // 初始化知识图谱
  const graphSection = document.getElementById('graph');
  if (graphSection) {
    const observer = new MutationObserver(function(mutations) {
      mutations.forEach(function(mutation) {
        if (mutation.target.classList.contains('active')) {
          if (!knowledgeGraphChart) {
            loadFullGraph();
          }
        }
      });
    });

    observer.observe(graphSection, { attributes: true, attributeFilter: ['class'] });
  }
});

// 创建诗人输入框+下拉选择组合
function createPoetInputWithDropdown(selectElement) {
  // 创建输入框
  const inputWrapper = document.createElement('div');
  inputWrapper.className = 'graph-input-wrapper';
  inputWrapper.style.display = 'flex';
  inputWrapper.style.gap = '0.5rem';
  inputWrapper.style.alignItems = 'center';

  const input = document.createElement('input');
  input.type = 'text';
  input.id = 'graph-node-input';
  input.className = 'graph-input';
  input.placeholder = '输入诗人姓名（如：李白）';
  input.style.padding = '0.5rem 0.75rem';
  input.style.border = '1px solid var(--paper-dark)';
  input.style.borderRadius = '4px';
  input.style.fontSize = '0.9rem';
  input.style.minWidth = '200px';

  // 下拉按钮
  const dropdownBtn = document.createElement('button');
  dropdownBtn.textContent = '选择';
  dropdownBtn.className = 'btn btn-secondary btn-sm';
  dropdownBtn.style.padding = '0.4rem 0.75rem';
  dropdownBtn.style.fontSize = '0.85rem';

  // 加载诗人列表下拉框
  const poetSelect = document.createElement('select');
  poetSelect.className = 'graph-filter-select';
  poetSelect.style.minWidth = '140px';
  poetSelect.style.display = 'none';

  inputWrapper.appendChild(input);
  inputWrapper.appendChild(dropdownBtn);
  inputWrapper.appendChild(poetSelect);

  // 替换selectElement为inputWrapper
  selectElement.parentNode.replaceChild(inputWrapper, selectElement);

  // 加载诗人列表
  loadPoetList(poetSelect, input);

  // 下拉按钮点击事件
  dropdownBtn.addEventListener('click', function() {
    if (poetSelect.style.display === 'none') {
      poetSelect.style.display = 'inline-block';
      dropdownBtn.textContent = '收起';
    } else {
      poetSelect.style.display = 'none';
      dropdownBtn.textContent = '选择';
    }
  });

  // 下拉框选择事件
  poetSelect.addEventListener('change', function() {
    if (this.value) {
      input.value = this.value;
      poetSelect.style.display = 'none';
      dropdownBtn.textContent = '选择';
    }
  });

  // 输入框回车事件
  input.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      document.getElementById('graph-view-btn').click();
    }
  });
}

// 加载诗人列表
async function loadPoetList(selectElement, inputElement) {
  try {
    const res = await fetch(`${API_BASE}/api/graph/nodes/category?category=${encodeURIComponent('诗人')}`);
    const json = await res.json();

    if (json.success && json.data) {
      selectElement.innerHTML = '<option value="">-- 选择诗人 --</option>';
      json.data.forEach(name => {
        const option = document.createElement('option');
        option.value = name;
        option.textContent = name;
        selectElement.appendChild(option);
      });
    }
  } catch (error) {
    console.error('加载诗人列表失败:', error);
  }
}

// 根据分类加载节点列表
async function loadNodesByCategory(category, selectElement) {
  try {
    const categoryMap = {
      'dynasty': '朝代',
      'poem': '诗词',
      'tag': '题材'
    };

    const res = await fetch(`${API_BASE}/api/graph/nodes/category?category=${encodeURIComponent(categoryMap[category])}`);
    const json = await res.json();

    if (json.success && json.data) {
      selectElement.innerHTML = '<option value="">-- 请选择 --</option>';
      json.data.forEach(node => {
        const option = document.createElement('option');
        option.value = node;
        option.textContent = node;
        selectElement.appendChild(option);
      });
      selectElement.disabled = false;
    } else {
      selectElement.innerHTML = '<option value="">-- 无数据 --</option>';
    }
  } catch (error) {
    console.error('加载节点列表失败:', error);
    selectElement.innerHTML = '<option value="">-- 加载失败 --</option>';
  }
}

// 显示错误信息图表
function showErrorChart(message) {
  const chartDom = document.getElementById('knowledge-graph-chart');
  if (!chartDom) return;

  // 销毁旧图表
  if (knowledgeGraphChart) {
    knowledgeGraphChart.dispose();
  }

  knowledgeGraphChart = null;

  // 显示错误信息
  chartDom.innerHTML = `
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:#666;">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="color:#999;margin-bottom:1rem;">
        <circle cx="12" cy="12" r="10"/>
        <path d="M12 8v4M12 16h.01"/>
      </svg>
      <p style="font-size:1.1rem;color:#666;margin-bottom:0.5rem;">暂无图谱数据</p>
      <p style="font-size:0.9rem;color:#999;">${message}</p>
      <p style="font-size:0.85rem;color:#999;margin-top:0.5rem;">请尝试选择其他诗人、朝代或题材</p>
    </div>
  `;
}

// 显示空图谱结果
function showEmptyGraphResult(name, type) {
  const chartDom = document.getElementById('knowledge-graph-chart');
  if (!chartDom) return;

  // 销毁旧图表
  if (knowledgeGraphChart) {
    knowledgeGraphChart.dispose();
  }

  knowledgeGraphChart = null;

  const typeMap = {
    'poet': '诗人',
    'dynasty': '朝代',
    'poem': '诗词',
    'tag': '题材'
  };

  // 显示空结果信息
  chartDom.innerHTML = `
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:#666;">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="color:#999;margin-bottom:1rem;">
        <circle cx="12" cy="12" r="10"/>
        <path d="M12 8v4M12 16h.01"/>
      </svg>
      <p style="font-size:1.1rem;color:#666;margin-bottom:0.5rem;">未找到「${name}」的关联数据</p>
      <p style="font-size:0.9rem;color:#999;">该${typeMap[type] || type}可能还没有构建知识图谱关联</p>
      <button onclick="loadFullGraph()" style="margin-top:1rem;padding:0.5rem 1rem;background:#455a64;color:white;border:none;border-radius:4px;cursor:pointer;">
        查看完整图谱
      </button>
    </div>
  `;
}

// 加载完整知识图谱
async function loadFullGraph() {
  const chartDom = document.getElementById('knowledge-graph-chart');
  if (!chartDom) return;

  // 显示加载状态
  chartDom.innerHTML = '<div style="text-align:center;padding:2rem;">加载中...</div>';

  try {
    const res = await fetch(`${API_BASE}/api/graph/knowledge?limit=1000`);
    const json = await res.json();

    if (json.success && json.data && json.data.nodes && json.data.nodes.length > 0) {
      renderKnowledgeGraph(chartDom, json.data);
      // 隐藏信息栏
      const graphInfo = document.getElementById('graph-info');
      if (graphInfo) {
        graphInfo.style.display = 'none';
      }
      // 重置筛选器
      const categorySelect = document.getElementById('graph-category');
      if (categorySelect) categorySelect.value = '';
      
      // 恢复原始select元素
      restoreOriginalSelect();
    } else {
      showErrorChart('知识图谱数据为空，请稍后重试');
    }
  } catch (error) {
    console.error('加载知识图谱失败:', error);
    showErrorChart('加载失败，请检查网络连接后重试');
  }
}

// 恢复原始select元素
function restoreOriginalSelect() {
  const inputWrapper = document.getElementById('graph-node-input')?.parentElement;
  if (inputWrapper) {
    const nodeSelect = document.createElement('select');
    nodeSelect.id = 'graph-node';
    nodeSelect.className = 'graph-filter-select';
    nodeSelect.disabled = true;
    nodeSelect.innerHTML = '<option value="">-- 请选择 --</option>';
    inputWrapper.parentNode.replaceChild(nodeSelect, inputWrapper);
  }
}

// 加载局部知识图谱
async function loadSubGraph(type, name) {
  const chartDom = document.getElementById('knowledge-graph-chart');
  if (!chartDom) return;

  // 显示加载状态
  chartDom.innerHTML = '<div style="text-align:center;padding:2rem;">加载中...</div>';

  currentGraphType = type;
  currentGraphNode = name;

  try {
    const res = await fetch(`${API_BASE}/api/graph/subgraph?type=${encodeURIComponent(type)}&name=${encodeURIComponent(name)}`);
    const json = await res.json();

    if (json.success && json.data && json.data.nodes && json.data.nodes.length > 0) {
      renderKnowledgeGraph(chartDom, json.data);

      // 显示信息栏
      const graphInfo = document.getElementById('graph-info');
      const graphCenterInfo = document.getElementById('graph-center-info');
      if (graphInfo && graphCenterInfo) {
        const categoryMap = {
          'poet': '诗人',
          'dynasty': '朝代',
          'poem': '诗词',
          'tag': '题材'
        };
        const nodeCount = json.data.nodes.length;
        const linkCount = json.data.links.length;
        graphCenterInfo.textContent = `当前查看：${categoryMap[type] || type} - ${name} | 共 ${nodeCount} 个节点，${linkCount} 条关联`;
        graphInfo.style.display = 'block';
      }
    } else {
      // 显示空结果
      showEmptyGraphResult(name, type);
    }
  } catch (error) {
    console.error('加载局部知识图谱失败:', error);
    showErrorChart('加载失败，请检查网络连接后重试');
  }
}

// 渲染知识图谱
function renderKnowledgeGraph(chartDom, data) {
  if (!chartDom) return;

  // 销毁旧图表
  if (knowledgeGraphChart) {
    knowledgeGraphChart.dispose();
  }

  // 初始化ECharts
  knowledgeGraphChart = echarts.init(chartDom);

  const option = {
    title: {
      text: '诗词知识图谱',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        if (params.dataType === 'node') {
          return `<strong>${params.data.name}</strong><br/>${params.data.description || ''}`;
        } else if (params.dataType === 'edge') {
          return `${params.data.source} → ${params.data.target}<br/>关系：${params.data.relation || ''}`;
        }
        return '';
      }
    },
    legend: {
      data: data.categories.map(c => c.value),
      bottom: 10
    },
    series: [{
      type: 'graph',
      layout: 'force',
      data: data.nodes,
      links: data.links,
      categories: data.categories,
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        formatter: '{b}'
      },
      force: {
        repulsion: 300,
        edgeLength: 100,
        gravity: 0.1,
        friction: 0.6,
        layoutAnimation: false
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 4
        }
      },
      edgeLabel: {
        show: true,
        formatter: function(params) {
          return params.data.relation || '';
        },
        fontSize: 10
      }
    }]
  };

  knowledgeGraphChart.setOption(option);

  // 禁用初始动画，保持图谱静止
  setTimeout(function() {
    if (knowledgeGraphChart) {
      knowledgeGraphChart.setOption({
        series: [{
          force: {
            repulsion: 300,
            edgeLength: 100,
            gravity: 0.1
          }
        }]
      });
    }
  }, 100);

  // 添加窗口大小变化时的自适应
  window.addEventListener('resize', function() {
    if (knowledgeGraphChart) {
      knowledgeGraphChart.resize();
    }
  });
}