package com.poemweb.service;

import com.poemweb.entity.Poem;
import com.poemweb.mapper.PoemMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class PoemDetailEnhancer {

    @Autowired
    private PoemMapper poemMapper;

    private Map<String, String[]> annotationData = new HashMap<>();
    private Map<String, String[]> translationData = new HashMap<>();
    private Map<String, String[]> backgroundData = new HashMap<>();
    private Map<String, String[]> emotionData = new HashMap<>();
    private Map<String, String[]> allusionData = new HashMap<>();

    @PostConstruct
    public void init() {
        // 初始化注解数据
        initAnnotationData();
        initTranslationData();
        initBackgroundData();
        initEmotionData();
        initAllusionData();
    }

    private void initAnnotationData() {
        annotationData.put("静夜思", new String[]{
            "床前明月光：床前明亮的月光。",
            "疑是地上霜：看起来好像是地上结了霜。",
            "举头望明月：抬起头来看着天上的明月。",
            "低头思故乡：低下头思念起自己的故乡。"
        });
        annotationData.put("春望", new String[]{
            "国破山河在：国家沦陷了，但山河依旧存在。",
            "城春草木深：春天来了，长安城中草木丛生。",
            "感时花溅泪：感伤时局，看到花开也流泪。",
            "恨别鸟惊心：痛恨离别，听到鸟鸣也惊心。",
            "烽火连三月：战火已经连续了三个月。",
            "家书抵万金：一封家信价值万金。"
        });
        annotationData.put("登鹳雀楼", new String[]{
            "白日依山尽：太阳依傍着西山慢慢落下。",
            "黄河入海流：黄河水奔腾着流向大海。",
            "欲穷千里目：想要看到更远的景色。",
            "更上一层楼：就要登上更高的一层楼。"
        });
        annotationData.put("黄鹤楼送孟浩然之广陵", new String[]{
            "故人西辞黄鹤楼：老朋友在西边的黄鹤楼辞别。",
            "烟花三月下扬州：在繁花似锦的三月顺江而下前往扬州。",
            "孤帆远影碧空尽：一叶孤舟的影子渐渐消失在碧蓝的天空尽头。",
            "唯见长江天际流：只能看见浩浩荡荡的长江水流向天际。"
        });
        annotationData.put("水调歌头·明月几时有", new String[]{
            "丙辰中秋：宋神宗熙宁九年（1076年）的中秋节。",
            "把酒问青天：举起酒杯询问苍天。",
            "天上宫阙：天上的宫殿。",
            "何似在人间：哪里比得上在人间。",
            "朱阁：华丽的楼阁。",
            "绮户：雕花的窗户。",
            "千里共婵娟：虽然相隔千里，却能共享这美好的月光。"
        });
    }

    private void initTranslationData() {
        translationData.put("静夜思", new String[]{
            "明亮的月光洒在床前，好像地上泛起了一层白霜。",
            "我禁不住抬起头来，看那天窗外的一轮明月，不由得低头沉思，想起远方的家乡。"
        });
        translationData.put("春望", new String[]{
            "长安沦陷了，但山河依旧存在；春天来了，长安城中草木丛生。",
            "感伤时局，看到花开也流泪；痛恨离别，听到鸟鸣也惊心。",
            "战火已经连续了三个月，一封家信价值万金。",
            "满头的白发越搔越短，简直连簪子也插不住了。"
        });
        translationData.put("登鹳雀楼", new String[]{
            "夕阳依傍着西山慢慢落下，滔滔黄河朝着大海汹涌奔流。",
            "想要看到千里之外的风光，那就要再登上更高的一层城楼。"
        });
        translationData.put("水调歌头·明月几时有", new String[]{
            "丙辰年的中秋节，我欢饮直到天亮，喝得大醉。写下了这首词，同时怀念我的弟弟子由。",
            "明月从什么时候开始有的？我举起酒杯询问青天。不知道天上的宫殿里，今晚是哪一年哪一日的良辰？",
            "我想要乘着清风回到天上去，又恐怕在美玉砌成的楼宇里，经受不住高处的寒冷。",
            "翩翩起舞玩赏着月下清影，哪里比得上在人间。",
            "月儿转过朱红色的楼阁，低低地挂在雕花的窗户上，照着没有睡意的自己。",
            "明月不应该对人们有什么怨恨吧？为什么偏在人们离别时才圆呢？",
            "人有悲欢离合的变迁，月有阴晴圆缺的转换，这种事自古来难以周全。",
            "只希望自己思念的人平安长久，不管相隔千山万水，都可以一起看到明月皎洁美好的样子。"
        });
    }

    private void initBackgroundData() {
        backgroundData.put("静夜思", new String[]{
            "这首诗写于李白26岁时，当时李白客居扬州旅舍。在一个明月当空的夜晚，诗人仰望天空中的明月，不由得想起了自己的故乡，于是写下了这首流传千古的思乡之作。"
        });
        backgroundData.put("春望", new String[]{
            "这首诗写于唐肃宗至德二年（757年）三月。当时长安被安史叛军焚掠一空，满目凄凉。诗人眼见山河依旧而国破家亡，春回大地却满城荒凉，触景伤情，写下了这首脍炙人口的佳作。"
        });
        backgroundData.put("黄鹤楼送孟浩然之广陵", new String[]{
            "这首诗写于唐玄宗开元十八年（730年）三月。当时李白在江夏（今湖北武汉）与好友孟浩然重逢，不久孟浩然要乘船东下扬州，李白在黄鹤楼为他送行，写下了这首送别诗。"
        });
        backgroundData.put("水调歌头·明月几时有", new String[]{
            "这首词写于宋神宗熙宁九年（1076年）中秋节。当时苏轼在密州（今山东诸城）任太守，政治上失意，与弟弟苏辙也已有七年没有团聚了。中秋之夜，面对一轮明月，苏轼心潮起伏，写下了这首千古名篇。"
        });
        backgroundData.put("登高", new String[]{
            "这首诗写于唐代宗大历二年（767年）秋天。当时杜甫流寓夔州（今重庆奉节），重阳节独自登高，面对秋景，联想到自己漂泊无依的处境，写下了这首被誉为'古今七律第一'的佳作。"
        });
    }

    private void initEmotionData() {
        emotionData.put("静夜思", new String[]{
            "表达了诗人客居他乡时对故乡和亲人的深深思念之情。"
        });
        emotionData.put("春望", new String[]{
            "表达了诗人对国家沦陷的沉痛、对家人安危的牵挂，以及自己衰老的感慨。"
        });
        emotionData.put("登鹳雀楼", new String[]{
            "表达了诗人积极向上的进取精神和开阔的胸襟，寓意着只有站得高才能看得远的人生哲理。"
        });
        emotionData.put("黄鹤楼送孟浩然之广陵", new String[]{
            "表达了诗人对友人的依依惜别之情，同时也展现了对扬州繁华的向往。"
        });
        emotionData.put("水调歌头·明月几时有", new String[]{
            "表达了诗人对弟弟的思念之情，以及面对人生坎坷时的豁达态度。'但愿人长久，千里共婵娟'成为千古传诵的名句。"
        });
        emotionData.put("登高", new String[]{
            "表达了诗人年老多病、漂泊他乡的悲凉心境，以及面对国事日非的忧国忧民之情。"
        });
    }

    private void initAllusionData() {
        allusionData.put("静夜思", new String[]{
            "明月：在中国传统文化中，明月常常象征团圆和思乡。"
        });
        allusionData.put("春望", new String[]{
            "烽火：古代边防报警的烟火，这里指战争。",
            "家书：家信。古代交通不便，家信难得，故有'家书抵万金'之说。"
        });
        allusionData.put("登鹳雀楼", new String[]{
            "鹳雀楼：位于今山西省永济市，因常有鹳雀栖息而得名，是古代著名的登高胜地。"
        });
        allusionData.put("水调歌头·明月几时有", new String[]{
            "水调歌头：词牌名，相传隋炀帝开凿运河时作有《水调歌》，唐人演为大曲，'歌头'是大曲中的开头部分。",
            "婵娟：指月亮，也指美好的样子。"
        });
        allusionData.put("黄鹤楼送孟浩然之广陵", new String[]{
            "黄鹤楼：位于今湖北武汉蛇山之巅，自古为江南名楼，传说有仙人乘黄鹤于此。",
            "广陵：今江苏扬州。"
        });
        allusionData.put("登高", new String[]{
            "登高：重阳节习俗，古人认为九月九日登高可以避灾。",
            "落木：落叶。《楚辞·九歌》中有'袅袅兮秋风，洞庭波兮木叶下'之句。"
        });
    }

    public void enhancePoemDetails() {
        System.out.println("开始丰富诗词详细信息...");
        int updatedCount = 0;

        // 更新注解
        updatedCount += updateField("annotation", annotationData);
        // 更新译文
        updatedCount += updateField("translation", translationData);
        // 更新创作背景
        updatedCount += updateField("background", backgroundData);
        // 更新情感分析
        updatedCount += updateField("emotion", emotionData);
        // 更新典故
        updatedCount += updateField("allusion", allusionData);

        System.out.println("诗词详细信息丰富完成，共更新了 " + updatedCount + " 条记录");
    }

    private int updateField(String fieldName, Map<String, String[]> data) {
        int count = 0;
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            String title = entry.getKey();
            String[] values = entry.getValue();
            String content = String.join("\n", values);

            // 使用SQL直接更新，避免加载整个实体
            int result = poemMapper.update(null, 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Poem>()
                    .set(fieldName, content)
                    .eq("title", title)
                    .isNull(fieldName)  // 只更新该字段为空的记录
            );
            
            if (result > 0) {
                count++;
                System.out.println("更新了诗词《" + title + "》的" + fieldName);
            }
        }
        return count;
    }
}
