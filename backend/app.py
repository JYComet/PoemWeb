# -*- coding: utf-8 -*-
"""古诗词智能问答 Web 应用 - Flask 后端"""
import os
import sys
import json

# 添加 PoemKBQA 路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', '..', 'PoemKBQA-master'))

from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__, static_folder='../frontend', static_url_path='')
CORS(app)

# 诗词数据
POEMS_DATA = None

def load_poems():
    global POEMS_DATA
    if POEMS_DATA is None:
        data_path = os.path.join(os.path.dirname(__file__), '..', 'data', 'poems.json')
        if os.path.exists(data_path):
            with open(data_path, 'r', encoding='utf-8') as f:
                POEMS_DATA = json.load(f)
        else:
            POEMS_DATA = []
    return POEMS_DATA


@app.route('/')
def index():
    return app.send_static_file('index.html')


@app.route('/api/poems/search', methods=['GET'])
def search_poems():
    """多维度诗词检索"""
    poems = load_poems()
    keyword = request.args.get('keyword', '').strip()
    author = request.args.get('author', '').strip()
    dynasty = request.args.get('dynasty', '').strip()
    tag = request.args.get('tag', '').strip()


    results = poems
    if keyword:
        results = [p for p in results if keyword in p['title'] or keyword in p['content'] or keyword in p.get('author', '')]
    if author:
        results = [p for p in results if author in p.get('author', '')]
    if dynasty:
        results = [p for p in results if dynasty in p.get('dynasty', '')]
    if tag:
        results = [p for p in results if tag in p.get('tag', '')]

    return jsonify({'success': True, 'data': results[:50], 'total': len(results)})


@app.route('/api/poems/detail', methods=['GET'])
def get_poem_detail():
    """获取诗词详情（含深度解析）"""
    title = request.args.get('title', '')
    poems = load_poems()
    for p in poems:
        if p['title'] == title:
            return jsonify({'success': True, 'data': p})
    return jsonify({'success': False, 'message': '未找到该诗词'}), 404


@app.route('/api/qa', methods=['POST'])
def intelligent_qa():
    """自然语言智能问答"""
    question = (request.json or {}).get('question', '').strip()
    if not question:
        return jsonify({'success': False, 'message': '请输入问题'})

    try:
        from QuestionClassifier import PoemQuestionClassifier
        from get_answer import GetAnswer
        pqc = PoemQuestionClassifier()
        ga = GetAnswer()
        index, params = pqc.analysis_question(question)
        answers = ga.get_data(index, params[0]) if params else []

        if index == 0:
            result = list(set([a['p.name'] for a in answers]))
        elif index == 1:
            result = answers[0]['a.name'] if answers else None
        elif index == 2:
            result = list(set([a['a.name'] for a in answers]))
        elif index == 3:
            result = answers[0]['d.name'] if answers else None
        elif index == 4:
            result = [{'title': a['p.name'], 'content': a['p.content'], 'author': a['a.name'], 'dynasty': a['d.name']} for a in answers]
        elif index == 5:
            result = list(set([a['t.name'] for a in answers]))
        elif index == 6:
            result = [{'title': a['p.name'], 'content': a['p.content'], 'author': a['a.name'], 'dynasty': a['d.name']} for a in answers]
        else:
            result = None

        return jsonify({'success': True, 'data': result, 'type': index})
    except Exception:
        # Neo4j 未配置或出错时使用本地 JSON 数据问答
        return qa_fallback(question)


def qa_fallback(question):
    """无 Neo4j 时的本地问答回退"""
    poems = load_poems()
    q = question.lower()

    # 按关键词匹配
    if '写过' in question or '写' in question:
        for p in poems:
            if p['author'] in question:
                author_poems = [x['title'] for x in poems if x['author'] == p['author']]
                return jsonify({'success': True, 'data': list(set(author_poems))[:20], 'type': 0})

    if '朝代' in question or '哪个' in question:
        for p in poems:
            if p['author'] in question:
                return jsonify({'success': True, 'data': p['dynasty'], 'type': 3})

    if '诗人' in question or '朝代' in question:
        dynasty = next((d for d in ['唐代', '宋代', '元代', '明代', '清代', '五代'] if d in question), None)
        if dynasty:
            authors = list(set([p['author'] for p in poems if p['dynasty'] == dynasty]))
            return jsonify({'success': True, 'data': authors[:30], 'type': 2})

    # 按诗名查询
    for p in poems:
        if p['title'] in question:
            return jsonify({'success': True, 'data': [p], 'type': 4})

    # 按诗句查询
    for p in poems:
        if p['content'] and question in p['content']:
            return jsonify({'success': True, 'data': [p], 'type': 6})

    return jsonify({'success': False, 'message': '未找到相关答案，请尝试其他问法'})


@app.route('/api/feihualing', methods=['GET'])
def feihualing():
    """飞花令 - 按关键字返回含该字的诗句"""
    poems = load_poems()
    char = request.args.get('char', '').strip()
    if not char or len(char) != 1:
        return jsonify({'success': False, 'message': '请输入一个汉字'})

    lines = []
    for p in poems:
        if p['content'] and char in p['content']:
            for s in p['content'].replace('。', '。\n').replace('，', '，\n').replace('？', '？\n').replace('！', '！\n').split('\n'):
                s = s.strip()
                if s and char in s and len(s) <= 30:
                    lines.append({'line': s, 'title': p['title'], 'author': p['author']})

    return jsonify({'success': True, 'data': lines[:30], 'char': char})


@app.route('/api/ai/write', methods=['POST'])
def ai_write_poem():
    """AI 写诗"""
    theme = request.json.get('theme', '春').strip() or '春'
    # 简单模板生成（可接入真实 AI API）
    templates = [
        f'{theme}风拂面暖，万物复苏时。',
        f'一{theme}一世界，一花一菩提。',
        f'待到{theme}来日，花开满枝头。',
        f'{theme}水悠悠去，青山隐隐来。',
        f'明月照{theme}江，清风送客归。'
    ]
    import random
    lines = random.sample(templates, 4)
    poem = '\n'.join(lines)
    return jsonify({'success': True, 'data': {'content': poem, 'title': f'《{theme}》'}})


@app.route('/api/ai/describe_poem', methods=['POST'])
def ai_describe_poem():
    """AI 生成诗词描述"""
    title = request.json.get('title', '').strip()
    author = request.json.get('author', '').strip()
    dynasty = request.json.get('dynasty', '').strip()
    content = request.json.get('content', '').strip()
    
    if not title or not author:
        return jsonify({'success': False, 'message': '缺少必要参数'})
    
    # 简单模板生成诗词描述（可接入真实 AI API）
    descriptions = [
        f'《{title}》是{author}在{ dynasty }时期创作的经典作品。此诗语言凝练，意境深远，通过对自然景物的描绘，表达了诗人对生活的感悟和对美好事物的向往。全诗结构严谨，韵律和谐，展现了中国古典诗歌的独特魅力。',
        f'{dynasty}诗人{author}的《{title}》是中国文学史上的瑰宝。诗中运用生动的意象和凝练的语言，营造出独特的艺术境界，表达了诗人内心的情感世界。这首诗不仅具有极高的文学价值，也是研究{ dynasty }社会文化的重要资料。',
        f'《{title}》为{author}所作，是{ dynasty }诗歌的代表作之一。诗中通过细腻的描写和深刻的意境，展现了诗人的艺术才华和思想深度。此诗语言优美，情感真挚，千百年来一直为人们所传诵，成为中国古典文学的经典之作。',
        f'{author}的《{title}》创作于{ dynasty }，是一首充满诗意的佳作。诗中描绘了美丽的自然景象，表达了诗人对生活的热爱和对理想的追求。全诗意境优美，语言流畅，体现了中国古典诗歌的独特韵味。',
        f'《{title}》是{ dynasty }诗人{author}的传世之作。此诗以简洁的语言和深刻的意境，展现了诗人对人生的思考和对美好事物的赞美。诗歌结构精巧，韵律和谐，充分体现了中国古典诗歌的艺术魅力。'
    ]
    
    import random
    description = random.choice(descriptions)
    
    # 如果有内容，根据内容生成更具体的描述
    if content:
        # 提取前两句作为参考
        content_lines = content.split('。')[:2]
        content_sample = '。'.join(content_lines).strip()
        if content_sample:
            description = f'《{title}》是{author}在{ dynasty }时期的作品。诗中写道："{content_sample}。" 通过这样的描写，诗人营造出了独特的艺术氛围，表达了内心的情感。此诗语言凝练，意境深远，展现了中国古典诗歌的魅力。'
    
    return jsonify({'success': True, 'data': {'description': description}})


@app.route('/api/recite', methods=['GET'])
def recite():
    """诗词背诵 - 随机返回一首诗词（可隐藏部分）"""
    poems = load_poems()
    import random
    p = random.choice(poems)
    return jsonify({'success': True, 'data': p})


@app.route('/api/dictation', methods=['GET'])
def dictation():
    """诗词默写 - 返回带空白的诗词"""
    poems = load_poems()
    import random
    p = random.choice(poems)
    content = p['content']
    # 随机替换部分字为下划线
    chars = list(content)
    blanks = min(5, len([c for c in chars if c not in '。，、；：？！\n']))
    indices = [i for i, c in enumerate(chars) if c not in '。，、；：？！\n']
    if indices:
        for i in random.sample(indices, min(blanks, len(indices))):
            chars[i] = '□'
    p['content_blank'] = ''.join(chars)
    return jsonify({'success': True, 'data': p})


@app.route('/api/nav/authors', methods=['GET'])
def nav_authors():
    """诗人分类导航"""
    poems = load_poems()
    authors = {}
    for p in poems:
        a = p.get('author', '')
        if a not in authors:
            authors[a] = {'dynasty': p.get('dynasty', ''), 'count': 0}
        authors[a]['count'] += 1
    return jsonify({'success': True, 'data': [{'name': k, **v} for k, v in sorted(authors.items(), key=lambda x: -x[1]['count'])]})


@app.route('/api/nav/tags', methods=['GET'])
def nav_tags():
    """题材分类导航"""
    poems = load_poems()
    tags = {}
    for p in poems:
        t = p.get('tag', '其他')
        tags[t] = tags.get(t, 0) + 1
    return jsonify({'success': True, 'data': [{'name': k, 'count': v} for k, v in sorted(tags.items(), key=lambda x: -x[1])]})


@app.route('/api/nav/dynasties', methods=['GET'])
def nav_dynasties():
    """朝代分类导航"""
    poems = load_poems()
    dynasties = {}
    for p in poems:
        d = p.get('dynasty', '')
        dynasties[d] = dynasties.get(d, 0) + 1
    return jsonify({'success': True, 'data': [{'name': k, 'count': v} for k, v in sorted(dynasties.items(), key=lambda x: -x[1])]})


@app.route('/api/graph/knowledge', methods=['GET'])
def knowledge_graph():
    """知识图谱数据：诗人-朝代-诗词-题材 关系，供 ECharts graph 使用"""
    poems = load_poems()
    limit = min(int(request.args.get('limit', 150)), 300)
    # 统计并选取高频节点
    author_count = {}
    dynasty_set = set()
    tag_set = set()
    poem_titles = set()
    for p in poems:
        author_count[p.get('author', '')] = author_count.get(p['author'], 0) + 1
        dynasty_set.add(p.get('dynasty', ''))
        tag_set.add(p.get('tag', '') or '其他')
        poem_titles.add(p.get('title', ''))
    # 取作品数较多的诗人
    top_authors = sorted(author_count.keys(), key=lambda x: -author_count[x])[:limit // 4]
    dynasties = list(dynasty_set)
    tags = list(tag_set)
    # 节点：诗人/朝代/诗词/题材
    nodes = []
    node_ids = set()
    categories = [{'name': '诗人'}, {'name': '朝代'}, {'name': '诗词'}, {'name': '题材'}]

    def add_node(name, cat, value=1):
        if not name or name in node_ids:
            return
        node_ids.add(name)
        nodes.append({'name': name, 'category': cat, 'value': value, 'symbolSize': 10 + min(value, 50)})

    for a in top_authors:
        add_node(a, 0, author_count[a])
    for d in dynasties:
        add_node(d, 1)
    for t in tags:
        add_node(t, 3)
    # 边与诗词节点：只取部分诗词避免过多
    links = []
    poem_count = 0
    poem_limit = limit // 2
    for p in poems:
        if p['author'] not in top_authors:
            continue
        if poem_count >= poem_limit:
            break
        title = p.get('title', '')
        if not title or title in node_ids:
            continue
        node_ids.add(title)
        nodes.append({'name': title, 'category': 2, 'value': 1, 'symbolSize': 8})
        poem_count += 1
        links.append({'source': p['author'], 'target': title})
        links.append({'source': p['author'], 'target': p.get('dynasty', '')})
        links.append({'source': title, 'target': p.get('tag', '') or '其他'})
    return jsonify({
        'success': True,
        'data': {'nodes': nodes, 'links': links, 'categories': categories}
    })


@app.route('/api/poet/trajectory', methods=['GET'])
def poet_trajectory():
    """诗人行迹：作品数量排行、朝代分布、指定诗人的题材分布"""
    poems = load_poems()
    author = request.args.get('author', '').strip()

    # 诗人作品数量排行（Top 20）
    author_count = {}
    for p in poems:
        a = p.get('author', '')
        author_count[a] = author_count.get(a, 0) + 1
    author_rank = sorted([{'name': k, 'value': v} for k, v in author_count.items()],
                         key=lambda x: -x['value'])[:20]

    # 各朝代诗词分布
    dynasty_count = {}
    for p in poems:
        d = p.get('dynasty', '')
        dynasty_count[d] = dynasty_count.get(d, 0) + 1
    dynasty_dist = [{'name': k, 'value': v} for k, v in sorted(dynasty_count.items(), key=lambda x: -x[1])]

    result = {'authorRank': author_rank, 'dynastyDist': dynasty_dist}

    # 若指定诗人，返回该诗人的题材分布（创作轨迹）
    if author:
        poet_poems = [p for p in poems if p.get('author') == author]
        tag_count = {}
        for p in poet_poems:
            t = p.get('tag', '') or '其他'
            tag_count[t] = tag_count.get(t, 0) + 1
        result['poetTagDist'] = {
            'author': author,
            'dynasty': poet_poems[0].get('dynasty', '') if poet_poems else '',
            'total': len(poet_poems),
            'tagDist': [{'name': k, 'value': v} for k, v in sorted(tag_count.items(), key=lambda x: -x[1])]
        }
        result['poetPoems'] = [{'title': p['title'], 'tag': p.get('tag', '')} for p in poet_poems[:30]]

    return jsonify({'success': True, 'data': result})


# 诗人行迹地图数据
POET_TRAJECTORY = None

def load_poet_trajectory():
    global POET_TRAJECTORY
    if POET_TRAJECTORY is None:
        path = os.path.join(os.path.dirname(__file__), '..', 'data', 'poet_trajectory.json')
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                POET_TRAJECTORY = json.load(f)
        else:
            POET_TRAJECTORY = {}
    return POET_TRAJECTORY


@app.route('/api/poet/trajectory/map', methods=['GET'])
def poet_trajectory_map():
    """诗人行迹地图：地点变动、时间、该地创作的诗词"""
    trajectory = load_poet_trajectory()
    author = request.args.get('author', '').strip()
    poems_data = load_poems()

    # 可选诗人列表
    poets = list(trajectory.keys())

    if not author:
        return jsonify({
            'success': True,
            'data': {
                'poets': poets,
                'trajectory': None,
                'message': '请选择诗人查看行迹地图'
            }
        })

    if author not in trajectory:
        return jsonify({
            'success': True,
            'data': {
                'poets': poets,
                'trajectory': None,
                'message': f'暂无 {author} 的行迹数据'
            }
        })

    points = trajectory[author]
    poem_title_to_content = {p['title']: p for p in poems_data}

    # 为每个地点补充诗词详情
    for pt in points:
        pt['poemsDetail'] = []
        for title in pt.get('poems', []):
            detail = poem_title_to_content.get(title)
            if detail:
                pt['poemsDetail'].append({
                    'title': title,
                    'content': detail.get('content', '')[:200] + ('...' if len(detail.get('content', '')) > 200 else '')
                })
            else:
                pt['poemsDetail'].append({'title': title, 'content': ''})

    # 生成路线（按时间顺序连线）
    lines = []
    for i in range(len(points) - 1):
        lines.append({
            'from': [points[i]['lng'], points[i]['lat']],
            'to': [points[i + 1]['lng'], points[i + 1]['lat']],
            'yearFrom': points[i]['year'],
            'yearTo': points[i + 1]['year']
        })

    return jsonify({
        'success': True,
        'data': {
            'poets': poets,
            'author': author,
            'trajectory': points,
            'lines': lines
        }
    })


if __name__ == '__main__':
    load_poems()
    load_poet_trajectory()
    app.run(host='0.0.0.0', port=5000, debug=True)
