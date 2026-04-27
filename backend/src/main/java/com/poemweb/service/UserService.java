package com.poemweb.service;

import com.poemweb.entity.User;
import com.poemweb.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    // 用户注册
    public User register(String username, String password, String nickname, String email, String phone) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    // 用户登录
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());
        user.setLastLoginTime(LocalDateTime.now());

        return user;
    }

    // 更新用户信息
    public User updateUser(Long userId, String nickname, String email, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        return user;
    }

    // 修改密码
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return true;
    }

    // 保存用户信息到Session
    public void saveUserToSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("nickname", user.getNickname());
        session.setAttribute("role", user.getRole());
    }

    // 从Session获取用户信息
    public User getUserFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    // 退出登录
    public void logout(HttpSession session) {
        session.invalidate();
    }

    // 检查是否已登录
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }
}
