/**
 * 诗韵雅集 - 古诗词智能问答 - 前端逻辑
 */
const API_BASE = '';

// 顶部提示
function showToast(message, type = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = message;
  toast.className = 'toast ' + type;
  void toast.offsetWidth;
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// ==================== 用户登录状态管理 ====================

// 检查登录状态
async function checkLoginStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/user/check`);
    const json = await res.json();
    
    const notLoggedEl = document.getElementById('user-not-logged');
    const loggedEl = document.getElementById('user-logged');
    
    if (json.success && json.loggedIn) {
      // 已登录
      notLoggedEl.style.display = 'none';
      loggedEl.style.display = 'block';
      
      const user = json.data;
      const displayName = user.nickname || user.username;
      document.getElementById('user-display-name').textContent = displayName;
      document.getElementById('dropdown-username').textContent = displayName;
    } else {
      // 未登录
      notLoggedEl.style.display = 'flex';
      loggedEl.style.display = 'none';
    }
  } catch (e) {
    console.error('检查登录状态失败:', e);
  }
}

// 退出登录
async function handleLogout() {
  try {
    const res = await fetch(`${API_BASE}/api/user/logout`, {
      method: 'POST'
    });
    const json = await res.json();
    
    if (json.success) {
      showToast('已退出登录', 'success');
      // 可选：跳转到首页
      setTimeout(() => { window.location.href = '/login.html'; }, 1500);
    }
  } catch (e) {
    console.error('退出登录失败:', e);
    showToast('退出失败，请重试', 'error');
  }
}

// 初始化用户下拉菜单交互
function initUserDropdown() {
  const avatarTrigger = document.getElementById('user-avatar-trigger');
  const dropdown = document.getElementById('user-dropdown');
  const logoutBtn = document.getElementById('btn-logout');
  
  if (avatarTrigger) {
    avatarTrigger.addEventListener('click', (e) => {
      e.stopPropagation();
      const isShown = dropdown.style.display === 'block';
      dropdown.style.display = isShown ? 'none' : 'block';
      avatarTrigger.classList.toggle('active', !isShown);
    });
  }
  
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      handleLogout();
    });
  }
  
  // 点击其他地方关闭下拉菜单
  document.addEventListener('click', (e) => {
    if (!dropdown.contains(e.target) && !avatarTrigger.contains(e.target)) {
      dropdown.style.display = 'none';
      if (avatarTrigger) avatarTrigger.classList.remove('active');
    }
  });
  
  // 下拉菜单中的导航链接点击事件
  dropdown.querySelectorAll('.dropdown-item[data-section]').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const section = item.dataset.section;
      dropdown.style.display = 'none';
      avatarTrigger.classList.remove('active');
      // 触发导航切换
      document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
      const navLink = document.querySelector(`.nav-link[data-section="${section}"]`);
      if (navLink) {
        navLink.classList.add('active');
        navLink.click();
      }
    });
  });
}

// 在页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', () => {
  checkLoginStatus();
  initUserDropdown();
});

// 导航切换
document.querySelectorAll('.nav-link').forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    const section = link.dataset.section;
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    link.classList.add('active');
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(section).classList.add('active');
    
    // 如果是首页，加载推荐诗词、随机古诗和初始化功能卡片
    if (section === 'home') {
      loadHomePoems();
      loadRandomPoem();
      setTimeout(initFeatureCards, 100);
    }
  });
});

// 搜索栏交互
const searchBar = document.querySelector('.search-bar');
const searchDropdown = document.getElementById('search-dropdown');
const topSearchInput = document.getElementById('top-search-input');
const topSearchBtn = document.getElementById('top-search-btn');

// 点击搜索栏展开/收起
searchBar.addEventListener('click', (e) => {
  e.stopPropagation();
  searchDropdown.classList.toggle('active');
});

// 点击页面其他地方收起搜索栏
document.addEventListener('click', (e) => {
  if (!searchBar.contains(e.target) && !searchDropdown.contains(e.target)) {
    searchDropdown.classList.remove('active');
  }
});

// 顶部搜索框回车搜索
topSearchInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {
    const keyword = topSearchInput.value.trim();
    if (keyword) {
      // 将关键词填入详细搜索框并执行搜索
      document.getElementById('search-keyword').value = keyword;
      searchPoems();
      // 切换到搜索页面
      document.querySelector('.nav-link[data-section="search"]').click();
    }
  }
});

// 顶部搜索按钮点击
topSearchBtn.addEventListener('click', () => {
  const keyword = topSearchInput.value.trim();
  if (keyword) {
    // 将关键词填入详细搜索框并执行搜索
    document.getElementById('search-keyword').value = keyword;
    searchPoems();
    // 切换到搜索页面
    document.querySelector('.nav-link[data-section="search"]').click();
  } else {
    // 展开详细搜索栏
    searchDropdown.classList.toggle('active');
  }
});

// 下拉搜索框搜索按钮点击
const dropdownSearchBtn = document.getElementById('dropdown-btn-search');
if (dropdownSearchBtn) {
  dropdownSearchBtn.addEventListener('click', () => {
    // 将下拉搜索框的值复制到主搜索框
    document.getElementById('search-keyword').value = document.getElementById('dropdown-search-keyword').value.trim();
    document.getElementById('search-author').value = document.getElementById('dropdown-search-author').value.trim();
    document.getElementById('search-dynasty').value = document.getElementById('dropdown-search-dynasty').value.trim();
    document.getElementById('search-tag').value = document.getElementById('dropdown-search-tag').value.trim();
    // 执行搜索
    searchPoems();
    // 切换到搜索页面
    document.querySelector('.nav-link[data-section="search"]').click();
    // 收起下拉搜索框
    searchDropdown.classList.remove('active');
  });
}

// 首页按钮导航
document.querySelectorAll('.hero-buttons a').forEach(btn => {
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    const section = btn.dataset.section;
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelector(`.nav-link[data-section="${section}"]`).classList.add('active');
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(section).classList.add('active');
  });
});

// 加载首页推荐诗词
async function loadHomePoems() {
  const slider = document.getElementById('home-poems-slider');
  if (!slider) return;
  
  try {
    // 获取推荐诗词（使用搜索接口获取热门诗词）
    const res = await fetch(`${API_BASE}/api/poems/search`);
    const json = await res.json();
    
    if (json.success && json.data) {
      const poems = json.data.slice(0, 6); // 取前6首诗词
      
      // 生成滑块内容
      let sliderHtml = `
        <div class="slider-container" id="slider-container">
          ${poems.map(poem => `
            <div class="slider-item" data-title="${escapeHtml(poem.title)}">
              <div class="poem-title">《${escapeHtml(poem.title)}》</div>
              <div class="poem-meta">${escapeHtml(poem.author)} · ${escapeHtml(poem.dynasty)}</div>
              <div class="poem-preview">${escapeHtml((poem.content || '').slice(0, 100))}...</div>
            </div>
          `).join('')}
        </div>
        <div class="slider-controls">
          <button class="slider-btn" id="slider-prev">‹</button>
          <button class="slider-btn" id="slider-next">›</button>
        </div>
      `;
      
      slider.innerHTML = sliderHtml;
      
      // 添加诗词卡片点击事件
      slider.querySelectorAll('.slider-item').forEach(item => {
        item.addEventListener('click', () => {
          const title = item.dataset.title;
          showPoemDetail(title);
        });
      });
      
      // 初始化滑块控制
      initSlider();
    }
  } catch (error) {
    console.error('加载推荐诗词失败:', error);
  }
}

// 初始化滑块控制
function initSlider() {
  const container = document.getElementById('slider-container');
  const prevBtn = document.getElementById('slider-prev');
  const nextBtn = document.getElementById('slider-next');
  
  if (!container || !prevBtn || !nextBtn) return;
  
  let position = 0;
  const itemWidth = 300 + 24; //  item width + margin
  const itemsCount = container.children.length;
  const maxPosition = Math.max(0, (itemsCount - 3) * itemWidth); // 显示3个项目
  
  prevBtn.addEventListener('click', () => {
    position = Math.max(0, position - itemWidth);
    container.style.transform = `translateX(-${position}px)`;
  });
  
  nextBtn.addEventListener('click', () => {
    position = Math.min(maxPosition, position + itemWidth);
    container.style.transform = `translateX(-${position}px)`;
  });
}

// 为首页核心功能卡片添加点击跳转
function initFeatureCards() {
  const featureCards = document.querySelectorAll('.feature-card');
  featureCards.forEach(card => {
    card.addEventListener('click', () => {
      const cardText = card.textContent.toLowerCase();
      let section = '';
      
      if (cardText.includes('多维检索')) {
        section = 'search';
      } else if (cardText.includes('智能问答')) {
        section = 'qa';
      } else if (cardText.includes('飞花令')) {
        section = 'feihualing';
      } else if (cardText.includes('背诵默写')) {
        section = 'recite';
      } else if (cardText.includes('知识图谱')) {
        section = 'graph';
      } else if (cardText.includes('诗人行迹')) {
        section = 'trajectory';
      }
      
      if (section) {
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        document.querySelector(`.nav-link[data-section="${section}"]`).classList.add('active');
        document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
        document.getElementById(section).classList.add('active');
      }
    });
    
    // 添加鼠标悬停效果
    card.style.cursor = 'pointer';
  });
}

// 加载随机古诗
async function loadRandomPoem() {
  const container = document.getElementById('random-poem-content');
  if (!container) return;
  
  try {
    // 获取随机古诗（使用搜索接口获取所有古诗，然后随机选择）
    const res = await fetch(`${API_BASE}/api/poems/search`);
    const json = await res.json();
    
    if (json.success && json.data && json.data.length > 0) {
      // 随机选择一首古诗
      const randomIndex = Math.floor(Math.random() * json.data.length);
      const poem = json.data[randomIndex];
      
      // 显示随机古诗
      container.innerHTML = `
        <div class="random-poem-item" data-title="${escapeHtml(poem.title)}">
          <div class="poem-title">《${escapeHtml(poem.title)}》</div>
          <div class="poem-meta">${escapeHtml(poem.author)} · ${escapeHtml(poem.dynasty)}</div>
          <div class="poem-text">${escapeHtml(formatContent(poem.content))}</div>
        </div>
      `;
      
      // 添加点击事件
      const poemItem = container.querySelector('.random-poem-item');
      if (poemItem) {
        poemItem.addEventListener('click', () => {
          const title = poemItem.dataset.title;
          showPoemDetail(title);
        });
        // 添加鼠标悬停效果
        poemItem.style.cursor = 'pointer';
      }
    } else {
      container.innerHTML = '<p class="empty">暂无诗词</p>';
    }
  } catch (error) {
    console.error('加载随机古诗失败:', error);
    container.innerHTML = '<p class="empty">加载失败</p>';
  }
}

// 页面加载时初始化首页
window.addEventListener('load', () => {
  const activeSection = document.querySelector('.section.active');
  if (activeSection && activeSection.id === 'home') {
    loadHomePoems();
    loadRandomPoem(); // 加载随机古诗
  }
  
  // 初始化功能卡片点击事件
  initFeatureCards();
  
  // 添加刷新按钮点击事件
  const refreshBtn = document.getElementById('btn-refresh-poem');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', loadRandomPoem);
  }
  
  // 初始化回到顶部按钮
  initBackToTop();
  
  // 重新绑定搜索按钮点击事件
  const searchBtn = document.getElementById('btn-search');
  if (searchBtn) {
    searchBtn.addEventListener('click', searchPoems);
  }
  
  // 绑定下拉搜索框按钮事件
  const dropdownSearchBtn = document.getElementById('dropdown-btn-search');
  if (dropdownSearchBtn) {
    dropdownSearchBtn.addEventListener('click', () => {
      // 将下拉搜索框的值复制到主搜索框
      document.getElementById('search-keyword').value = document.getElementById('dropdown-search-keyword').value.trim();
      document.getElementById('search-author').value = document.getElementById('dropdown-search-author').value.trim();
      document.getElementById('search-dynasty').value = document.getElementById('dropdown-search-dynasty').value.trim();
      document.getElementById('search-tag').value = document.getElementById('dropdown-search-tag').value.trim();
      // 执行搜索
      searchPoems();
      // 切换到搜索页面
      document.querySelector('.nav-link[data-section="search"]').click();
      // 收起下拉搜索框
      searchDropdown.classList.remove('active');
    });
  }
});

// 多维检索
async function searchPoems() {
  const keyword = document.getElementById('search-keyword').value.trim();
  const author = document.getElementById('search-author').value.trim();
  const dynasty = document.getElementById('search-dynasty').value.trim();
  const tag = document.getElementById('search-tag').value.trim();

  const params = new URLSearchParams();
  if (keyword) params.set('keyword', keyword);
  if (author) params.set('author', author);
  if (dynasty) params.set('dynasty', dynasty);
  if (tag) params.set('tag', tag);

  const res = await fetch(`${API_BASE}/api/poems/search?${params}`);
  const json = await res.json();
  const container = document.getElementById('search-results');

  if (!json.success) {
    container.innerHTML = '<p class="empty">检索失败</p>';
    return;
  }

  const poems = json.data || [];
  if (poems.length === 0) {
    container.innerHTML = '<p class="empty">未找到相关诗词</p>';
    return;
  }

  // 如果是诗人检索，先展示诗人介绍
  if (author) {
    // 提取诗人信息（假设所有结果都是同一诗人的作品）
    const poetInfo = poems[0];
    const poetDynasty = poetInfo.dynasty || '未知朝代';
    const poemCount = poems.length;
    
    // 生成诗人介绍
    const authorIntro = `
      <div class="poet-intro" data-author="${escapeHtml(author)}">
        <h3>${escapeHtml(author)}</h3>
        <p class="poet-meta">${escapeHtml(poetDynasty)} · 共 ${poemCount} 首诗词</p>
        <p class="poet-description">${escapeHtml(author)}是${poetDynasty}著名诗人，其作品题材广泛，风格独特，在中国文学史上占有重要地位。以下是其部分代表作品：</p>
      </div>
    `;
    
    // 生成诗词卡片
    const poemCards = poems.map(p => `
      <div class="poem-card" data-title="${escapeHtml(p.title)}">
        <div class="title">《${escapeHtml(p.title)}》</div>
        <div class="meta">${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</div>
        <div class="preview">${escapeHtml((p.content || '').slice(0, 80))}...</div>
      </div>
    `).join('');
    
    container.innerHTML = authorIntro + poemCards;
  } else {
    // 普通搜索，直接展示诗词卡片
    container.innerHTML = poems.map(p => `
      <div class="poem-card" data-title="${escapeHtml(p.title)}">
        <div class="title">《${escapeHtml(p.title)}》</div>
        <div class="meta">${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</div>
        <div class="preview">${escapeHtml((p.content || '').slice(0, 80))}...</div>
      </div>
    `).join('');
  }

  // 添加诗词卡片点击事件
  container.querySelectorAll('.poem-card').forEach(card => {
    card.addEventListener('click', () => showPoemDetail(card.dataset.title));
  });
  
  // 添加诗人介绍点击事件
  container.querySelectorAll('.poet-intro').forEach(intro => {
    intro.addEventListener('click', () => {
      const author = intro.dataset.author;
      showPoetDetail(author);
    });
    // 添加鼠标悬停效果
    intro.style.cursor = 'pointer';
  });
}

document.getElementById('btn-search').addEventListener('click', searchPoems);

// 智能问答
async function askQuestion() {
  const input = document.getElementById('qa-input');
  const question = input.value.trim();
  const resultEl = document.getElementById('qa-result');

  if (!question) {
    resultEl.innerHTML = '<p class="empty">请输入问题</p>';
    return;
  }

  resultEl.innerHTML = '<p class="empty">思考中...</p>';

  const res = await fetch(`${API_BASE}/api/qa`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question })
  });
  const json = await res.json();

  if (!json.success) {
    resultEl.innerHTML = `<p class="empty">${escapeHtml(json.message || '暂无答案')}</p>`;
    return;
  }

  let html = '';
  const data = json.data;
  if (Array.isArray(data)) {
    if (data.length > 0 && typeof data[0] === 'object' && data[0].title) {
      html = data.map(p => `
        <div class="poem-card" style="cursor:pointer;margin-bottom:0.5rem" data-title="${escapeHtml(p.title)}">
          <strong>《${escapeHtml(p.title)}》</strong> — ${escapeHtml(p.author || '')} · ${escapeHtml(p.dynasty || '')}<br>
          <span style="font-size:0.9rem">${escapeHtml((p.content || '').slice(0, 100))}...</span>
        </div>
      `).join('');
      resultEl.innerHTML = html;
      resultEl.querySelectorAll('.poem-card').forEach(c => {
        c.addEventListener('click', () => showPoemDetail(c.dataset.title));
      });
    } else {
      html = '<ul>' + data.map(item => `<li>${escapeHtml(String(item))}</li>`).join('') + '</ul>';
      resultEl.innerHTML = html;
    }
  } else {
    resultEl.innerHTML = `<p>${escapeHtml(String(data))}</p>`;
  }
}

document.getElementById('btn-qa').addEventListener('click', askQuestion);

// 飞花令游戏状态
let feihualingGameState = {
  targetChar: '',
  usedLines: new Set(),
  allLines: [],
  gameStarted: false
};

// 诗词接龙游戏状态
let solitaireGameState = {
  mode: 'next', // 'next' 上句接下句, 'prev' 下句猜上句
  currentPoem: null,
  currentIndex: 0,
  score: 0,
  usedPoems: new Set(),
  gameStarted: false
};

// 飞花令
async function feihualing() {
  const char = document.getElementById('feihualing-char').value.trim();
  const resultEl = document.getElementById('feihualing-result');
  const gameEl = document.getElementById('feihualing-game');

  if (!char || char.length !== 1) {
    resultEl.innerHTML = '<p class="empty">请输入一个汉字</p>';
    return;
  }

  // 隐藏结果区域，显示游戏区域
  resultEl.style.display = 'none';
  gameEl.style.display = 'block';

  // 初始化游戏状态
  feihualingGameState = {
    targetChar: char,
    usedLines: new Set(),
    allLines: [],
    gameStarted: true
  };

  // 获取所有含该字的诗句
  const res = await fetch(`${API_BASE}/api/feihualing?char=${encodeURIComponent(char)}`);
  const json = await res.json();

  if (!json.success || !json.data || json.data.length === 0) {
    document.getElementById('game-status').innerHTML = `<p class="empty">未找到含「${char}」的诗句</p>`;
    return;
  }

  feihualingGameState.allLines = json.data;

  // 系统先抛出第一句
  startFeihualingGame();
}

// 开始飞花令游戏
function startFeihualingGame() {
  const statusEl = document.getElementById('game-status');
  const historyEl = document.getElementById('game-history');
  const char = feihualingGameState.targetChar;

  // 显示游戏状态
  statusEl.innerHTML = `<h4>飞花令：「${char}」</h4><p>系统先开始，请输入含「${char}」的诗句</p>`;
  historyEl.innerHTML = '';

  // 系统抛出第一句
  systemThrowLine();

  // 绑定事件
  document.getElementById('btn-submit').addEventListener('click', submitUserLine);
  document.getElementById('btn-hint').addEventListener('click', showHint);

  // 回车提交
  document.getElementById('user-input').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      submitUserLine();
    }
  });
}

// 系统抛出一句
function systemThrowLine() {
  const historyEl = document.getElementById('game-history');
  const char = feihualingGameState.targetChar;
  const allLines = feihualingGameState.allLines;
  const usedLines = feihualingGameState.usedLines;

  // 过滤出未使用的诗句（忽略标点符号）
  const availableLines = allLines.filter(line => {
    return !isLineDuplicate(line.line, usedLines);
  });

  if (availableLines.length === 0) {
    showAlert(`游戏结束，已无更多含「${char}」的诗句`);
    document.getElementById('game-status').innerHTML = `<h4>游戏结束</h4><p>已无更多含「${char}」的诗句</p>`;
    return;
  }

  // 随机选择一句
  const randomIndex = Math.floor(Math.random() * availableLines.length);
  const line = availableLines[randomIndex];

  // 添加到已使用集合
  usedLines.add(line.line);

  // 显示系统抛出的诗句
  historyEl.innerHTML += `
    <div class="game-turn system-turn">
      <div class="turn-label">系统：</div>
      <div class="turn-content">${escapeHtml(line.line)} — 《${escapeHtml(line.title)}》${escapeHtml(line.author)}</div>
    </div>
  `;

  // 滚动到底部
  historyEl.scrollTop = historyEl.scrollHeight;

  // 清空用户输入
  document.getElementById('user-input').value = '';
}

// 移除标点符号的函数
function removePunctuation(text) {
  return text.replace(/[，。；：？！、]/g, '');
}

// 检测是否是有效诗句
function isValidPoemLine(line) {
  // 简单的有效诗句检测：长度至少为5个字符，且包含至少一个汉字
  if (line.length < 5) return false;
  if (!/[\u4e00-\u9fa5]/.test(line)) return false;
  return true;
}

// 检查诗句是否重复（忽略标点符号）
function isLineDuplicate(line, usedLines) {
  const lineWithoutPunc = removePunctuation(line);
  for (const usedLine of usedLines) {
    const usedLineWithoutPunc = removePunctuation(usedLine);
    if (lineWithoutPunc === usedLineWithoutPunc) {
      return true;
    }
  }
  return false;
}

// 显示提示对话框
function showAlert(message) {
  // 创建对话框
  const alertDiv = document.createElement('div');
  alertDiv.style.position = 'fixed';
  alertDiv.style.top = '50%';
  alertDiv.style.left = '50%';
  alertDiv.style.transform = 'translate(-50%, -50%)';
  alertDiv.style.background = 'white';
  alertDiv.style.padding = '2rem';
  alertDiv.style.borderRadius = '8px';
  alertDiv.style.boxShadow = '0 4px 20px rgba(0,0,0,0.15)';
  alertDiv.style.zIndex = '1000';
  alertDiv.style.textAlign = 'center';
  alertDiv.style.minWidth = '300px';
  
  // 对话框内容
  alertDiv.innerHTML = `
    <p style="margin-bottom: 1.5rem; font-size: 1.1rem;">${message}</p>
    <button class="btn btn-primary" style="padding: 0.5rem 1.5rem;">确定</button>
  `;
  
  // 添加到页面
  document.body.appendChild(alertDiv);
  
  // 点击确定按钮关闭对话框
  alertDiv.querySelector('button').addEventListener('click', () => {
    document.body.removeChild(alertDiv);
  });
}

// 用户提交诗句
function submitUserLine() {
  const userInput = document.getElementById('user-input').value.trim();
  const char = feihualingGameState.targetChar;
  const usedLines = feihualingGameState.usedLines;
  const historyEl = document.getElementById('game-history');
  const statusEl = document.getElementById('game-status');

  if (!userInput) {
    showAlert('请输入诗句');
    return;
  }

  // 检查是否是有效诗句
  if (!isValidPoemLine(userInput)) {
    showAlert('请输入有效的诗句');
    return;
  }

  // 检查是否包含目标字
  if (!userInput.includes(char)) {
    showAlert(`诗句中未包含「${char}」`);
    return;
  }

  // 检查是否重复（忽略标点符号）
  if (isLineDuplicate(userInput, usedLines)) {
    showAlert('该诗句已使用过');
    return;
  }

  // 添加到已使用集合
  usedLines.add(userInput);

  // 显示用户提交的诗句
  historyEl.innerHTML += `
    <div class="game-turn user-turn">
      <div class="turn-label">用户：</div>
      <div class="turn-content">${escapeHtml(userInput)}</div>
    </div>
  `;

  // 滚动到底部
  historyEl.scrollTop = historyEl.scrollHeight;

  // 系统继续抛出下一句
  setTimeout(systemThrowLine, 1000);
}

// 显示提示
function showHint() {
  const char = feihualingGameState.targetChar;
  const allLines = feihualingGameState.allLines;
  const usedLines = feihualingGameState.usedLines;
  const historyEl = document.getElementById('game-history');

  // 过滤出未使用的诗句（忽略标点符号）
  const availableLines = allLines.filter(line => {
    return !isLineDuplicate(line.line, usedLines);
  });

  if (availableLines.length === 0) {
    showAlert(`已无更多含「${char}」的诗句`);
    return;
  }

  // 随机选择一句作为提示
  const randomIndex = Math.floor(Math.random() * availableLines.length);
  const line = availableLines[randomIndex];

  // 添加到已使用集合
  usedLines.add(line.line);

  // 显示提示
  historyEl.innerHTML += `
    <div class="game-turn hint-turn">
      <div class="turn-label">提示：</div>
      <div class="turn-content">${escapeHtml(line.line)} — 《${escapeHtml(line.title)}》${escapeHtml(line.author)}</div>
    </div>
  `;

  // 滚动到底部
  historyEl.scrollTop = historyEl.scrollHeight;

  // 系统继续抛出下一句
  setTimeout(systemThrowLine, 1000);
}

document.getElementById('btn-feihualing').addEventListener('click', feihualing);



// 背诵
async function recite() {
  const res = await fetch(`${API_BASE}/api/recite`);
  const json = await res.json();
  const container = document.getElementById('recite-content');
  const answerEl = document.getElementById('dictation-answer');
  answerEl.style.display = 'none';

  if (!json.success || !json.data) {
    container.innerHTML = '<p class="empty">获取失败</p>';
    return;
  }

  const p = json.data;
  container.innerHTML = `
    <div class="poem-title">《${escapeHtml(p.title)}》</div>
    <div class="poem-meta">${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)}</div>
    <div class="poem-text">${escapeHtml(formatContent(p.content))}</div>
    <div style="margin-top: 1.5rem;">
      <button class="btn btn-primary" id="btn-start-dictation">开始默写</button>
    </div>
  `;
  
  // 保存诗词数据
  container.dataset.poemTitle = p.title;
  container.dataset.poemAuthor = p.author;
  container.dataset.poemDynasty = p.dynasty;
  container.dataset.poemContent = p.content;
  
  // 添加开始默写按钮事件
  document.getElementById('btn-start-dictation').addEventListener('click', () => {
    startDictation(container);
  });
}

// 开始默写
function startDictation(container) {
  const title = container.dataset.poemTitle;
  const author = container.dataset.poemAuthor;
  const dynasty = container.dataset.poemDynasty;
  const content = container.dataset.poemContent;
  
  // 按行分割内容
  const lines = formatContent(content).split('\n').filter(line => line.trim());
  let inputHtml = '';
  
  // 为每行创建一个输入框
  lines.forEach((line, index) => {
    const inputId = `recite-line-${index}`;
    // 计算输入框的长度，根据诗句长度动态调整
    const inputLength = line.length;
    inputHtml += `
      <div style="margin-bottom: 0.8rem;">
        <input type="text" class="dictation-line-input" id="${inputId}" placeholder="请输入第${index + 1}行" style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 4px; font-family: 'Noto Serif SC', serif; font-size: 1rem;">
      </div>
    `;
  });

  container.innerHTML = `
    <div class="poem-title">默写：《${escapeHtml(title)}》</div>
    <div class="poem-meta">${escapeHtml(author)} · ${escapeHtml(dynasty)}</div>
    <div style="margin-top: 1rem;">
      ${inputHtml}
    </div>
    <div style="margin-top: 1.5rem;">
      <button class="btn btn-primary" id="btn-submit-recite">提交答案</button>
    </div>
  `;
  
  // 添加提交按钮事件
  document.getElementById('btn-submit-recite').addEventListener('click', () => {
    submitRecite(container, content);
  });
}

// 提交背诵答案
function submitRecite(container, originalContent) {
  const answerEl = document.getElementById('dictation-answer');
  
  // 获取用户输入
  const inputs = container.querySelectorAll('.dictation-line-input');
  const userLines = Array.from(inputs).map(input => input.value.trim());
  
  // 按行分割原始内容
  const originalLines = formatContent(originalContent).split('\n').filter(line => line.trim());
  
  // 提取原始内容中的所有汉字（不包括标点和换行）
  const originalChars = originalContent.split('');
  const originalWords = [];
  
  for (let i = 0; i < originalChars.length; i++) {
    if (!originalChars[i].match(/[，。；：？！\n]/)) {
      originalWords.push(originalChars[i]);
    }
  }
  
  // 提取用户输入中的所有汉字
  let userWords = [];
  userLines.forEach(line => {
    const lineChars = line.split('');
    lineChars.forEach(char => {
      if (!char.match(/[，。；：？！\n]/)) {
        userWords.push(char);
      }
    });
  });
  
  // 生成对比结果
  let comparisonHtml = '';
  let inputIndex = 0;
  
  for (let i = 0; i < originalChars.length; i++) {
    const char = originalChars[i];
    if (char.match(/[，。；：？！\n]/)) {
      comparisonHtml += char;
    } else {
      const userAnswer = userWords[inputIndex] || '';
      const isCorrect = userAnswer === originalWords[inputIndex];
      comparisonHtml += isCorrect 
        ? `<span class="correct">${escapeHtml(userAnswer || '_')}</span>`
        : `<span class="incorrect">${escapeHtml(userAnswer || '_')}</span>`;
      inputIndex++;
    }
  }
  
  // 计算正确率
  let correctCount = 0;
  for (let i = 0; i < Math.min(userWords.length, originalWords.length); i++) {
    if (userWords[i] === originalWords[i]) {
      correctCount++;
    }
  }
  
  // 显示结果
  answerEl.innerHTML = `
    <p style="margin-bottom: 0.5rem; font-weight: 600">提交结果：</p>
    <p>答对 ${correctCount} 题，共 ${originalWords.length} 题</p>
    <p style="margin-top: 1rem; margin-bottom: 0.5rem; font-weight: 600">您的答案：</p>
    <div class="poem-text">${comparisonHtml}</div>
    <p style="margin-top: 1rem; margin-bottom: 0.5rem; font-weight: 600">参考答案：</p>
    <div class="poem-text">${escapeHtml(formatContent(originalContent))}</div>
  `;
  answerEl.style.display = 'block';
}

document.getElementById('btn-recite').addEventListener('click', recite);

// 默写
async function dictation() {
  const res = await fetch(`${API_BASE}/api/dictation`);
  const json = await res.json();
  const container = document.getElementById('recite-content');
  const answerEl = document.getElementById('dictation-answer');
  answerEl.style.display = 'none';

  if (!json.success || !json.data) {
    container.innerHTML = '<p class="empty">获取失败</p>';
    return;
  }

  const p = json.data;
  const contentBlank = p.content_blank || p.content;
  const contentOriginal = p.content;
  
  // 生成带输入框的内容
  let interactiveContent = escapeHtml(formatContent(contentBlank));
  let inputCount = 0;
  
  // 替换空白为输入框
  interactiveContent = interactiveContent.replace(/□/g, () => {
    const inputId = `blank-${inputCount++}`;
    return `<input type="text" class="dictation-input" id="${inputId}" maxlength="1" placeholder="_" style="width: 2rem; height: 1.5rem; text-align: center; margin: 0 0.2rem; border: 1px solid #ccc; border-radius: 4px; font-family: 'Noto Serif SC', serif; font-size: 1rem;">`;
  });

  container.innerHTML = `
    <div class="poem-title">填空默写</div>
    <div class="poem-meta">${escapeHtml(p.author)} · 《${escapeHtml(p.title)}》</div>
    <div class="poem-text">${interactiveContent}</div>
    <div style="margin-top: 1rem;">
      <button class="btn btn-primary" id="btn-submit-dictation">提交答案</button>
    </div>
  `;
  
  // 保存原始内容和空白内容，用于判定答案和高亮显示
  container.dataset.originalContent = contentOriginal;
  container.dataset.contentBlank = contentBlank;
  container.dataset.poemTitle = p.title;
  container.dataset.poemAuthor = p.author;
  container.dataset.poemDynasty = p.dynasty;
  
  // 添加提交按钮事件
  document.getElementById('btn-submit-dictation').addEventListener('click', () => {
    submitDictation(container);
  });
}

// 提交默写答案
async function submitDictation(container) {
  const originalContent = container.dataset.originalContent;
  const poemTitle = container.dataset.poemTitle;
  const poemAuthor = container.dataset.poemAuthor;
  const poemDynasty = container.dataset.poemDynasty;
  
  // 获取用户输入
  const inputs = container.querySelectorAll('.dictation-input');
  const userAnswers = Array.from(inputs).map(input => input.value.trim());
  
  // 提取原始内容中的空白位置的正确字符
  const originalChars = originalContent.split('');
  const blankPositions = [];
  let index = 0;
  
  for (let i = 0; i < originalChars.length; i++) {
    if (originalChars[i] !== '。' && originalChars[i] !== '，' && originalChars[i] !== '、' && originalChars[i] !== '；' && originalChars[i] !== '：' && originalChars[i] !== '？' && originalChars[i] !== '！' && originalChars[i] !== '\n') {
      blankPositions.push(originalChars[i]);
    }
  }
  
  // 判定答案
  let correctCount = 0;
  for (let i = 0; i < userAnswers.length; i++) {
    if (userAnswers[i] === blankPositions[i]) {
      correctCount++;
    }
  }
  
  // 生成高亮显示的答案
  const contentBlank = container.dataset.contentBlank;
  let highlightedContent = escapeHtml(formatContent(originalContent));
  
  // 分析空白位置并高亮显示
  const blankContent = formatContent(contentBlank);
  const originalFormatted = formatContent(originalContent);
  
  // 查找所有空白位置并高亮
  let result = '';
  let originalIndex = 0;
  
  for (let i = 0; i < blankContent.length; i++) {
    if (blankContent[i] === '□') {
      // 找到对应的原始字符并高亮
      while (originalIndex < originalFormatted.length && 
             (originalFormatted[originalIndex] === '。' || 
              originalFormatted[originalIndex] === '，' || 
              originalFormatted[originalIndex] === '、' || 
              originalFormatted[originalIndex] === '；' || 
              originalFormatted[originalIndex] === '：' || 
              originalFormatted[originalIndex] === '？' || 
              originalFormatted[originalIndex] === '！' || 
              originalFormatted[originalIndex] === '\n')) {
        result += originalFormatted[originalIndex];
        originalIndex++;
      }
      
      if (originalIndex < originalFormatted.length) {
        result += `<span class="blank-highlight">${escapeHtml(originalFormatted[originalIndex])}</span>`;
        originalIndex++;
      }
    } else {
      if (originalIndex < originalFormatted.length) {
        result += originalFormatted[originalIndex];
        originalIndex++;
      }
    }
  }
  
  // 显示结果
  const answerEl = document.getElementById('dictation-answer');
  answerEl.innerHTML = `
    <p style="margin-bottom:0.5rem;font-weight:600">提交结果：</p>
    <p>答对 ${correctCount} 题，共 ${userAnswers.length} 题</p>
    <p style="margin-bottom:0.5rem;font-weight:600">参考答案：</p>
    <div class="poem-text">${result}</div>
  `;
  answerEl.style.display = 'block';
  
  // 调用AI接口生成诗词描述
  try {
    const res = await fetch(`${API_BASE}/api/ai/describe_poem`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: poemTitle,
        author: poemAuthor,
        dynasty: poemDynasty,
        content: originalContent
      })
    });
    const json = await res.json();
    
    if (json.success && json.data) {
      const description = json.data.description;
      let detailHtml = `
        <div style="margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #eee;">
          <h4 style="margin-bottom: 0.5rem;">诗词介绍</h4>
          <div class="detail-meta">${escapeHtml(poemAuthor)} · ${escapeHtml(poemDynasty)}</div>
          <div class="detail-section"><p>${escapeHtml(description)}</p></div>
        </div>
      `;
      answerEl.innerHTML += detailHtml;
    }
  } catch (error) {
    console.error('获取诗词描述失败:', error);
    // 失败时显示默认介绍
    const defaultDescription = `《${poemTitle}》是${poemAuthor}在${poemDynasty}时期创作的经典作品。此诗语言凝练，意境深远，展现了中国古典诗歌的独特魅力。`;
    let detailHtml = `
      <div style="margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #eee;">
        <h4 style="margin-bottom: 0.5rem;">诗词介绍</h4>
        <div class="detail-meta">${escapeHtml(poemAuthor)} · ${escapeHtml(poemDynasty)}</div>
        <div class="detail-section"><p>${escapeHtml(defaultDescription)}</p></div>
      </div>
    `;
    answerEl.innerHTML += detailHtml;
  }
}

document.getElementById('btn-dictation').addEventListener('click', dictation);

// 分类导航
const navTabs = document.querySelectorAll('.tab-btn');
const navContent = document.getElementById('nav-content');
const navSearchInput = document.getElementById('nav-search-input');
const btnNavSearch = document.getElementById('btn-nav-search');

// 存储当前分类数据
let currentTab = 'authors';
let currentItems = [];

navTabs.forEach(btn => {
  btn.addEventListener('click', async () => {
    navTabs.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const tab = btn.dataset.tab;
    currentTab = tab;
    await loadNavItems(tab);
  });
});

// 加载分类项
async function loadNavItems(tab) {
  const res = await fetch(`${API_BASE}/api/nav/${tab}`);
  const json = await res.json();
  if (!json.success) return;
  currentItems = json.data || [];
  renderNavItems(currentItems);
}

// 渲染分类项
function renderNavItems(items) {
  navContent.innerHTML = items.map(item =>
    `<span class="nav-item" data-type="${currentTab}" data-name="${escapeHtml(item.name)}">${escapeHtml(item.name)} (${item.count || ''})</span>`
  ).join('');
  navContent.querySelectorAll('.nav-item').forEach(el => {
    el.addEventListener('click', () => {
      const type = el.dataset.type;
      const name = el.dataset.name;
      if (type === 'authors') document.getElementById('search-author').value = name;
      else if (type === 'tags') document.getElementById('search-tag').value = name;
      else if (type === 'dynasties') document.getElementById('search-dynasty').value = name;
      document.querySelector('.nav-link[data-section="search"]').click();
      document.querySelector('.nav-link[data-section="search"]').classList.add('active');
      searchPoems();
    });
  });
}

// 搜索功能
btnNavSearch.addEventListener('click', () => {
  const searchTerm = navSearchInput.value.trim().toLowerCase();
  if (!searchTerm) {
    renderNavItems(currentItems);
    return;
  }
  
  const filteredItems = currentItems.filter(item => 
    item.name.toLowerCase().includes(searchTerm)
  );
  renderNavItems(filteredItems);
});

// 回车键搜索
navSearchInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {
    btnNavSearch.click();
  }
});

// 初始化
navTabs[0].click();

// 诗词接龙游戏

// 初始化诗词接龙游戏
function initSolitaire() {
  // 绑定模式切换按钮事件
  document.getElementById('btn-mode-next').addEventListener('click', () => {
    solitaireGameState.mode = 'next';
    document.getElementById('btn-mode-next').classList.add('active');
    document.getElementById('btn-mode-prev').classList.remove('active');
    startSolitaireGame();
  });
  
  document.getElementById('btn-mode-prev').addEventListener('click', () => {
    solitaireGameState.mode = 'prev';
    document.getElementById('btn-mode-prev').classList.add('active');
    document.getElementById('btn-mode-next').classList.remove('active');
    startSolitaireGame();
  });
  
  // 绑定提交和提示按钮事件
  document.getElementById('btn-solitaire-submit').addEventListener('click', submitSolitaireAnswer);
  document.getElementById('btn-solitaire-hint').addEventListener('click', showSolitaireHint);
  
  // 绑定回车键提交
  document.getElementById('solitaire-input').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      submitSolitaireAnswer();
    }
  });
  
  // 初始开始游戏
  startSolitaireGame();
}

// 开始诗词接龙游戏
async function startSolitaireGame() {
  const statusEl = document.getElementById('solitaire-status');
  const questionEl = document.getElementById('solitaire-question');
  const scoreEl = document.getElementById('solitaire-score');
  
  statusEl.innerHTML = '<p>加载中...</p>';
  questionEl.innerHTML = '';
  
  try {
    // 获取诗词数据
    const res = await fetch(`${API_BASE}/api/poems/search`);
    const json = await res.json();
    
    if (!json.success || !json.data || json.data.length === 0) {
      statusEl.innerHTML = '<p class="empty">未找到诗词数据</p>';
      return;
    }
    
    // 过滤出有足够行数的诗词（至少2句）
    const validPoems = json.data.filter(poem => {
      const lines = formatContent(poem.content).split('\n').filter(line => line.trim());
      return lines.length >= 2;
    });
    
    if (validPoems.length === 0) {
      statusEl.innerHTML = '<p class="empty">未找到合适的诗词</p>';
      return;
    }
    
    // 随机选择一首未使用的诗词
    let selectedPoem;
    let attempts = 0;
    const maxAttempts = 10;
    
    do {
      const randomIndex = Math.floor(Math.random() * validPoems.length);
      selectedPoem = validPoems[randomIndex];
      attempts++;
    } while (solitaireGameState.usedPoems.has(selectedPoem.title) && attempts < maxAttempts);
    
    // 如果所有诗词都已使用，重置使用记录
    if (attempts >= maxAttempts) {
      solitaireGameState.usedPoems.clear();
      const randomIndex = Math.floor(Math.random() * validPoems.length);
      selectedPoem = validPoems[randomIndex];
    }
    
    // 添加到已使用集合
    solitaireGameState.usedPoems.add(selectedPoem.title);
    solitaireGameState.currentPoem = selectedPoem;
    
    // 分割诗词为行
    const lines = formatContent(selectedPoem.content).split('\n').filter(line => line.trim());
    
    // 随机选择一个位置（不是第一句或最后一句，除非诗词只有2句）
    let index;
    if (lines.length === 2) {
      index = 0;
    } else {
      index = Math.floor(Math.random() * (lines.length - 1));
    }
    
    solitaireGameState.currentIndex = index;
    
    // 生成题目
    if (solitaireGameState.mode === 'next') {
      // 上句接下句
      statusEl.innerHTML = `<h4>诗词接龙 - 上句接下句</h4><p>请接出下一句：</p>`;
      questionEl.innerHTML = `
        <div class="question-poem">
          <div class="poem-meta">${escapeHtml(selectedPoem.author)} · 《${escapeHtml(selectedPoem.title)}》</div>
          <div class="poem-line">${escapeHtml(lines[index])}</div>
          <div class="poem-line blank">__________</div>
        </div>
      `;
    } else {
      // 下句猜上句
      statusEl.innerHTML = `<h4>诗词接龙 - 下句猜上句</h4><p>请猜出上一句：</p>`;
      questionEl.innerHTML = `
        <div class="question-poem">
          <div class="poem-meta">${escapeHtml(selectedPoem.author)} · 《${escapeHtml(selectedPoem.title)}》</div>
          <div class="poem-line blank">__________</div>
          <div class="poem-line">${escapeHtml(lines[index + 1])}</div>
        </div>
      `;
    }
    
    // 清空输入框
    document.getElementById('solitaire-input').value = '';
    
    // 更新分数
    scoreEl.textContent = `得分：${solitaireGameState.score}`;
    
  } catch (error) {
    console.error('加载诗词失败:', error);
    statusEl.innerHTML = '<p class="empty">加载失败</p>';
  }
}

// 提交诗词接龙答案
function submitSolitaireAnswer() {
  const userInput = document.getElementById('solitaire-input').value.trim();
  const statusEl = document.getElementById('solitaire-status');
  const questionEl = document.getElementById('solitaire-question');
  
  if (!userInput) {
    showAlert('请输入答案');
    return;
  }
  
  const poem = solitaireGameState.currentPoem;
  const lines = formatContent(poem.content).split('\n').filter(line => line.trim());
  const index = solitaireGameState.currentIndex;
  
  let correctAnswer;
  if (solitaireGameState.mode === 'next') {
    correctAnswer = lines[index + 1];
  } else {
    correctAnswer = lines[index];
  }
  
  // 比较答案（忽略标点符号）
  const userAnswerNoPunc = removePunctuation(userInput);
  const correctAnswerNoPunc = removePunctuation(correctAnswer);
  
  if (userAnswerNoPunc === correctAnswerNoPunc) {
    // 答案正确
    solitaireGameState.score += 10;
    showAlert('回答正确！加10分');
    setTimeout(startSolitaireGame, 1000);
  } else {
    // 答案错误
    showAlert(`回答错误！正确答案是：${correctAnswer}`);
    // 显示正确答案后继续游戏
    setTimeout(startSolitaireGame, 1000);
  }
}

// 显示诗词接龙提示
function showSolitaireHint() {
  const poem = solitaireGameState.currentPoem;
  const lines = formatContent(poem.content).split('\n').filter(line => line.trim());
  const index = solitaireGameState.currentIndex;
  
  let correctAnswer;
  if (solitaireGameState.mode === 'next') {
    correctAnswer = lines[index + 1];
  } else {
    correctAnswer = lines[index];
  }
  
  // 显示提示（显示前两个字）
  const hint = correctAnswer.substring(0, 2) + '...';
  showAlert(`提示：${hint}`);
}

// 页面加载时初始化诗词接龙
window.addEventListener('load', () => {
  // 初始化诗词接龙游戏
  const solitaireSection = document.getElementById('solitaire');
  if (solitaireSection) {
    initSolitaire();
  }
});

// 诗词详情弹窗
async function showPoemDetail(title) {
  const res = await fetch(`${API_BASE}/api/poems/detail?title=${encodeURIComponent(title)}`);
  const json = await res.json();
  const modal = document.getElementById('poem-modal');
  const detail = document.getElementById('poem-detail');

  if (!json.success || !json.data) {
    detail.innerHTML = '<p>未找到该诗词</p>';
  } else {
    const p = json.data;
    
    // 生成诗词介绍（300字左右）
    let introduction = `《${p.title}》是${p.dynasty}诗人${p.author}的代表作之一。`;
    if (p.background) {
      introduction += ` ${p.background}`;
    } else {
      introduction += ` 此诗创作于${p.dynasty}时期，是诗人的经典作品。`;
    }
    if (p.emotion) {
      introduction += ` ${p.emotion}`;
    } else {
      introduction += ` 诗中表达了诗人对生活的感悟和对美好事物的向往。`;
    }
    if (p.allusion) {
      introduction += ` ${p.allusion}`;
    } else {
      introduction += ` 诗中运用了丰富的意象，展现了中国古典诗歌的独特魅力。`;
    }
    
    // 确保介绍长度在300字左右
    if (introduction.length < 200) {
      introduction += ` 全诗语言凝练，意境深远，结构严谨，韵律和谐，千百年来一直为人们所传诵，成为中国古典文学的经典之作。`;
    }
    if (introduction.length > 400) {
      introduction = introduction.substring(0, 350) + '...';
    }
    
    // 关键名词列表（扩展版）
    const keyTerms = [
      { term: '明月', explanation: '明月是中国古典诗歌中常见的意象，常象征团圆、思乡之情。' },
      { term: '夕阳', explanation: '夕阳常象征时光流逝、人生迟暮，也可表达离别的伤感。' },
      { term: '春风', explanation: '春风象征生机、希望和温暖，常用来表现春天的到来。' },
      { term: '秋霜', explanation: '秋霜象征寒冷、孤寂，常用来表达悲伤或高洁的品质。' },
      { term: '孤帆', explanation: '孤帆象征孤独、漂泊，常用来表达离别的伤感或游子的思乡之情。' },
      { term: '归雁', explanation: '归雁象征思乡、归期，常用来表达对家乡的思念。' },
      { term: '落花', explanation: '落花象征美好事物的消逝，常用来表达伤春之情。' },
      { term: '流水', explanation: '流水象征时光流逝、生命短暂，也可表达绵绵不绝的情感。' },
      { term: '青山', explanation: '青山常象征坚韧、永恒，也可表达归隐之情。' },
      { term: '白云', explanation: '白云象征自由、高洁，常用来表达超脱世俗的情怀。' },
      { term: '芳草', explanation: '芳草常象征离情别绪，也可表达春天的生机。' },
      { term: '梧桐', explanation: '梧桐常象征孤独、悲伤，尤其在秋雨梧桐的场景中。' },
      { term: '杜鹃', explanation: '杜鹃鸟的啼声常象征哀怨、思归之情。' },
      { term: '菊花', explanation: '菊花象征高洁、隐逸，常用来表达诗人的品格。' },
      { term: '梅花', explanation: '梅花象征坚韧、高洁，常用来表达诗人的品格。' },
      { term: '竹子', explanation: '竹子象征高洁、坚韧，常用来表达诗人的品格。' },
      { term: '莲花', explanation: '莲花象征高洁、纯净，常用来表达诗人的品格。' },
      { term: '饮酒', explanation: '饮酒常用来表达诗人的洒脱、豪放之情。' },
      { term: '登高', explanation: '登高常用来表达诗人的壮志豪情或思乡之情。' },
      { term: '望远', explanation: '望远常用来表达诗人的思乡之情或对未来的期待。' }
    ];
    
    // 高亮诗词中的关键名词
    let highlightedContent = escapeHtml(formatContent(p.content));
    let highlightedCount = 0;
    
    keyTerms.forEach(item => {
      if (highlightedContent.includes(item.term)) {
        const regex = new RegExp(`(${item.term})`, 'g');
        highlightedContent = highlightedContent.replace(regex, `<span class="key-term" data-term="${item.term}" data-explanation="${escapeHtml(item.explanation)}">$1</span>`);
        highlightedCount++;
      }
    });
    
    // 如果高亮的关键词少于3个，添加一些通用的关键词
    if (highlightedCount < 3) {
      // 检查是否有其他常见的关键词
      const commonTerms = [
        { term: '人生', explanation: '人生是诗歌中常见的主题，常用来表达对生命意义的思考。' },
        { term: '青春', explanation: '青春象征美好时光，常用来表达对时光流逝的感慨。' },
        { term: '故乡', explanation: '故乡是诗歌中常见的主题，常用来表达思乡之情。' },
        { term: '友情', explanation: '友情是诗歌中常见的主题，常用来表达朋友之间的深厚感情。' },
        { term: '爱情', explanation: '爱情是诗歌中常见的主题，常用来表达男女之间的深情。' }
      ];
      
      commonTerms.forEach(item => {
        if (highlightedContent.includes(item.term) && highlightedCount < 3) {
          const regex = new RegExp(`(${item.term})`, 'g');
          highlightedContent = highlightedContent.replace(regex, `<span class="key-term" data-term="${item.term}" data-explanation="${escapeHtml(item.explanation)}">$1</span>`);
          highlightedCount++;
        }
      });
    }
    
    // 生成数据来源标识
    const dataSourceBadge = (p.dataSource === 1) 
      ? '<span class="data-source-badge ai">AI生成</span>' 
      : '<span class="data-source-badge database">原始数据</span>';
    
    let html = `
      <div class="detail-title">《${escapeHtml(p.title)}》</div>
      <div class="detail-meta">
        <span>${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</span>
        ${dataSourceBadge}
      </div>
      <div class="detail-section">
        <h4>诗词介绍</h4>
        <p>${escapeHtml(introduction)}</p>
      </div>
      <div class="detail-section">
        <h4>原文</h4>
        <div class="detail-content">${highlightedContent}</div>
        <p style="font-size: 0.9rem; color: #666; margin-top: 0.5rem;">注：点击诗中高亮的关键名词查看解释</p>
      </div>
    `;
    if (p.translation) {
      html += `<div class="detail-section"><h4>译文</h4><p>${escapeHtml(p.translation)}</p></div>`;
    }
    if (p.background) {
      html += `<div class="detail-section"><h4>创作背景</h4><p>${escapeHtml(p.background)}</p></div>`;
    }
    if (p.emotion) {
      html += `<div class="detail-section"><h4>情感主旨</h4><p>${escapeHtml(p.emotion)}</p></div>`;
    }
    if (p.allusion) {
      html += `<div class="detail-section"><h4>典故意象</h4><p>${escapeHtml(p.allusion)}</p></div>`;
    }
    
    // 添加背诵默写功能
    html += `
      <div class="detail-section">
        <h4>背诵默写</h4>
        <div class="recite-buttons" style="display: flex; gap: 1rem; margin-top: 1rem;">
          <button class="btn btn-primary" id="btn-recite-this" data-title="${escapeHtml(p.title)}" data-author="${escapeHtml(p.author)}" data-dynasty="${escapeHtml(p.dynasty)}" data-content="${escapeHtml(p.content)}">整首默写</button>
          <button class="btn btn-secondary" id="btn-dictation-this" data-title="${escapeHtml(p.title)}" data-author="${escapeHtml(p.author)}" data-dynasty="${escapeHtml(p.dynasty)}" data-content="${escapeHtml(p.content)}">填空默写</button>
        </div>
        <div class="recite-result" id="recite-result-this" style="margin-top: 1rem; display: none;"></div>
      </div>
    `;
    detail.innerHTML = html;
    
    // 添加关键名词点击事件
    setTimeout(() => {
      document.querySelectorAll('.key-term').forEach(term => {
        term.addEventListener('click', (e) => {
          e.stopPropagation();
          const termText = term.dataset.term;
          const explanation = term.dataset.explanation;
          showTermExplanation(termText, explanation);
        });
      });
      
      // 添加背诵默写按钮事件
      const btnReciteThis = document.getElementById('btn-recite-this');
      const btnDictationThis = document.getElementById('btn-dictation-this');
      const reciteResultThis = document.getElementById('recite-result-this');
      
      if (btnReciteThis) {
        btnReciteThis.addEventListener('click', () => {
          const poemData = {
            title: btnReciteThis.dataset.title,
            author: btnReciteThis.dataset.author,
            dynasty: btnReciteThis.dataset.dynasty,
            content: btnReciteThis.dataset.content
          };
          startReciteThis(poemData, reciteResultThis);
        });
      }
      
      if (btnDictationThis) {
        btnDictationThis.addEventListener('click', () => {
          const poemData = {
            title: btnDictationThis.dataset.title,
            author: btnDictationThis.dataset.author,
            dynasty: btnDictationThis.dataset.dynasty,
            content: btnDictationThis.dataset.content
          };
          startDictationThis(poemData, reciteResultThis);
        });
      }
    }, 100);
  }
  modal.classList.add('active');
}

// 显示名词解释弹窗
function showTermExplanation(term, explanation) {
  // 检查是否已有解释弹窗
  let modal = document.getElementById('term-modal');
  let termTitle, termExplanation, termModalClose;
  
  if (!modal) {
    // 创建解释弹窗
    modal = document.createElement('div');
    modal.id = 'term-modal';
    modal.className = 'modal';
    modal.style.zIndex = '1001';
    modal.innerHTML = `
      <div class="modal-content" style="max-width: 400px;">
        <button class="modal-close" id="term-modal-close">×</button>
        <div class="term-detail">
          <h4 id="term-title"></h4>
          <p id="term-explanation"></p>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
    
    // 获取弹窗内的元素
    termTitle = modal.querySelector('#term-title');
    termExplanation = modal.querySelector('#term-explanation');
    termModalClose = modal.querySelector('#term-modal-close');
    
    // 存储元素引用
    modal._elements = {
      termTitle: termTitle,
      termExplanation: termExplanation,
      termModalClose: termModalClose
    };
    
    // 添加关闭事件
    termModalClose.addEventListener('click', () => {
      modal.classList.remove('active');
    });
    
    modal.addEventListener('click', (e) => {
      if (e.target.id === 'term-modal') {
        modal.classList.remove('active');
      }
    });
  } else {
    // 从存储的引用中获取元素
    termTitle = modal._elements.termTitle;
    termExplanation = modal._elements.termExplanation;
  }
  
  // 填充内容
  termTitle.textContent = term;
  termExplanation.textContent = explanation;
  
  // 显示弹窗
  modal.classList.add('active');
}

// 显示诗人详情弹窗
async function showPoetDetail(author) {
  const modal = document.getElementById('poet-modal');
  const detail = document.getElementById('poet-detail');
  
  // 显示加载状态
  detail.innerHTML = '<p class="empty">加载中...</p>';
  modal.classList.add('active');
  
  try {
    // 获取诗人的作品
    const poemsRes = await fetch(`${API_BASE}/api/poems/search?author=${encodeURIComponent(author)}`);
    const poemsJson = await poemsRes.json();
    
    if (!poemsJson.success || !poemsJson.data || poemsJson.data.length === 0) {
      detail.innerHTML = '<p>未找到该诗人的信息</p>';
      return;
    }
    
    const poems = poemsJson.data;
    const poetInfo = poems[0];
    const dynasty = poetInfo.dynasty || '未知朝代';
    const poemCount = poems.length;
    
    // 生成诗人介绍
    let poetBio = `${author}（约${dynasty}时期），中国古代著名诗人。`;
    poetBio += ` 其作品题材广泛，风格独特，在中国文学史上占有重要地位。`;
    poetBio += ` 一生创作了大量诗词，题材涵盖山水、田园、边塞、思乡等多个方面，`;
    poetBio += ` 其作品语言凝练，意境深远，对后世文学产生了深远影响。`;
    
    // 生成诗人详情HTML
    let html = `
      <div class="poet-header">
        <h2 class="poet-name">${escapeHtml(author)}</h2>
        <p class="poet-meta">${escapeHtml(dynasty)} · 共 ${poemCount} 首诗词</p>
        <div class="poet-bio">${escapeHtml(poetBio)}</div>
      </div>
      
      <div class="tab-buttons">
        <button class="tab-btn active" data-tab="works">代表作品</button>
        <button class="tab-btn" data-tab="graph">知识图谱</button>
        <button class="tab-btn" data-tab="trajectory">诗人行迹</button>
      </div>
      
      <div class="tab-content active" id="tab-works">
        <h3 class="section-title">代表作品</h3>
        <div class="poet-works">
          ${poems.slice(0, 6).map(p => `
            <div class="work-item" data-title="${escapeHtml(p.title)}" data-author="${escapeHtml(author)}">
              <div class="work-title">《${escapeHtml(p.title)}》</div>
              <div class="work-preview">${escapeHtml((p.content || '').slice(0, 100))}...</div>
            </div>
          `).join('')}
        </div>
      </div>
      
      <div class="tab-content" id="tab-graph">
        <h3 class="section-title">知识图谱</h3>
        <div class="chart-container" id="poet-knowledge-graph"></div>
      </div>
      
      <div class="tab-content" id="tab-trajectory">
        <h3 class="section-title">诗人行迹</h3>
        <div class="chart-container" id="poet-trajectory-chart"></div>
      </div>
    `;
    
    detail.innerHTML = html;
    
    // 添加标签页切换事件
    detail.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        detail.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        detail.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        const tab = btn.dataset.tab;
        document.getElementById(`tab-${tab}`).classList.add('active');
        
        // 加载知识图谱
        if (tab === 'graph') {
          setTimeout(() => loadPoetKnowledgeGraph(author), 100);
        }
        
        // 加载诗人行迹
        if (tab === 'trajectory') {
          setTimeout(() => loadPoetTrajectory(author), 100);
        }
      });
    });
    
    // 添加作品点击事件
    detail.querySelectorAll('.work-item').forEach(item => {
      item.addEventListener('click', () => {
        const title = item.dataset.title;
        const authorName = item.dataset.author;
        // 关闭诗人弹窗
        modal.classList.remove('active');
        // 跳转到诗词页面并传递诗人信息
        showPoemDetailWithBack(title, authorName);
      });
    });
    
  } catch (error) {
    console.error('加载诗人详情失败:', error);
    detail.innerHTML = '<p>加载失败，请稍后重试</p>';
  }
}

// 显示诗词详情弹窗（带返回按钮）
async function showPoemDetailWithBack(title, author) {
  const res = await fetch(`${API_BASE}/api/poems/detail?title=${encodeURIComponent(title)}`);
  const json = await res.json();
  const modal = document.getElementById('poem-modal');
  const detail = document.getElementById('poem-detail');

  if (!json.success || !json.data) {
    detail.innerHTML = '<p>未找到该诗词</p>';
  } else {
    const p = json.data;
    
    // 生成诗词介绍（300字左右）
    let introduction = `《${p.title}》是${p.dynasty}诗人${p.author}的代表作之一。`;
    if (p.background) {
      introduction += ` ${p.background}`;
    } else {
      introduction += ` 此诗创作于${p.dynasty}时期，是诗人的经典作品。`;
    }
    if (p.emotion) {
      introduction += ` ${p.emotion}`;
    } else {
      introduction += ` 诗中表达了诗人对生活的感悟和对美好事物的向往。`;
    }
    if (p.allusion) {
      introduction += ` ${p.allusion}`;
    } else {
      introduction += ` 诗中运用了丰富的意象，展现了中国古典诗歌的独特魅力。`;
    }
    
    // 确保介绍长度在300字左右
    if (introduction.length < 200) {
      introduction += ` 全诗语言凝练，意境深远，结构严谨，韵律和谐，千百年来一直为人们所传诵，成为中国古典文学的经典之作。`;
    }
    if (introduction.length > 400) {
      introduction = introduction.substring(0, 350) + '...';
    }
    
    // 关键名词列表（扩展版）
    const keyTerms = [
      { term: '明月', explanation: '明月是中国古典诗歌中常见的意象，常象征团圆、思乡之情。' },
      { term: '夕阳', explanation: '夕阳常象征时光流逝、人生迟暮，也可表达离别的伤感。' },
      { term: '春风', explanation: '春风象征生机、希望和温暖，常用来表现春天的到来。' },
      { term: '秋霜', explanation: '秋霜象征寒冷、孤寂，常用来表达悲伤或高洁的品质。' },
      { term: '孤帆', explanation: '孤帆象征孤独、漂泊，常用来表达离别的伤感或游子的思乡之情。' },
      { term: '归雁', explanation: '归雁象征思乡、归期，常用来表达对家乡的思念。' },
      { term: '落花', explanation: '落花象征美好事物的消逝，常用来表达伤春之情。' },
      { term: '流水', explanation: '流水象征时光流逝、生命短暂，也可表达绵绵不绝的情感。' },
      { term: '青山', explanation: '青山常象征坚韧、永恒，也可表达归隐之情。' },
      { term: '白云', explanation: '白云象征自由、高洁，常用来表达超脱世俗的情怀。' },
      { term: '芳草', explanation: '芳草常象征离情别绪，也可表达春天的生机。' },
      { term: '梧桐', explanation: '梧桐常象征孤独、悲伤，尤其在秋雨梧桐的场景中。' },
      { term: '杜鹃', explanation: '杜鹃鸟的啼声常象征哀怨、思归之情。' },
      { term: '菊花', explanation: '菊花象征高洁、隐逸，常用来表达诗人的品格。' },
      { term: '梅花', explanation: '梅花象征坚韧、高洁，常用来表达诗人的品格。' },
      { term: '竹子', explanation: '竹子象征高洁、坚韧，常用来表达诗人的品格。' },
      { term: '莲花', explanation: '莲花象征高洁、纯净，常用来表达诗人的品格。' },
      { term: '饮酒', explanation: '饮酒常用来表达诗人的洒脱、豪放之情。' },
      { term: '登高', explanation: '登高常用来表达诗人的壮志豪情或思乡之情。' },
      { term: '望远', explanation: '望远常用来表达诗人的思乡之情或对未来的期待。' }
    ];
    
    // 高亮诗词中的关键名词
    let highlightedContent = escapeHtml(formatContent(p.content));
    let highlightedCount = 0;
    
    keyTerms.forEach(item => {
      if (highlightedContent.includes(item.term)) {
        const regex = new RegExp(`(${item.term})`, 'g');
        highlightedContent = highlightedContent.replace(regex, `<span class="key-term" data-term="${item.term}" data-explanation="${escapeHtml(item.explanation)}">$1</span>`);
        highlightedCount++;
      }
    });
    
    // 如果高亮的关键词少于3个，添加一些通用的关键词
    if (highlightedCount < 3) {
      // 检查是否有其他常见的关键词
      const commonTerms = [
        { term: '人生', explanation: '人生是诗歌中常见的主题，常用来表达对生命意义的思考。' },
        { term: '青春', explanation: '青春象征美好时光，常用来表达对时光流逝的感慨。' },
        { term: '故乡', explanation: '故乡是诗歌中常见的主题，常用来表达思乡之情。' },
        { term: '友情', explanation: '友情是诗歌中常见的主题，常用来表达朋友之间的深厚感情。' },
        { term: '爱情', explanation: '爱情是诗歌中常见的主题，常用来表达男女之间的深情。' }
      ];
      
      commonTerms.forEach(item => {
        if (highlightedContent.includes(item.term) && highlightedCount < 3) {
          const regex = new RegExp(`(${item.term})`, 'g');
          highlightedContent = highlightedContent.replace(regex, `<span class="key-term" data-term="${item.term}" data-explanation="${escapeHtml(item.explanation)}">$1</span>`);
          highlightedCount++;
        }
      });
    }
    
    // 生成数据来源标识
    const dataSourceBadge = (p.dataSource === 1) 
      ? '<span class="data-source-badge ai">AI生成</span>' 
      : '<span class="data-source-badge database">原始数据</span>';
    
    let html = `
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <h3 class="detail-title">《${escapeHtml(p.title)}》</h3>
        <button class="btn btn-secondary" id="back-to-poet" data-author="${escapeHtml(author)}">返回诗人页</button>
      </div>
      <div class="detail-meta">
        <span>${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</span>
        ${dataSourceBadge}
      </div>
      <div class="detail-section">
        <h4>诗词介绍</h4>
        <p>${escapeHtml(introduction)}</p>
      </div>
      <div class="detail-section">
        <h4>原文</h4>
        <div class="detail-content">${highlightedContent}</div>
        <p style="font-size: 0.9rem; color: #666; margin-top: 0.5rem;">注：点击诗中高亮的关键名词查看解释</p>
      </div>
    `;
    if (p.translation) {
      html += `<div class="detail-section"><h4>译文</h4><p>${escapeHtml(p.translation)}</p></div>`;
    }
    if (p.background) {
      html += `<div class="detail-section"><h4>创作背景</h4><p>${escapeHtml(p.background)}</p></div>`;
    }
    if (p.emotion) {
      html += `<div class="detail-section"><h4>情感主旨</h4><p>${escapeHtml(p.emotion)}</p></div>`;
    }
    if (p.allusion) {
      html += `<div class="detail-section"><h4>典故意象</h4><p>${escapeHtml(p.allusion)}</p></div>`;
    }
    
    // 添加背诵默写功能
    html += `
      <div class="detail-section">
        <h4>背诵默写</h4>
        <div class="recite-buttons" style="display: flex; gap: 1rem; margin-top: 1rem;">
          <button class="btn btn-primary" id="btn-recite-this" data-title="${escapeHtml(p.title)}" data-author="${escapeHtml(p.author)}" data-dynasty="${escapeHtml(p.dynasty)}" data-content="${escapeHtml(p.content)}">整首默写</button>
          <button class="btn btn-secondary" id="btn-dictation-this" data-title="${escapeHtml(p.title)}" data-author="${escapeHtml(p.author)}" data-dynasty="${escapeHtml(p.dynasty)}" data-content="${escapeHtml(p.content)}">填空默写</button>
        </div>
        <div class="recite-result" id="recite-result-this" style="margin-top: 1rem; display: none;"></div>
      </div>
    `;
    detail.innerHTML = html;
    
    // 添加关键名词点击事件
    setTimeout(() => {
      document.querySelectorAll('.key-term').forEach(term => {
        term.addEventListener('click', (e) => {
          e.stopPropagation();
          const termText = term.dataset.term;
          const explanation = term.dataset.explanation;
          showTermExplanation(termText, explanation);
        });
      });
      
      // 添加背诵默写按钮事件
      const btnReciteThis = document.getElementById('btn-recite-this');
      const btnDictationThis = document.getElementById('btn-dictation-this');
      const reciteResultThis = document.getElementById('recite-result-this');
      
      if (btnReciteThis) {
        btnReciteThis.addEventListener('click', () => {
          const poemData = {
            title: btnReciteThis.dataset.title,
            author: btnReciteThis.dataset.author,
            dynasty: btnReciteThis.dataset.dynasty,
            content: btnReciteThis.dataset.content
          };
          startReciteThis(poemData, reciteResultThis);
        });
      }
      
      if (btnDictationThis) {
        btnDictationThis.addEventListener('click', () => {
          const poemData = {
            title: btnDictationThis.dataset.title,
            author: btnDictationThis.dataset.author,
            dynasty: btnDictationThis.dataset.dynasty,
            content: btnDictationThis.dataset.content
          };
          startDictationThis(poemData, reciteResultThis);
        });
      }
    }, 100);
    
    // 添加返回按钮事件
    setTimeout(() => {
      const backBtn = document.getElementById('back-to-poet');
      if (backBtn) {
        backBtn.addEventListener('click', () => {
          modal.classList.remove('active');
          showPoetDetail(backBtn.dataset.author);
        });
      }
    }, 100);
  }
  modal.classList.add('active');
}

// 加载诗人知识图谱
function loadPoetKnowledgeGraph(author) {
  const chartDom = document.getElementById('poet-knowledge-graph');
  if (!chartDom) return;
  
  const myChart = echarts.init(chartDom);
  chartDom.innerHTML = '<p class="empty">图谱加载中...</p>';
  
  fetch(`${API_BASE}/api/graph/subgraph?type=poet&name=${encodeURIComponent(author)}`)
    .then(res => res.json())
    .then(json => {
      chartDom.innerHTML = '';
      if (!json.success || !json.data || !json.data.nodes || json.data.nodes.length === 0) {
        chartDom.innerHTML = '<p class="empty">暂无知识图谱数据</p>';
        return;
      }
      
      const graphData = json.data;
      const categories = graphData.categories || [];
      
      const categoriesOption = categories.map(c => ({ name: c.value }));
      
      const nodes = graphData.nodes.map(n => ({
        name: n.name,
        category: n.category,
        symbolSize: n.symbolSize || 25,
        itemStyle: { color: getCategoryColor(n.category, categories.length) }
      }));
      
      const links = (graphData.links || []).map(e => ({
        source: e.source,
        target: e.target,
        label: { show: true, formatter: e.relation || '' }
      }));
      
      const option = {
        title: { text: `${author} 知识图谱`, left: 'center', textStyle: { fontSize: 16 } },
        tooltip: {
          formatter: function(params) {
            if (params.dataType === 'node') {
              const cat = categoriesOption[params.data.category]?.name || '未知';
              return `<strong>${params.name}</strong><br/>类型：${cat}`;
            } else if (params.dataType === 'edge') {
              return `${params.data.source} → ${params.data.target}<br/>关系：${params.data.label?.formatter || ''}`;
            }
            return params.name;
          }
        },
        legend: [{ data: categoriesOption.map(c => c.name), bottom: 10 }],
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [{
          type: 'graph',
          layout: 'force',
          force: { repulsion: 200, edgeLength: 100, gravity: 0.1 },
          roam: true,
          label: { show: true, fontSize: 12 },
          edgeLabel: { fontSize: 10 },
          categories: categoriesOption,
          data: nodes,
          links: links,
          lineStyle: { color: 'source', curveness: 0.3 }
        }]
      };
      
      myChart.setOption(option, true);
      window.addEventListener('resize', () => myChart.resize());
    })
    .catch(err => {
      console.error('加载知识图谱失败:', err);
      chartDom.innerHTML = '<p class="empty">知识图谱加载失败，请稍后重试</p>';
    });
}

function getCategoryColor(categoryIndex, totalCategories) {
  const colors = ['#c23531', '#2f4554', '#61a0a8', '#d48265', '#91c7ae', '#749f83', '#ca8622', '#bda29a'];
  return colors[categoryIndex % colors.length];
}

// 加载诗人行迹
function loadPoetTrajectory(author) {
  const chartDom = document.getElementById('poet-trajectory-chart');
  if (!chartDom) return;
  
  const myChart = echarts.init(chartDom);
  chartDom.innerHTML = '<p class="empty">行迹数据加载中...</p>';
  
  fetch(`${API_BASE}/api/poet/trajectory?author=${encodeURIComponent(author)}`)
    .then(res => res.json())
    .then(json => {
      chartDom.innerHTML = '';
      if (!json.success || !json.data || !json.data.poetTagDist) {
        chartDom.innerHTML = '<p class="empty">暂无诗人行迹数据</p>';
        return;
      }
      
      const poetTag = json.data.poetTagDist;
      const tagDist = poetTag.tagDist || [];
      
      const names = tagDist.map(t => t.name);
      const values = tagDist.map(t => t.value);
      
      const option = {
        title: {
          text: `${author} · 创作题材分布`,
          subtext: `${poetTag.dynasty || '未知朝代'} · 共 ${poetTag.total || 0} 首`,
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            const p = params[0];
            return `${p.name}<br/>作品数量：${p.value} 首<br/>占比：${((p.value / poetTag.total) * 100).toFixed(1)}%`;
          }
        },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: names,
          axisLabel: { rotate: 30, interval: 0 },
          axisTick: { alignWithLabel: true }
        },
        yAxis: { type: 'value', name: '作品数量' },
        series: [{
          name: '作品数量',
          type: 'bar',
          data: values,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          label: { show: true, position: 'top' }
        }]
      };
      
      myChart.setOption(option, true);
      window.addEventListener('resize', () => myChart.resize());
    })
    .catch(err => {
      console.error('加载诗人行迹失败:', err);
      chartDom.innerHTML = '<p class="empty">行迹数据加载失败，请稍后重试</p>';
    });
}
window.showPoemDetail = showPoemDetail;

document.getElementById('modal-close').addEventListener('click', () => {
  document.getElementById('poem-modal').classList.remove('active');
});

document.getElementById('poem-modal').addEventListener('click', (e) => {
  if (e.target.id === 'poem-modal') e.target.classList.remove('active');
});

// 诗人弹窗关闭事件
document.getElementById('poet-modal-close').addEventListener('click', () => {
  document.getElementById('poet-modal').classList.remove('active');
});

document.getElementById('poet-modal').addEventListener('click', (e) => {
  if (e.target.id === 'poet-modal') e.target.classList.remove('active');
});

// 回到顶部按钮功能
function initBackToTop() {
  const backToTopBtn = document.getElementById('back-to-top');
  if (!backToTopBtn) return;
  
  // 监听页面滚动事件
  window.addEventListener('scroll', () => {
    if (window.pageYOffset > 300) {
      backToTopBtn.classList.add('active');
    } else {
      backToTopBtn.classList.remove('active');
    }
  });
  
  // 点击按钮回到顶部
  backToTopBtn.addEventListener('click', () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
}

// 工具函数
function escapeHtml(s) {
  if (!s) return '';
  const div = document.createElement('div');
  div.textContent = s;
  return div.innerHTML;
}

function formatContent(content) {
  if (!content) return '';
  return content
    .replace(/。/g, '。\n')
    .replace(/，/g, '，\n')
    .replace(/？/g, '？\n')
    .replace(/！/g, '！\n')
    .replace(/；/g, '；\n');
}

// 开始整首默写（针对当前诗词）
function startReciteThis(poemData, resultContainer) {
  // 显示默写界面
  resultContainer.style.display = 'block';
  
  // 按行分割内容
  const lines = formatContent(poemData.content).split('\n').filter(line => line.trim());
  let inputHtml = '';
  
  // 为每行创建一个输入框
  lines.forEach((line, index) => {
    const inputId = `recite-line-${Date.now()}-${index}`;
    inputHtml += `
      <div style="margin-bottom: 0.8rem;">
        <input type="text" class="dictation-line-input" id="${inputId}" placeholder="请输入第${index + 1}行" style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 4px; font-family: 'Noto Serif SC', serif; font-size: 1rem;">
      </div>
    `;
  });

  resultContainer.innerHTML = `
    <div style="background: #f9f9f9; padding: 1.5rem; border-radius: 8px;">
      <h4 style="margin-bottom: 1rem;">默写：《${escapeHtml(poemData.title)}》</h4>
      <div class="poem-meta" style="margin-bottom: 1.5rem;">${escapeHtml(poemData.author)} · ${escapeHtml(poemData.dynasty)}</div>
      <div style="margin-bottom: 1.5rem;">
        ${inputHtml}
      </div>
      <button class="btn btn-primary" id="btn-submit-recite-this" data-title="${escapeHtml(poemData.title)}" data-content="${escapeHtml(poemData.content)}">提交答案</button>
    </div>
  `;
  
  // 添加提交按钮事件
  const submitBtn = document.getElementById('btn-submit-recite-this');
  if (submitBtn) {
    submitBtn.addEventListener('click', () => {
      const originalContent = submitBtn.dataset.content;
      submitReciteThis(resultContainer, originalContent);
    });
  }
}

// 提交整首默写答案
function submitReciteThis(container, originalContent) {
  // 获取用户输入
  const inputs = container.querySelectorAll('.dictation-line-input');
  const userLines = Array.from(inputs).map(input => input.value.trim());
  
  // 按行分割原始内容
  const originalLines = formatContent(originalContent).split('\n').filter(line => line.trim());
  
  // 提取原始内容中的所有汉字（不包括标点和换行）
  const originalChars = originalContent.split('');
  const originalWords = [];
  
  for (let i = 0; i < originalChars.length; i++) {
    if (!originalChars[i].match(/[，。；：？！\n]/)) {
      originalWords.push(originalChars[i]);
    }
  }
  
  // 提取用户输入中的所有汉字
  let userWords = [];
  userLines.forEach(line => {
    const lineChars = line.split('');
    lineChars.forEach(char => {
      if (!char.match(/[，。；：？！\n]/)) {
        userWords.push(char);
      }
    });
  });
  
  // 生成对比结果
  let comparisonHtml = '';
  let inputIndex = 0;
  
  for (let i = 0; i < originalChars.length; i++) {
    const char = originalChars[i];
    if (char.match(/[，。；：？！\n]/)) {
      comparisonHtml += char;
    } else {
      const userAnswer = userWords[inputIndex] || '';
      const isCorrect = userAnswer === originalWords[inputIndex];
      comparisonHtml += isCorrect 
        ? `<span style="color: green; font-weight: bold;">${escapeHtml(userAnswer || '_')}</span>`
        : `<span style="color: red; font-weight: bold;">${escapeHtml(userAnswer || '_')}</span>`;
      inputIndex++;
    }
  }
  
  // 计算正确率
  let correctCount = 0;
  for (let i = 0; i < Math.min(userWords.length, originalWords.length); i++) {
    if (userWords[i] === originalWords[i]) {
      correctCount++;
    }
  }
  
  // 显示结果
  container.innerHTML = `
    <div style="background: #f9f9f9; padding: 1.5rem; border-radius: 8px;">
      <h4 style="margin-bottom: 1rem;">默写结果</h4>
      <p style="margin-bottom: 1rem;">答对 ${correctCount} 题，共 ${originalWords.length} 题</p>
      <p style="margin-bottom: 0.5rem; font-weight: 600;">您的答案：</p>
      <div class="poem-text" style="margin-bottom: 1.5rem;">${comparisonHtml}</div>
      <p style="margin-bottom: 0.5rem; font-weight: 600;">参考答案：</p>
      <div class="poem-text" style="margin-bottom: 1.5rem;">${escapeHtml(formatContent(originalContent))}</div>
      <button class="btn btn-secondary" id="btn-reset-recite-this">重新默写</button>
    </div>
  `;
  
  // 添加重新默写按钮事件
  const resetBtn = document.getElementById('btn-reset-recite-this');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      container.style.display = 'none';
    });
  }
}

// 开始填空默写（针对当前诗词）
function startDictationThis(poemData, resultContainer) {
  // 显示默写界面
  resultContainer.style.display = 'block';
  
  // 生成带空白的内容
  const content = poemData.content;
  let contentBlank = content;
  
  // 随机选择一些位置替换为空白
  const chars = content.split('');
  const blankPositions = [];
  
  // 计算要替换的字符数量（约30%）
  let charCount = 0;
  for (let i = 0; i < chars.length; i++) {
    if (!chars[i].match(/[，。；：？！\n]/)) {
      charCount++;
    }
  }
  
  const blankCount = Math.max(2, Math.floor(charCount * 0.3));
  
  // 随机选择位置
  while (blankPositions.length < blankCount) {
    const pos = Math.floor(Math.random() * chars.length);
    if (!chars[pos].match(/[，。；：？！\n]/) && !blankPositions.includes(pos)) {
      blankPositions.push(pos);
    }
  }
  
  // 替换为空白
  for (let i = 0; i < chars.length; i++) {
    if (blankPositions.includes(i)) {
      chars[i] = '□';
    }
  }
  
  contentBlank = chars.join('');
  
  // 生成带输入框的内容
  let interactiveContent = escapeHtml(formatContent(contentBlank));
  let inputCount = 0;
  
  // 替换空白为输入框
  interactiveContent = interactiveContent.replace(/□/g, () => {
    const inputId = `blank-${Date.now()}-${inputCount++}`;
    return `<input type="text" class="dictation-input" id="${inputId}" maxlength="1" placeholder="_" style="width: 2rem; height: 1.5rem; text-align: center; margin: 0 0.2rem; border: 1px solid #ccc; border-radius: 4px; font-family: 'Noto Serif SC', serif; font-size: 1rem;">`;
  });

  resultContainer.innerHTML = `
    <div style="background: #f9f9f9; padding: 1.5rem; border-radius: 8px;">
      <h4 style="margin-bottom: 1rem;">填空默写：《${escapeHtml(poemData.title)}》</h4>
      <div class="poem-meta" style="margin-bottom: 1.5rem;">${escapeHtml(poemData.author)} · ${escapeHtml(poemData.dynasty)}</div>
      <div class="poem-text" style="margin-bottom: 1.5rem;">${interactiveContent}</div>
      <button class="btn btn-primary" id="btn-submit-dictation-this" data-content="${escapeHtml(poemData.content)}">提交答案</button>
    </div>
  `;
  
  // 添加提交按钮事件
  const submitBtn = document.getElementById('btn-submit-dictation-this');
  if (submitBtn) {
    submitBtn.addEventListener('click', () => {
      const originalContent = submitBtn.dataset.content;
      submitDictationThis(resultContainer, originalContent);
    });
  }
}

// 提交填空默写答案
function submitDictationThis(container, originalContent) {
  // 获取用户输入
  const inputs = container.querySelectorAll('.dictation-input');
  const userAnswers = Array.from(inputs).map(input => input.value.trim());
  
  // 提取原始内容中的空白位置的正确字符
  const originalChars = originalContent.split('');
  const blankPositions = [];
  let index = 0;
  
  for (let i = 0; i < originalChars.length; i++) {
    if (!originalChars[i].match(/[。，、；：？！\n]/)) {
      blankPositions.push(originalChars[i]);
    }
  }
  
  // 判定答案
  let correctCount = 0;
  for (let i = 0; i < userAnswers.length; i++) {
    if (userAnswers[i] === blankPositions[i]) {
      correctCount++;
    }
  }
  
  // 生成高亮显示的答案
  let highlightedContent = escapeHtml(formatContent(originalContent));
  
  // 分析空白位置并高亮显示
  const originalFormatted = formatContent(originalContent);
  let result = '';
  let originalIndex = 0;
  let answerIndex = 0;
  
  for (let i = 0; i < originalFormatted.length; i++) {
    if (originalFormatted[i] !== '。' && originalFormatted[i] !== '，' && originalFormatted[i] !== '、' && originalFormatted[i] !== '；' && originalFormatted[i] !== '：' && originalFormatted[i] !== '？' && originalFormatted[i] !== '！' && originalFormatted[i] !== '\n') {
      const userAnswer = userAnswers[answerIndex] || '';
      const isCorrect = userAnswer === originalFormatted[i];
      result += isCorrect 
        ? `<span style="color: green; font-weight: bold;">${escapeHtml(originalFormatted[i])}</span>`
        : `<span style="color: red; font-weight: bold;">${escapeHtml(originalFormatted[i])}</span>`;
      answerIndex++;
    } else {
      result += originalFormatted[i];
    }
  }
  
  // 显示结果
  container.innerHTML = `
    <div style="background: #f9f9f9; padding: 1.5rem; border-radius: 8px;">
      <h4 style="margin-bottom: 1rem;">默写结果</h4>
      <p style="margin-bottom: 1rem;">答对 ${correctCount} 题，共 ${userAnswers.length} 题</p>
      <p style="margin-bottom: 0.5rem; font-weight: 600;">参考答案：</p>
      <div class="poem-text" style="margin-bottom: 1.5rem;">${result}</div>
      <button class="btn btn-secondary" id="btn-reset-dictation-this">重新默写</button>
    </div>
  `;
  
  // 添加重新默写按钮事件
  const resetBtn = document.getElementById('btn-reset-dictation-this');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      container.style.display = 'none';
    });
  }
}

// ==================== 诗词问答功能 ====================
let quizQuestions = [];
let quizScore = 0;
let quizAnswered = 0;

// 诗词问答子标签页切换
document.querySelectorAll('.quiz-tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.quiz-tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.quiz-panel').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    const tab = btn.dataset.quizTab;
    document.getElementById(tab === 'practice' ? 'quiz-panel' : 'wrong-panel').classList.add('active');
    // 切换到错题时加载数据
    if (tab === 'wrong') {
      loadWrongAnswers();
    }
  });
});

// 开始答题
async function startQuiz() {
  const count = document.getElementById('questionCount').value;
  const type = document.getElementById('questionType').value;
  const btn = document.getElementById('btn-start-quiz');
  
  // 显示加载状态
  btn.textContent = '⏳ 生成题目中...';
  btn.disabled = true;
  
  try {
    const res = await fetch(`${API_BASE}/api/quiz/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ count: parseInt(count), type: type === 'all' ? null : type })
    });
    
    if (res.status === 401) {
      showToast('请先登录后再答题', 'warning');
      setTimeout(() => { window.location.href = '/login.html'; }, 1500);
      return;
    }
    
    const json = await res.json();
    
    if (!json.success) {
      showToast(json.message || '生成题目失败', 'error');
      return;
    }
    
    quizQuestions = json.data || [];
    quizScore = 0;
    quizAnswered = 0;
    
    if (quizQuestions.length === 0) {
      showToast('暂无可生成的题目', 'warning');
      return;
    }
    
    renderQuizQuestions();
    
    // 重置进度条
    updateProgress();
    
    document.getElementById('quizSetup').style.display = 'none';
    document.getElementById('quizArea').style.display = 'block';
    document.getElementById('quizResult').style.display = 'none';
    
    // 滚动到题目区域
    document.getElementById('quizArea').scrollIntoView({ behavior: 'smooth', block: 'start' });
  } catch (error) {
    console.error('生成题目失败:', error);
    showToast('网络错误，请检查后端服务是否启动', 'error');
  } finally {
    btn.innerHTML = '<span class="btn-icon">✍️</span>开始答题';
    btn.disabled = false;
  }
}

// 更新进度条
function updateProgress() {
  const progressText = document.getElementById('progressText');
  const progressPercent = document.getElementById('progressPercent');
  const progressFill = document.getElementById('progressFill');
  
  if (progressText && progressPercent && progressFill) {
    const percent = quizQuestions.length > 0 ? Math.round((quizAnswered / quizQuestions.length) * 100) : 0;
    progressText.textContent = `进度：${quizAnswered}/${quizQuestions.length}`;
    progressPercent.textContent = `${percent}%`;
    progressFill.style.width = `${percent}%`;
  }
}

// 渲染题目
function renderQuizQuestions() {
  const container = document.getElementById('quizQuestions');
  
  container.innerHTML = quizQuestions.map((q, index) => {
    // 解析options：可能是逗号分隔字符串、JSON字符串或数组
    let opts = q.options;
    if (typeof opts === 'string') {
      if (opts.startsWith('[')) {
        try { opts = JSON.parse(opts); } catch(e) { opts = []; }
      } else {
        opts = opts.split(',').map(s => s.trim()).filter(s => s.length > 0);
      }
    }
    if (!Array.isArray(opts)) opts = [];
    
    if (q.questionType === 'choice') {
      return `
        <div class="question-card" id="question-${index}">
          <div class="question-header">
            <span class="question-number">第 ${index + 1} 题</span>
            <span class="question-type">选择题</span>
          </div>
          <div class="question-content">${escapeHtml(q.question)}</div>
          <div class="options-container">
            ${opts.map((opt, optIndex) => `
              <button class="option-btn" onclick="submitQuizAnswer(${index}, '${escapeHtml(opt)}', this)">${escapeHtml(opt)}</button>
            `).join('')}
          </div>
          <div class="question-result" id="result-${index}" style="display: none;"></div>
        </div>
      `;
    } else if (q.questionType === 'judge') {
      return `
        <div class="question-card" id="question-${index}">
          <div class="question-header">
            <span class="question-number">第 ${index + 1} 题</span>
            <span class="question-type">判断题</span>
          </div>
          <div class="question-content">${escapeHtml(q.question)}</div>
          <div class="options-container">
            <button class="option-btn" onclick="submitQuizAnswer(${index}, '正确', this)">正确</button>
            <button class="option-btn" onclick="submitQuizAnswer(${index}, '错误', this)">错误</button>
          </div>
          <div class="question-result" id="result-${index}" style="display: none;"></div>
        </div>
      `;
    } else {
      return `
        <div class="question-card" id="question-${index}">
          <div class="question-header">
            <span class="question-number">第 ${index + 1} 题</span>
            <span class="question-type">填空题</span>
          </div>
          <div class="question-content">${escapeHtml(q.question)}</div>
          <div class="fill-input-container">
            <input type="text" id="fill-input-${index}" placeholder="请输入答案" class="fill-input">
            <button class="btn btn-primary" onclick="submitFillAnswer(${index})">提交</button>
          </div>
          <div class="question-result" id="result-${index}" style="display: none;"></div>
        </div>
      `;
    }
  }).join('');
}

// 提交选择/判断答案
async function submitQuizAnswer(index, userAnswer, btnElement) {
  const question = quizQuestions[index];
  const resultDiv = document.getElementById(`result-${index}`);
  const questionCard = document.getElementById(`question-${index}`);
  
  // 禁用所有按钮
  const allButtons = questionCard.querySelectorAll('.option-btn');
  allButtons.forEach(btn => {
    btn.disabled = true;
    if (btn.textContent === question.answer) {
      btn.classList.add('correct');
    }
  });
  
  try {
    const res = await fetch(`${API_BASE}/api/quiz/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        questionId: question.id,
        userAnswer: userAnswer
      })
    });
    
    if (res.status === 401) {
      resultDiv.innerHTML = '<div class="result-error">请先登录</div>';
      resultDiv.style.display = 'block';
      return;
    }
    
    const json = await res.json();
    
    if (json.correct) {
      btnElement.classList.add('correct');
      resultDiv.innerHTML = '<div class="result-correct">回答正确！</div>';
      quizScore++;
    } else {
      btnElement.classList.add('wrong');
      resultDiv.innerHTML = `<div class="result-wrong">回答错误，正确答案：${escapeHtml(question.answer)}</div>`;
    }
    
    resultDiv.style.display = 'block';
    quizAnswered++;
    
    // 标记题目为已答
    questionCard.classList.add('answered');
    
    // 更新进度条
    updateProgress();
    
    if (quizAnswered === quizQuestions.length) {
      showQuizResult();
    }
  } catch (error) {
    console.error('提交答案失败:', error);
    resultDiv.innerHTML = '<div class="result-error">提交失败</div>';
    resultDiv.style.display = 'block';
  }
}

// 提交填空答案
async function submitFillAnswer(index) {
  const input = document.getElementById(`fill-input-${index}`);
  const userAnswer = input.value.trim();
  
  if (!userAnswer) {
    showToast('请输入答案', 'warning');
    return;
  }
  
  const question = quizQuestions[index];
  const resultDiv = document.getElementById(`result-${index}`);
  const questionCard = document.getElementById(`question-${index}`);
  
  // 禁用输入框和按钮
  input.disabled = true;
  const submitBtn = questionCard.querySelector('.btn');
  submitBtn.disabled = true;
  
  try {
    const res = await fetch(`${API_BASE}/api/quiz/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        questionId: question.id,
        userAnswer: userAnswer
      })
    });
    
    if (res.status === 401) {
      resultDiv.innerHTML = '<div class="result-error">请先登录</div>';
      resultDiv.style.display = 'block';
      return;
    }
    
    const json = await res.json();
    
    if (json.correct) {
      resultDiv.innerHTML = '<div class="result-correct">回答正确！</div>';
      quizScore++;
    } else {
      resultDiv.innerHTML = `<div class="result-wrong">回答错误，正确答案：${escapeHtml(question.answer)}</div>`;
    }
    
    resultDiv.style.display = 'block';
    quizAnswered++;
    
    // 标记题目为已答
    questionCard.classList.add('answered');
    
    // 更新进度条
    updateProgress();
    
    if (quizAnswered === quizQuestions.length) {
      showQuizResult();
    }
  } catch (error) {
    console.error('提交答案失败:', error);
    resultDiv.innerHTML = '<div class="result-error">提交失败</div>';
    resultDiv.style.display = 'block';
  }
}

// 显示答题结果
function showQuizResult() {
  const resultDiv = document.getElementById('quizResult');
  const percentage = Math.round((quizScore / quizQuestions.length) * 100);
  
  resultDiv.innerHTML = `
    <div class="quiz-score">
      <h3>答题完成</h3>
      <div class="score-circle">
        <span class="score-number">${quizScore}/${quizQuestions.length}</span>
        <span class="score-percent">${percentage}%</span>
      </div>
      <p class="score-message">${percentage >= 80 ? '优秀！继续保持！' : percentage >= 60 ? '不错，继续努力！' : '还需加强练习，查看错题巩固知识！'}</p>
      <div class="quiz-actions">
        <button class="btn btn-primary" onclick="restartQuiz()">重新开始</button>
        <button class="btn btn-secondary" onclick="viewWrongAnswers()">查看错题</button>
      </div>
    </div>
  `;
  resultDiv.style.display = 'block';
}

// 重新开始
function restartQuiz() {
  document.getElementById('quizSetup').style.display = 'block';
  document.getElementById('quizArea').style.display = 'none';
  document.getElementById('quizResult').style.display = 'none';
  document.getElementById('quizQuestions').innerHTML = '';
  updateProgress();
}

// 查看错题
function viewWrongAnswers() {
  document.querySelector('.nav-link[data-section="wrong"]').click();
  loadWrongAnswers();
}

// 加载错题
async function loadWrongAnswers() {
  try {
    const res = await fetch(`${API_BASE}/api/quiz/wrongAnswers`);
    
    if (res.status === 401) {
      document.getElementById('wrongAnswers').innerHTML = '<p class="empty">请先登录后查看错题</p>';
      return;
    }
    
    const json = await res.json();
    
    const container = document.getElementById('wrongAnswers');
    const emptyState = document.getElementById('wrongEmpty');
    
    if (!json.success) {
      container.innerHTML = '<p class="empty">加载失败</p>';
      return;
    }
    
    const wrongAnswers = json.data || [];
    
    if (wrongAnswers.length === 0) {
      container.innerHTML = '';
      emptyState.style.display = 'block';
      return;
    }
    
    emptyState.style.display = 'none';
    
    container.innerHTML = wrongAnswers.map(w => `
      <div class="wrong-card" id="wrong-${w.id}">
        <div class="wrong-question">${escapeHtml(w.question)}</div>
        <div class="wrong-detail">
          <span class="label">你的答案：</span><span class="wrong-answer">${escapeHtml(w.userAnswer)}</span>
        </div>
        <div class="wrong-detail">
          <span class="label">正确答案：</span><span class="correct-answer">${escapeHtml(w.correctAnswer)}</span>
        </div>
        <div class="wrong-detail">
          <span class="label">知识点：</span><span>${escapeHtml(w.knowledgePoint || '')}</span>
        </div>
        <div class="wrong-detail">
          <span class="label">错误次数：</span><span>${w.wrongCount || 1}</span>
        </div>
        <button class="btn btn-secondary btn-small" onclick="removeWrong(${w.id})">已掌握</button>
      </div>
    `).join('');
  } catch (error) {
    console.error('加载错题失败:', error);
    showToast('网络错误', 'error');
  }
}

// 移除错题
async function removeWrong(id) {
  try {
    const res = await fetch(`${API_BASE}/api/quiz/wrongAnswers/remove`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: id })
    });
    
    if (res.status === 401) {
      showToast('请先登录', 'warning');
      return;
    }
    
    const json = await res.json();
    
    if (json.success) {
      showToast('已掌握', 'success');
      const wrongCard = document.getElementById(`wrong-${id}`);
      if (wrongCard) {
        wrongCard.remove();
      }
      
      // 如果没有错题了，显示空状态
      const container = document.getElementById('wrongAnswers');
      if (container.children.length === 0) {
        document.getElementById('wrongEmpty').style.display = 'block';
      }
    }
  } catch (error) {
    console.error('移除错题失败:', error);
    showToast('移除失败', 'error');
  }
}

// 监听hash变化加载错题
window.addEventListener('hashchange', function() {
  const hash = window.location.hash.substring(1);
  if (hash === 'wrong') {
    loadWrongAnswers();
  }
});

// ==================== 诗词详情页增强功能 ====================

// 显示原文（展开诗词原文）
function showOriginalPoem(title, author, dynasty, content) {
  const detailContainer = document.getElementById('poem-detail');
  if (!detailContainer) return;
  
  // 检查是否已经有原文区域
  let originalSection = document.getElementById('poem-original-section');
  if (!originalSection) {
    // 在诗词介绍之后添加原文区域
    const introductionSection = detailContainer.querySelector('.detail-section:nth-of-type(1)');
    originalSection = document.createElement('div');
    originalSection.id = 'poem-original-section';
    originalSection.className = 'detail-section';
    originalSection.style.background = 'var(--paper)';
    originalSection.style.padding = '1.5rem';
    originalSection.style.borderRadius = '8px';
    originalSection.style.marginBottom = '1.5rem';
    originalSection.innerHTML = `
      <h4>诗词原文</h4>
      <div style="font-size: 1.15rem; line-height: 1.9; white-space: pre-wrap; padding: 1rem; background: white; border-radius: 6px; border-left: 3px solid var(--accent);">${escapeHtml(content)}</div>
    `;
    if (introductionSection) {
      detailContainer.insertBefore(originalSection, introductionSection.nextSibling);
    } else {
      detailContainer.appendChild(originalSection);
    }
  } else {
    // 已经显示，则滚动到该区域
    originalSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

// 显示AI赏析
async function showAIAppreciation(title, author, dynasty, content) {
  const detailContainer = document.getElementById('poem-detail');
  if (!detailContainer) return;
  
  const btn = event.target;
  const originalText = btn.textContent;
  btn.textContent = '生成中...';
  btn.disabled = true;
  
  // 检查是否已经有赏析区域
  let appreciationSection = document.getElementById('poem-appreciation-section');
  if (!appreciationSection) {
    appreciationSection = document.createElement('div');
    appreciationSection.id = 'poem-appreciation-section';
    appreciationSection.className = 'detail-section';
    detailContainer.appendChild(appreciationSection);
  }
  
  appreciationSection.innerHTML = '<h4>AI赏析</h4><div class="detail-content"><p style="color: var(--ink-muted);">正在生成赏析，请稍候...</p></div>';
  
  try {
    const response = await fetch(`${API_BASE}/api/qa/ai/describe_poem`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: title,
        author: author,
        dynasty: dynasty,
        content: content
      })
    });
    
    const json = await response.json();
    
    if (json.success && json.data && json.data.description) {
      appreciationSection.innerHTML = `
        <h4>AI赏析</h4>
        <div class="detail-content" style="white-space: pre-wrap; line-height: 1.9; padding: 1.5rem; background: var(--paper); border-radius: 8px; border-left: 3px solid var(--gold);">${escapeHtml(json.data.description)}</div>
      `;
    } else {
      appreciationSection.innerHTML = '<h4>AI赏析</h4><div class="detail-content"><p style="color: var(--red-seal);">生成失败：' + (json.message || '未知错误') + '</p></div>';
    }
  } catch (error) {
    console.error('AI赏析失败:', error);
    appreciationSection.innerHTML = '<h4>AI赏析</h4><div class="detail-content"><p style="color: var(--red-seal);">生成失败，请检查网络连接</p></div>';
  } finally {
    btn.textContent = originalText;
    btn.disabled = false;
  }
  
  // 滚动到赏析区域
  appreciationSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
}
