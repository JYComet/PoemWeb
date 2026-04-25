package com.poemweb.controller;

import com.poemweb.service.TrajectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/poet")
public class TrajectoryController {
    @Autowired
    private TrajectoryService trajectoryService;

    // 诗人行迹：作品数量排行、朝代分布、指定诗人的题材分布
    @GetMapping("/trajectory")
    public Map<String, Object> poetTrajectory(
            @RequestParam(value = "author", defaultValue = "") String author
    ) {
        Map<String, Object> trajectoryData = trajectoryService.getPoetTrajectory(author);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", trajectoryData);
        return result;
    }

    // 诗人行迹地图：地点变动、时间、该地创作的诗词
    @GetMapping("/trajectory/map")
    public Map<String, Object> poetTrajectoryMap(
            @RequestParam(value = "author", defaultValue = "") String author
    ) {
        Map<String, Object> mapData = trajectoryService.getPoetTrajectoryMap(author);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", mapData);
        return result;
    }
}