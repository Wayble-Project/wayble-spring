package com.wayble.server.admin.controller;

import com.wayble.server.admin.dto.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.AdminWaybleZonePageDto;
import com.wayble.server.admin.service.AdminWaybleZoneService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/wayble-zones")
@RequiredArgsConstructor
public class AdminWaybleZoneViewController {
    
    private final AdminWaybleZoneService adminWaybleZoneService;

    @GetMapping
    public String getWaybleZones(HttpSession session, Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "100") int size) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        // 페이징 데이터 조회
        AdminWaybleZonePageDto pageData = adminWaybleZoneService.getWaybleZonesWithPaging(page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("웨이블존 목록 조회 - 페이지: {}, 전체: {}", page, pageData.totalElements());

        return "admin/wayble-zones";
    }

    @GetMapping("/{id}")
    public String getWaybleZoneDetail(HttpSession session, Model model, @PathVariable Long id) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        Optional<AdminWaybleZoneDetailDto> waybleZoneOpt = adminWaybleZoneService.findWaybleZoneById(id);
        if (waybleZoneOpt.isEmpty()) {
            return "redirect:/admin/wayble-zones?error=notfound";
        }

        model.addAttribute("waybleZone", waybleZoneOpt.get());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("웨이블존 상세 조회 - ID: {}", id);

        return "admin/wayble-zone-detail";
    }
}