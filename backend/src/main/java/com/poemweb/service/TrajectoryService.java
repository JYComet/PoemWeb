package com.poemweb.service;

import com.poemweb.entity.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TrajectoryService {
    @Autowired
    private PoemService poemService;

    // 获取诗人行迹数据
    public Map<String, Object> getPoetTrajectory(String author) {
        List<Poem> poems = poemService.getAllPoems();

        // 诗人作品数量排行（Top 20）
        Map<String, Integer> authorCount = new HashMap<>();
        for (Poem p : poems) {
            authorCount.put(p.getAuthor(), authorCount.getOrDefault(p.getAuthor(), 0) + 1);
        }

        List<AuthorRank> authorRank = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : authorCount.entrySet()) {
            AuthorRank rank = new AuthorRank();
            rank.setName(entry.getKey());
            rank.setValue(entry.getValue());
            authorRank.add(rank);
        }
        authorRank.sort((a, b) -> b.getValue() - a.getValue());
        if (authorRank.size() > 20) {
            authorRank = authorRank.subList(0, 20);
        }

        // 各朝代诗词分布
        Map<String, Integer> dynastyCount = new HashMap<>();
        for (Poem p : poems) {
            dynastyCount.put(p.getDynasty(), dynastyCount.getOrDefault(p.getDynasty(), 0) + 1);
        }

        List<DynastyDist> dynastyDist = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dynastyCount.entrySet()) {
            DynastyDist dist = new DynastyDist();
            dist.setName(entry.getKey());
            dist.setValue(entry.getValue());
            dynastyDist.add(dist);
        }
        dynastyDist.sort((a, b) -> b.getValue() - a.getValue());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("authorRank", authorRank);
        result.put("dynastyDist", dynastyDist);

        // 若指定诗人，返回该诗人的题材分布（创作轨迹）
        if (author != null && !author.isEmpty()) {
            List<Poem> poetPoems = new ArrayList<>();
            for (Poem p : poems) {
                if (p.getAuthor().equals(author)) {
                    poetPoems.add(p);
                }
            }

            Map<String, Integer> tagCount = new HashMap<>();
            for (Poem p : poetPoems) {
                String tag = p.getTag() != null ? p.getTag() : "其他";
                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
            }

            List<TagDist> tagDistList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : tagCount.entrySet()) {
                TagDist dist = new TagDist();
                dist.setName(entry.getKey());
                dist.setValue(entry.getValue());
                tagDistList.add(dist);
            }
            tagDistList.sort((a, b) -> b.getValue() - a.getValue());

            PoetTagDist poetTagDist = new PoetTagDist();
            poetTagDist.setAuthor(author);
            poetTagDist.setDynasty(poetPoems.isEmpty() ? "" : poetPoems.get(0).getDynasty());
            poetTagDist.setTotal(poetPoems.size());
            poetTagDist.setTagDist(tagDistList);

            result.put("poetTagDist", poetTagDist);

            // 诗人作品列表
            List<PoetPoem> poetPoemsList = new ArrayList<>();
            for (Poem p : poetPoems.subList(0, Math.min(30, poetPoems.size()))) {
                PoetPoem poem = new PoetPoem();
                poem.setTitle(p.getTitle());
                poem.setTag(p.getTag());
                poetPoemsList.add(poem);
            }
            result.put("poetPoems", poetPoemsList);
        }

        return result;
    }

    // 诗人行迹地图数据
    public Map<String, Object> getPoetTrajectoryMap(String author) {
        // 这里简化实现，实际项目中可以从数据库或配置文件中读取诗人行迹数据
        Map<String, Object> result = new HashMap<>();
        result.put("poets", Arrays.asList("李白", "杜甫", "苏轼", "白居易"));

        if (author == null || author.isEmpty()) {
            result.put("trajectory", null);
            result.put("message", "请选择诗人查看行迹地图");
            return result;
        }

        // 模拟数据
        List<Point> points = new ArrayList<>();
        if (author.equals("李白")) {
            points.add(new Point(116.4074, 39.9042, "北京", 726, Arrays.asList("《登幽州台歌》")));
            points.add(new Point(121.4737, 31.2304, "上海", 730, Arrays.asList("《望庐山瀑布》")));
            points.add(new Point(118.7837, 32.0584, "南京", 735, Arrays.asList("《黄鹤楼送孟浩然之广陵》")));
        } else if (author.equals("杜甫")) {
            points.add(new Point(104.0668, 30.5728, "成都", 759, Arrays.asList("《茅屋为秋风所破歌》")));
            points.add(new Point(112.9834, 28.1941, "长沙", 768, Arrays.asList("《登高》")));
        } else if (author.equals("苏轼")) {
            points.add(new Point(120.1551, 30.2741, "杭州", 1071, Arrays.asList("《饮湖上初晴后雨》")));
            points.add(new Point(30.6723, 104.0665, "眉山", 1037, Arrays.asList("《念奴娇·赤壁怀古》")));
        } else if (author.equals("白居易")) {
            points.add(new Point(112.9348, 28.2283, "长沙", 820, Arrays.asList("《长恨歌》")));
            points.add(new Point(115.8921, 28.6764, "南昌", 815, Arrays.asList("《琵琶行》")));
        }

        // 生成路线
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Line line = new Line();
            line.setFrom(Arrays.asList(points.get(i).getLng(), points.get(i).getLat()));
            line.setTo(Arrays.asList(points.get(i + 1).getLng(), points.get(i + 1).getLat()));
            line.setYearFrom(points.get(i).getYear());
            line.setYearTo(points.get(i + 1).getYear());
            lines.add(line);
        }

        result.put("author", author);
        result.put("trajectory", points);
        result.put("lines", lines);

        return result;
    }

    // 作者排行
    public static class AuthorRank {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // 朝代分布
    public static class DynastyDist {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // 诗人题材分布
    public static class PoetTagDist {
        private String author;
        private String dynasty;
        private int total;
        private List<TagDist> tagDist;

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getDynasty() {
            return dynasty;
        }

        public void setDynasty(String dynasty) {
            this.dynasty = dynasty;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<TagDist> getTagDist() {
            return tagDist;
        }

        public void setTagDist(List<TagDist> tagDist) {
            this.tagDist = tagDist;
        }
    }

    // 题材分布
    public static class TagDist {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // 诗人作品
    public static class PoetPoem {
        private String title;
        private String tag;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    // 地图点
    public static class Point {
        private double lng;
        private double lat;
        private String place;
        private int year;
        private String desc;
        private List<String> poems;
        private List<PoemInfo> poemsDetail;

        public Point(double lng, double lat, String place, int year, List<String> poems) {
            this(lng, lat, place, year, poems, "");
        }

        public Point(double lng, double lat, String place, int year, List<String> poems, String desc) {
            this.lng = lng;
            this.lat = lat;
            this.place = place;
            this.year = year;
            this.poems = poems;
            this.desc = desc;
            // 将诗词标题转换为PoemInfo对象
            this.poemsDetail = new ArrayList<>();
            if (poems != null) {
                for (String title : poems) {
                    this.poemsDetail.add(new PoemInfo(title));
                }
            }
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public List<String> getPoems() {
            return poems;
        }

        public void setPoems(List<String> poems) {
            this.poems = poems;
        }

        public List<PoemInfo> getPoemsDetail() {
            return poemsDetail;
        }

        public void setPoemsDetail(List<PoemInfo> poemsDetail) {
            this.poemsDetail = poemsDetail;
        }
    }

    // 诗词信息（用于地图详情面板）
    public static class PoemInfo {
        private String title;
        private String content;

        public PoemInfo(String title) {
            // 去除书名号
            this.title = title.replace("《", "").replace("》", "");
            this.content = "";
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // 地图线
    public static class Line {
        private List<Double> from;
        private List<Double> to;
        private int yearFrom;
        private int yearTo;

        public List<Double> getFrom() {
            return from;
        }

        public void setFrom(List<Double> from) {
            this.from = from;
        }

        public List<Double> getTo() {
            return to;
        }

        public void setTo(List<Double> to) {
            this.to = to;
        }

        public int getYearFrom() {
            return yearFrom;
        }

        public void setYearFrom(int yearFrom) {
            this.yearFrom = yearFrom;
        }

        public int getYearTo() {
            return yearTo;
        }

        public void setYearTo(int yearTo) {
            this.yearTo = yearTo;
        }
    }
}