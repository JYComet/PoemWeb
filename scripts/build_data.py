# -*- coding: utf-8 -*-
"""将 CSV 诗词数据转换为 JSON，并添加深度解析字段"""
import csv
import json
import os

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    csv_path = os.path.join(base_dir, '..', 'PoemKBQA-master', 'poemData', 'csv', 'all.csv')
    
    poems = []
    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        for row in reader:
            if len(row) >= 5:
                poems.append({
                    'author': row[0],
                    'dynasty': row[1],
                    'title': row[2],
                    'content': row[3],
                    'tag': row[4],
                    'annotation': '',
                    'translation': '',
                    'background': '',
                    'emotion': '',
                    'allusion': ''
                })

    # 名篇深度解析示例
    sample_analysis = {
        '静夜思': {
            'annotation': '疑：好像。举头：抬头。',
            'translation': '明亮的月光洒在床前的窗户纸上，好像地上泛起了一层霜。我禁不住抬起头来，看那天窗外空中的一轮明月，不由得低头沉思，想起远方的家乡。',
            'background': '李白于开元十四年（726年）秋作于扬州旅舍，时年二十六岁。诗人漂泊异乡，见月思乡。',
            'emotion': '思乡之情，羁旅之愁。通过明月这一意象，表达对故乡的深切怀念。',
            'allusion': '明月：古典诗词中常象征思乡、团圆。床前明月：化用《古诗十九首》中明月意象。'
        },
        '春晓': {
            'annotation': '不觉晓：不知不觉天就亮了。晓：早晨，天明。闻：听见。啼鸟：鸟啼。',
            'translation': '春夜酣睡不知不觉天已破晓，处处可以听到小鸟的啼叫声。昨夜风雨声一直不断，那娇美的春花不知被吹落了多少？',
            'background': '孟浩然隐居鹿门山时所作，描写春日早晨醒来时的感受。',
            'emotion': '对春天的喜爱与怜惜，对自然生命的敏感与关怀。',
            'allusion': '风雨声：象征时光流逝、世事变迁。落花：常喻美好事物的消逝。'
        },
        '登鹳雀楼': {
            'annotation': '鹳雀楼：古名鹳鹊楼。白日：太阳。依：依傍。尽：消失。',
            'translation': '夕阳依傍着西山慢慢地沉没，滔滔黄河朝着东海汹涌奔流。若想把千里的风光景物看够，那就要登上更高的一层城楼。',
            'background': '王之涣在蒲州（今山西永济）任职时登鹳雀楼所作。',
            'emotion': '积极进取、登高望远的豪迈情怀，蕴含哲理。',
            'allusion': '更上一层楼：成为励志名句，喻指不断进取。'
        },
        '春江花月夜': {
            'annotation': '滟滟：水光摇曳。芳甸：芳草丰茂的原野。霰：小雪珠。',
            'translation': '春天的江潮水势浩荡，与大海连成一片，一轮明月从海上升起，好像与潮水一起涌出来。月光照耀着春江，随着波浪闪耀千万里，所有地方的春江都有明亮的月光。',
            'background': '张若虚作，被誉为「孤篇压全唐」。描绘春江月夜的壮丽景象，探索宇宙与人生的哲理。',
            'emotion': '对自然之美的赞叹，对人生短暂的感慨，对离人思妇的同情。',
            'allusion': '江月：永恒与短暂的对比。离人：游子思妇主题。'
        },
        '望庐山瀑布': {
            'annotation': '香炉：香炉峰。挂前川：一作「挂长川」。九天：极高的天空。',
            'translation': '香炉峰在阳光的照射下生起紫色烟霞，远远望见瀑布似白色绢绸悬挂在山前。高崖上飞腾直落的瀑布好像有几千尺，让人恍惚以为银河从天上泻落到人间。',
            'background': '李白游庐山时所作，以夸张手法描绘瀑布的壮丽。',
            'emotion': '对自然奇观的惊叹与赞美，浪漫主义情怀。',
            'allusion': '银河落九天：极言瀑布之高，想象奇特。'
        }
    }

    for p in poems:
        if p['title'] in sample_analysis:
            p.update(sample_analysis[p['title']])

    out_dir = os.path.join(base_dir, 'data')
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, 'poems.json')
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(poems, f, ensure_ascii=False, indent=2)
    print(f'已导出 {len(poems)} 首诗词至 {out_path}')

if __name__ == '__main__':
    main()
