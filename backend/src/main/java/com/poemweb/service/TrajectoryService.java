package com.poemweb.service;

import com.poemweb.entity.GraphEdge;
import com.poemweb.entity.GraphNode;
import com.poemweb.entity.PoetTrajectory;
import com.poemweb.entity.Poem;
import com.poemweb.mapper.PoetTrajectoryMapper;
import com.poemweb.mapper.GraphNodeMapper;
import com.poemweb.mapper.GraphEdgeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class TrajectoryService {
    @Autowired
    private PoemService poemService;
    
    @Autowired
    private PoetTrajectoryMapper trajectoryMapper;
    
    @Autowired
    private GraphNodeMapper graphNodeMapper;
    
    @Autowired
    private GraphEdgeMapper graphEdgeMapper;

    public Map<String, Object> getPoetTrajectory(String author) {
        List<Poem> poems = poemService.getAllPoems();

        Map<String, Integer> authorCount = new HashMap<>();
        for (Poem p : poems) {
            String authorName = p.getAuthor();
            if (authorName != null && !authorName.isEmpty()) {
                authorCount.put(authorName, authorCount.getOrDefault(authorName, 0) + 1);
            }
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

        Map<String, Integer> dynastyCount = new HashMap<>();
        for (Poem p : poems) {
            String dynasty = p.getDynasty();
            if (dynasty != null && !dynasty.isEmpty()) {
                dynastyCount.put(dynasty, dynastyCount.getOrDefault(dynasty, 0) + 1);
            }
        }

        List<DynastyDist> dynastyDist = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dynastyCount.entrySet()) {
            DynastyDist dist = new DynastyDist();
            dist.setName(entry.getKey());
            dist.setValue(entry.getValue());
            dynastyDist.add(dist);
        }
        dynastyDist.sort((a, b) -> b.getValue() - a.getValue());

        Map<String, Object> result = new HashMap<>();
        result.put("authorRank", authorRank);
        result.put("dynastyDist", dynastyDist);

        if (author != null && !author.isEmpty()) {
            List<Poem> poetPoems = new ArrayList<>();
            for (Poem p : poems) {
                if (p.getAuthor() != null && p.getAuthor().equals(author)) {
                    poetPoems.add(p);
                }
            }

            Map<String, Integer> tagCount = new HashMap<>();
            for (Poem p : poetPoems) {
                String tag = p.getTag() != null ? p.getTag() : "Other";
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

    public Map<String, Object> getPoetTrajectoryMap(String author) {
        List<String> poetsWithTrajectory = trajectoryMapper.selectDistinctPoetNames();

        Map<String, Object> result = new HashMap<>();
        result.put("poets", poetsWithTrajectory);

        if (author == null || author.isEmpty()) {
            result.put("trajectory", null);
            result.put("message", "Please select a poet to view trajectory map");
            return result;
        }

        List<PoetTrajectory> trajectories = trajectoryMapper.selectByPoetNameOrderByYearAsc(author);
        
        if (trajectories.isEmpty()) {
            result.put("trajectory", null);
            result.put("message", "No trajectory data for this poet");
            return result;
        }

        List<Point> points = new ArrayList<>();
        for (PoetTrajectory t : trajectories) {
            List<String> poemsList = new ArrayList<>();
            if (t.getPoems() != null && !t.getPoems().isEmpty()) {
                poemsList = Arrays.asList(t.getPoems().split(","));
            }
            Point point = new Point(t.getLongitude(), t.getLatitude(), t.getLocation(), 
                                   t.getYear() != null ? t.getYear() : 0, poemsList, 
                                   t.getDescription());
            points.add(point);
        }

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

    public void initTrajectoryFromGraph() {
        trajectoryMapper.delete(null);
        System.out.println("Initializing poet trajectory data...");

        List<TrajectoryData> allData = new ArrayList<>();

        allData.add(new TrajectoryData("\u674E\u767D", "\u957F\u5B89", 108.94, 34.34, 742, "\u5165\u957F\u5B89\uFF0C\u4F9B\u5949\u7FF0\u6797", Arrays.asList("\u6E05\u5E73\u8C03", "\u8700\u9053\u96BE")));
        allData.add(new TrajectoryData("\u674E\u767D", "\u91D1\u9675", 118.78, 32.06, 726, "\u6E38\u5386\u91D1\u9675", Arrays.asList("\u767B\u91D1\u9675\u51E4\u51F0\u53F0", "\u591C\u6CCA\u725B\u6E1A\u6000\u53E4")));
        allData.add(new TrajectoryData("\u674E\u767D", "\u6B66\u6C49", 114.31, 30.52, 735, "\u6E38\u5386\u6C5F\u590F", Arrays.asList("\u9EC4\u9E64\u697C\u9001\u5B5F\u6D69\u7136\u4E4B\u5E7F\u9675")));
        allData.add(new TrajectoryData("\u674E\u767D", "\u6210\u90FD", 104.07, 30.67, 755, "\u6E38\u5386\u8700\u5730", Arrays.asList("\u4E0A\u7687\u897F\u5DE1\u5357\u4EAC\u6B4C")));
        allData.add(new TrajectoryData("\u674E\u767D", "\u5CB3\u9633", 113.09, 29.37, 759, "\u6E38\u5386\u5CB3\u9633", Arrays.asList("\u4E0E\u590F\u5341\u4E8C\u767B\u5CB3\u9633\u697C")));

        allData.add(new TrajectoryData("\u675C\u752B", "\u957F\u5B89", 108.94, 34.34, 746, "\u56F0\u5B88\u957F\u5B89\u5341\u5E74", Arrays.asList("\u6625\u671B", "\u6708\u591C")));
        allData.add(new TrajectoryData("\u675C\u752B", "\u6210\u90FD", 104.07, 30.67, 759, "\u5B9A\u5C45\u6210\u90FD\u8349\u5802", Arrays.asList("\u8305\u5C4B\u4E3A\u79CB\u98CE\u6240\u7834\u6B4C", "\u6625\u591C\u559C\u96E8")));
        allData.add(new TrajectoryData("\u675C\u752B", "\u957F\u6C99", 112.94, 28.23, 768, "\u665A\u5E74\u6D41\u5BD3\u6E56\u6E58", Arrays.asList("\u6C5F\u5357\u9022\u674E\u9F9F\u5E74")));
        allData.add(new TrajectoryData("\u675C\u752B", "\u5CB3\u9633", 113.09, 29.37, 768, "\u767B\u5CB3\u9633\u697C", Arrays.asList("\u767B\u9AD8", "\u767B\u5CB3\u9633\u697C")));
        allData.add(new TrajectoryData("\u675C\u752B", "\u6D1B\u9633", 112.45, 34.62, 758, "\u7ECF\u5386\u5B89\u53F2\u4E4B\u4E71", Arrays.asList("\u77F3\u58D5\u540F")));

        allData.add(new TrajectoryData("\u82CF\u8F7C", "\u7709\u5C71", 103.83, 30.05, 1057, "\u51FA\u751F\u4E8E\u7709\u5C71", Arrays.asList("\u6C5F\u57CE\u5B50")));
        allData.add(new TrajectoryData("\u82CF\u8F7C", "\u676D\u5DDE", 120.15, 30.28, 1071, "\u4EFB\u676D\u5DDE\u901A\u5224", Arrays.asList("\u996E\u6E56\u4E0A\u521D\u6674\u540E\u96E8", "\u516D\u6708\u4E8C\u5341\u4E03\u65E5\u671B\u6E56\u697C\u9189\u4E66")));
        allData.add(new TrajectoryData("\u82CF\u8F7C", "\u6B66\u6C49", 114.31, 30.52, 1082, "\u6E38\u5386\u9EC4\u5DDE\u8D64\u58C1", Arrays.asList("\u5FF5\u5974\u5A07\u00B7\u8D64\u58C1\u6000\u53E4")));
        allData.add(new TrajectoryData("\u82CF\u8F7C", "\u6842\u6797", 110.28, 25.27, 1094, "\u8D2C\u8C2A\u5CAD\u5357", Arrays.asList("\u6842\u6797\u98CE\u571F\u8BB0")));

        allData.add(new TrajectoryData("\u767D\u5C45\u6613", "\u957F\u5B89", 108.94, 34.34, 800, "\u4E2D\u8FDB\u58EB\u540E\u4EFB\u804C\u957F\u5B89", Arrays.asList("\u957F\u6068\u6B4C", "\u7435\u7436\u884C")));
        allData.add(new TrajectoryData("\u767D\u5C45\u6613", "\u676D\u5DDE", 120.15, 30.28, 822, "\u4EFB\u676D\u5DDE\u523A\u53F2", Arrays.asList("\u94B1\u5858\u6E56\u6625\u884C", "\u5FC6\u6C5F\u5357")));
        allData.add(new TrajectoryData("\u767D\u5C45\u6613", "\u5357\u660C", 115.86, 28.68, 815, "\u8D2C\u8C2A\u6C5F\u5DDE", Arrays.asList("\u7435\u7436\u884C")));
        allData.add(new TrajectoryData("\u767D\u5C45\u6613", "\u957F\u6C99", 112.94, 28.23, 820, "\u6E38\u5386\u6E56\u6E58", Arrays.asList("\u8D4B\u5F97\u53E4\u539F\u8349\u9001\u522B")));

        allData.add(new TrajectoryData("\u738B\u7EF4", "\u957F\u5B89", 108.94, 34.34, 721, "\u4EFB\u804C\u957F\u5B89", Arrays.asList("\u9001\u5143\u4E8C\u4F7F\u5B89\u897F", "\u5C71\u5C45\u79CB\u6670")));
        allData.add(new TrajectoryData("\u738B\u7EF4", "\u91CD\u5E86", 106.55, 29.56, 730, "\u6E38\u5386\u8700\u5730", Arrays.asList("\u7AF9\u91CC\u9986")));
        allData.add(new TrajectoryData("\u738B\u7EF4", "\u6CF0\u5C71", 117.13, 36.18, 735, "\u6E38\u5386\u6CF0\u5C71", Arrays.asList("\u7EC8\u5357\u5C71")));

        allData.add(new TrajectoryData("\u5B5F\u6D69\u7136", "\u957F\u6C99", 112.94, 28.23, 725, "\u6E38\u5386\u6E56\u6E58", Arrays.asList("\u6625\u6653")));
        allData.add(new TrajectoryData("\u5B5F\u6D69\u7136", "\u6B66\u6C49", 114.31, 30.52, 730, "\u6E38\u5386\u6C5F\u590F", Arrays.asList("\u5BBF\u5EFA\u5FB7\u6C5F")));
        allData.add(new TrajectoryData("\u5B5F\u6D69\u7136", "\u676D\u5DDE", 120.15, 30.28, 735, "\u6E38\u5386\u5434\u8D8A", Arrays.asList("\u671B\u6D1E\u5EAD\u6E56\u8D60\u5F20\u627F\u76F8")));

        allData.add(new TrajectoryData("\u674E\u5546\u9690", "\u957F\u5B89", 108.94, 34.34, 838, "\u4EFB\u804C\u957F\u5B89", Arrays.asList("\u9526\u745F", "\u591C\u96E8\u5BC4\u5317")));
        allData.add(new TrajectoryData("\u674E\u5546\u9690", "\u6D1B\u9633", 112.45, 34.62, 845, "\u6E38\u5386\u4E1C\u90FD", Arrays.asList("\u65E0\u9898")));
        allData.add(new TrajectoryData("\u674E\u5546\u9690", "\u91D1\u9675", 118.78, 32.06, 850, "\u6E38\u5386\u6C5F\u5357", Arrays.asList("\u5AE6\u5A25")));

        allData.add(new TrajectoryData("\u675C\u7267", "\u957F\u5B89", 108.94, 34.34, 826, "\u4EFB\u804C\u957F\u5B89", Arrays.asList("\u6CCA\u79E6\u6DEF", "\u5C71\u884C")));
        allData.add(new TrajectoryData("\u675C\u7267", "\u6D1B\u9633", 112.45, 34.62, 835, "\u6E38\u5386\u4E1C\u90FD", Arrays.asList("\u6E05\u660E")));
        allData.add(new TrajectoryData("\u675C\u7267", "\u82CF\u5DDE", 120.62, 31.30, 842, "\u4EFB\u804C\u82CF\u5DDE", Arrays.asList("\u6C5F\u5357\u6625")));

        int totalInserted = 0;
        for (TrajectoryData data : allData) {
            PoetTrajectory trajectory = new PoetTrajectory();
            trajectory.setPoetName(data.poetName);
            trajectory.setLocation(data.location);
            trajectory.setLongitude(data.longitude);
            trajectory.setLatitude(data.latitude);
            trajectory.setYear(data.year);
            trajectory.setDescription(data.description);
            trajectory.setPoems(data.poems != null ? String.join(",", data.poems) : "");
            trajectory.setCreateTime(LocalDateTime.now());
            trajectory.setUpdateTime(LocalDateTime.now());
            trajectoryMapper.insert(trajectory);
            totalInserted++;
        }

        System.out.println("Poet trajectory data initialization completed, imported " + totalInserted + " records");
    }

    private static class TrajectoryData {
        String poetName;
        String location;
        double longitude;
        double latitude;
        int year;
        String description;
        List<String> poems;

        TrajectoryData() {}

        TrajectoryData(String poetName, String location, double longitude, double latitude, 
                      int year, String description, List<String> poems) {
            this.poetName = poetName;
            this.location = location;
            this.longitude = longitude;
            this.latitude = latitude;
            this.year = year;
            this.description = description;
            this.poems = poems;
        }
    }

    public static class AuthorRank {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class DynastyDist {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class PoetTagDist {
        private String author;
        private String dynasty;
        private int total;
        private List<TagDist> tagDist;

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getDynasty() { return dynasty; }
        public void setDynasty(String dynasty) { this.dynasty = dynasty; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public List<TagDist> getTagDist() { return tagDist; }
        public void setTagDist(List<TagDist> tagDist) { this.tagDist = tagDist; }
    }

    public static class TagDist {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class PoetPoem {
        private String title;
        private String tag;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
    }

    public static class Point {
        @JsonProperty("lng")
        private double lng;
        @JsonProperty("lat")
        private double lat;
        @JsonProperty("place")
        private String place;
        @JsonProperty("year")
        private int year;
        @JsonProperty("desc")
        private String desc;
        @JsonProperty("poems")
        private List<String> poems;
        @JsonProperty("poemsDetail")
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
            this.poemsDetail = new ArrayList<>();
            if (poems != null) {
                for (String title : poems) {
                    this.poemsDetail.add(new PoemInfo(title));
                }
            }
        }

        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public List<String> getPoems() { return poems; }
        public void setPoems(List<String> poems) { this.poems = poems; }
        public List<PoemInfo> getPoemsDetail() { return poemsDetail; }
        public void setPoemsDetail(List<PoemInfo> poemsDetail) { this.poemsDetail = poemsDetail; }
    }

    public static class PoemInfo {
        @JsonProperty("title")
        private String title;
        @JsonProperty("content")
        private String content;

        public PoemInfo(String title) {
            this.title = title.replace("\u300a", "").replace("\u300b", "");
            this.content = "";
        }

        public PoemInfo(String title, String content) {
            this.title = title.replace("\u300a", "").replace("\u300b", "");
            this.content = content != null ? content : "";
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Line {
        private List<Double> from;
        private List<Double> to;
        private int yearFrom;
        private int yearTo;

        public List<Double> getFrom() { return from; }
        public void setFrom(List<Double> from) { this.from = from; }
        public List<Double> getTo() { return to; }
        public void setTo(List<Double> to) { this.to = to; }
        public int getYearFrom() { return yearFrom; }
        public void setYearFrom(int yearFrom) { this.yearFrom = yearFrom; }
        public int getYearTo() { return yearTo; }
        public void setYearTo(int yearTo) { this.yearTo = yearTo; }
    }
}
