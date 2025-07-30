package com.wayble.server.admin.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Value("${spring.admin.username}")
    private String adminUsername;
    
    @Value("${spring.admin.password}")
    private String adminPassword;

    @GetMapping("")
    public String adminLoginPage(HttpSession session, Model model) {
        // 이미 로그인된 경우 대시보드로 리다이렉트
        if (session.getAttribute("adminLoggedIn") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminUsername", username);
            log.info("관리자 로그인 성공: {}", username);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "잘못된 사용자명 또는 비밀번호입니다.");
            log.warn("관리자 로그인 실패 시도: {}", username);
            return "admin/login";
        }
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
        return "admin/dashboard";
    }

    @PostMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        log.info("관리자 로그아웃");
        return "redirect:/admin";
    }
}