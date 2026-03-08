/**
 * 诗韵雅集 - 古诗词智能问答 - 前端逻辑
 */
const API_BASE = '';

// 导航切换
document.querySelectorAll('.nav-link').forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    const section = link.dataset.section;
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    link.classList.add('active');
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(section).classList.add('active');
  });
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

  container.innerHTML = poems.map(p => `
    <div class="poem-card" data-title="${escapeHtml(p.title)}">
      <div class="title">《${escapeHtml(p.title)}》</div>
      <div class="meta">${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</div>
      <div class="preview">${escapeHtml((p.content || '').slice(0, 80))}...</div>
    </div>
  `).join('');

  container.querySelectorAll('.poem-card').forEach(card => {
    card.addEventListener('click', () => showPoemDetail(card.dataset.title));
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

// 飞花令
async function feihualing() {
  const char = document.getElementById('feihualing-char').value.trim();
  const resultEl = document.getElementById('feihualing-result');

  if (!char || char.length !== 1) {
    resultEl.innerHTML = '<p class="empty">请输入一个汉字</p>';
    return;
  }

  const res = await fetch(`${API_BASE}/api/feihualing?char=${encodeURIComponent(char)}`);
  const json = await res.json();

  if (!json.success) {
    resultEl.innerHTML = `<p class="empty">${escapeHtml(json.message || '未找到')}</p>`;
    return;
  }

  const lines = json.data || [];
  resultEl.innerHTML = lines.length
    ? `<p style="margin-bottom:0.5rem;color:var(--ink-muted)">含「${char}」的诗句：</p>` +
      lines.map(l => `<div class="line-item">${escapeHtml(l.line)} — 《${escapeHtml(l.title)}》${escapeHtml(l.author)}</div>`).join('')
    : '<p class="empty">未找到含该字的诗句</p>';
}

document.getElementById('btn-feihualing').addEventListener('click', feihualing);

// AI 写诗
async function aiWrite() {
  const theme = document.getElementById('ai-theme').value.trim() || '春';
  const resultEl = document.getElementById('ai-result');

  resultEl.innerHTML = '<p class="empty">创作中...</p>';

  const res = await fetch(`${API_BASE}/api/ai/write`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ theme })
  });
  const json = await res.json();

  if (!json.success) {
    resultEl.innerHTML = `<p class="empty">${escapeHtml(json.message || '生成失败')}</p>`;
    return;
  }

  const d = json.data;
  resultEl.innerHTML = `
    <p style="margin-bottom:0.5rem;color:var(--ink-muted)">${escapeHtml(d.title || '')}</p>
    <div class="poem-content">${escapeHtml(d.content || '')}</div>
  `;
}

document.getElementById('btn-ai-write').addEventListener('click', aiWrite);

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
  `;
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
  
  // 保存原始内容，用于判定答案
  container.dataset.originalContent = contentOriginal;
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
  
  // 显示结果
  const answerEl = document.getElementById('dictation-answer');
  answerEl.innerHTML = `
    <p style="margin-bottom:0.5rem;font-weight:600">提交结果：</p>
    <p>答对 ${correctCount} 题，共 ${userAnswers.length} 题</p>
    <p style="margin-bottom:0.5rem;font-weight:600">参考答案：</p>
    <div class="poem-text">${escapeHtml(formatContent(originalContent))}</div>
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

navTabs.forEach(btn => {
  btn.addEventListener('click', async () => {
    navTabs.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const tab = btn.dataset.tab;
    const res = await fetch(`${API_BASE}/api/nav/${tab}`);
    const json = await res.json();
    if (!json.success) return;
    const items = json.data || [];
    navContent.innerHTML = items.map(item =>
      `<span class="nav-item" data-type="${tab}" data-name="${escapeHtml(item.name)}">${escapeHtml(item.name)} (${item.count || ''})</span>`
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
  });
});

navTabs[0].click();

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
    let html = `
      <div class="detail-title">《${escapeHtml(p.title)}》</div>
      <div class="detail-meta">${escapeHtml(p.author)} · ${escapeHtml(p.dynasty)} · ${escapeHtml(p.tag || '')}</div>
      <div class="detail-section">
        <h4>原文</h4>
        <div class="detail-content">${escapeHtml(formatContent(p.content))}</div>
      </div>
    `;
    if (p.annotation) {
      html += `<div class="detail-section"><h4>注释</h4><p>${escapeHtml(p.annotation)}</p></div>`;
    }
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
    detail.innerHTML = html;
  }
  modal.classList.add('active');
}
window.showPoemDetail = showPoemDetail;

document.getElementById('modal-close').addEventListener('click', () => {
  document.getElementById('poem-modal').classList.remove('active');
});

document.getElementById('poem-modal').addEventListener('click', (e) => {
  if (e.target.id === 'poem-modal') e.target.classList.remove('active');
});

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
