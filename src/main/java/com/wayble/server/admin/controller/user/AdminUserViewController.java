package com.wayble.server.admin.controller.user;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserPageDto;
import com.wayble.server.admin.service.AdminUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserViewController {
    
    private final AdminUserService adminUserService;

    @GetMapping
    public String getUsers(HttpSession session, Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "100") int size) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        // 페이징 데이터 조회
        AdminUserPageDto pageData = adminUserService.getUsersWithPaging(page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("사용자 목록 조회 - 페이지: {}, 전체: {}", page, pageData.totalElements());

        return "admin/user/users";
    }

    @GetMapping("/{id}")
    public String getUserDetail(HttpSession session, Model model, @PathVariable Long id) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        Optional<AdminUserDetailDto> userOpt = adminUserService.findUserById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/user/users?error=notfound";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("사용자 상세 조회 - ID: {}", id);

        return "admin/user/user-detail";
    }
    
    @GetMapping("/deleted")
    public String getDeletedUsers(HttpSession session, Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "100") int size) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        // 페이징 데이터 조회
        AdminUserPageDto pageData = adminUserService.getDeletedUsersWithPaging(page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("삭제된 사용자 목록 조회 - 페이지: {}, 전체: {}", page, pageData.totalElements());

        return "admin/user/deleted-users";
    }

    @GetMapping("/deleted/{id}")
    public String getDeletedUserDetail(HttpSession session, Model model, @PathVariable Long id) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        Optional<AdminUserDetailDto> userOpt = adminUserService.findDeletedUserById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users/deleted?error=notfound";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("삭제된 사용자 상세 조회 - ID: {}", id);

        return "admin/user/deleted-user-detail";
    }
    
    @PostMapping("/deleted/{id}/restore")
    public String restoreUser(HttpSession session, 
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        try {
            adminUserService.restoreUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "사용자가 성공적으로 복원되었습니다.");
            log.info("사용자 복원 완료 - ID: {}, 관리자: {}", id, session.getAttribute("adminUsername"));
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            log.error("사용자 복원 실패 - ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 복원에 실패했습니다: " + e.getMessage());
            return "redirect:/admin/users/deleted/" + id;
        }
    }
}