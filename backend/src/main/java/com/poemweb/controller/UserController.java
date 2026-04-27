package com.poemweb.controller;

import com.poemweb.entity.User;
import com.poemweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            String nickname = params.get("nickname");
            String email = params.get("email");
            String phone = params.get("phone");

            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return result;
            }
            if (password == null || password.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }

            User user = userService.register(username, password, nickname, email, phone);
            userService.saveUserToSession(session, user);
            
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("data", buildUserInfo(user));
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 用户登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");

            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return result;
            }
            if (password == null || password.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }

            User user = userService.login(username, password);
            userService.saveUserToSession(session, user);
            
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", buildUserInfo(user));
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 用户登出
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        userService.logout(session);
        result.put("success", true);
        result.put("message", "登出成功");
        return result;
    }

    // 获取当前用户信息
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserFromSession(session);
        if (user != null) {
            result.put("success", true);
            result.put("data", buildUserInfo(user));
        } else {
            result.put("success", false);
            result.put("message", "未登录");
        }
        return result;
    }

    // 检查登录状态
    @GetMapping("/check")
    public Map<String, Object> checkLogin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean loggedIn = userService.isLoggedIn(session);
        result.put("success", true);
        result.put("loggedIn", loggedIn);
        if (loggedIn) {
            User user = userService.getUserFromSession(session);
            result.put("data", buildUserInfo(user));
        }
        return result;
    }

    // 更新用户信息
    @PostMapping("/update")
    public Map<String, Object> updateUser(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            String nickname = params.get("nickname");
            String email = params.get("email");
            String phone = params.get("phone");

            User user = userService.updateUser(userId, nickname, email, phone);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("data", buildUserInfo(user));
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 修改密码
    @PostMapping("/changePassword")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }

            userService.changePassword(userId, oldPassword, newPassword);
            result.put("success", true);
            result.put("message", "密码修改成功");
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("role", user.getRole());
        info.put("status", user.getStatus());
        info.put("lastLoginTime", user.getLastLoginTime());
        return info;
    }
}
