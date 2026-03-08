# 诗韵雅集 · 古诗词智能问答

一款国风设计的古诗词智能问答网页应用，提供多维度诗词检索、自然语言智能问答、诗词全文深度解析，以及飞花令、AI 写诗、诗词背诵默写、诗人/题材分类导航等趣味互动功能。

## 功能特性

- **多维诗词检索**：按诗名、诗人、朝代、题材等条件精准查找
- **自然语言智能问答**：支持如「李白写过什么诗」「静夜思是谁写的」等自然问法
- **诗词深度解析**：原文、注释、译文、创作背景、情感主旨、典故意象讲解
- **飞花令**：输入一字，检索包含该字的诗句
- **AI 写诗**：根据主题生成诗词（可扩展接入大模型 API）
- **诗词背诵**：随机抽取诗词进行背诵练习
- **填空默写**：随机挖空，辅助记忆
- **分类导航**：按诗人、题材、朝代浏览

## 项目结构

```
PoemWebApp/
├── backend/
│   └── app.py          # Flask 后端 API
├── frontend/
│   ├── index.html      # 主页面
│   ├── css/style.css   # 国风样式
│   └── js/app.js       # 前端逻辑
├── data/
│   └── poems.json      # 诗词数据（由脚本生成）
├── scripts/
│   └── build_data.py   # 从 PoemKBQA 数据生成 JSON
├── requirements.txt
└── README.md
```

## 快速开始

### 1. 生成诗词数据

```bash
cd PoemWebApp
python scripts/build_data.py
```

（需确保 `../PoemKBQA-master/poemData/csv/all.csv` 存在）

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

### 3. 启动服务

```bash
cd backend
python app.py
```

访问 http://localhost:5000

### 4. 可选：启用 Neo4j 智能问答

若需使用原 PoemKBQA 的 Neo4j 知识图谱进行更精准的智能问答：

1. 安装并启动 Neo4j
2. 在 PoemKBQA-master 目录运行 `build_graph.py` 构建图谱
3. 修改 `get_answer.py` 中的 Neo4j 连接配置
4. 安装：`pip install py2neo jieba joblib numpy`

未配置 Neo4j 时，系统会自动使用本地 JSON 数据提供基础问答能力。

## 技术栈

- 后端：Flask + Flask-CORS
- 前端：原生 HTML/CSS/JavaScript，响应式布局
- 字体：Noto Serif SC、Ma Shan Zheng（毛笔风格）
- 设计：国风配色（墨色、宣纸、朱砂、竹青等）

## 数据来源

诗词数据来自 PoemKBQA 项目的 CSV，深度解析（注释、译文、背景等）对部分名篇做了示例补充，其余可后续接入 AI 生成或人工标注。
