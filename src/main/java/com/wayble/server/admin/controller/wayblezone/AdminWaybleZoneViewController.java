package com.wayble.server.admin.controller.wayblezone;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneCreateDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZonePageDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneUpdateDto;
import com.wayble.server.admin.service.AdminWaybleZoneService;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        return "admin/wayblezone/wayble-zones";
    }

    @GetMapping("/{id}")
    public String getWaybleZoneDetail(HttpSession session, Model model, @PathVariable Long id) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }

        Optional<AdminWaybleZoneDetailDto> waybleZoneOpt = adminWaybleZoneService.findWaybleZoneById(id);
        if (waybleZoneOpt.isEmpty()) {
            return "redirect:/admin/wayblezone/wayble-zones?error=notfound";
        }

        model.addAttribute("waybleZone", waybleZoneOpt.get());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));

        log.debug("웨이블존 상세 조회 - ID: {}", id);

        return "admin/wayblezone/wayble-zone-detail";
    }
    
    @GetMapping("/create")
    public String createWaybleZoneForm(HttpSession session, Model model) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        model.addAttribute("createDto", new AdminWaybleZoneCreateDto(
            "", "", null, "", "", "", "", "", null, null, ""
        ));
        model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
        
        return "admin/wayblezone/wayble-zone-create";
    }
    
    @PostMapping("/create")
    public String createWaybleZone(HttpSession session, 
                                  @Valid @ModelAttribute("createDto") AdminWaybleZoneCreateDto createDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
            model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
            return "admin/wayblezone/wayble-zone-create";
        }
        
        try {
            Long waybleZoneId = adminWaybleZoneService.createWaybleZone(createDto);
            redirectAttributes.addFlashAttribute("successMessage", "웨이블존이 성공적으로 생성되었습니다.");
            return "redirect:/admin/wayble-zones/" + waybleZoneId;
        } catch (Exception e) {
            log.error("웨이블존 생성 실패", e);
            model.addAttribute("errorMessage", "웨이블존 생성에 실패했습니다: " + e.getMessage());
            model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
            model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
            return "admin/wayblezone/wayble-zone-create";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String editWaybleZoneForm(HttpSession session, Model model, @PathVariable Long id) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        Optional<AdminWaybleZoneDetailDto> waybleZoneOpt = adminWaybleZoneService.findWaybleZoneById(id);
        if (waybleZoneOpt.isEmpty()) {
            return "redirect:/admin/wayble-zones?error=notfound";
        }
        
        AdminWaybleZoneDetailDto waybleZone = waybleZoneOpt.get();
        AdminWaybleZoneUpdateDto updateDto = AdminWaybleZoneUpdateDto.fromDetailDto(waybleZone);
        
        model.addAttribute("updateDto", updateDto);
        model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
        
        return "admin/wayblezone/wayble-zone-edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateWaybleZone(HttpSession session, 
                                  @PathVariable Long id,
                                  @Valid @ModelAttribute("updateDto") AdminWaybleZoneUpdateDto updateDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
            model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
            return "admin/wayblezone/wayble-zone-edit";
        }
        
        // DTO의 ID와 URL의 ID 일치 확인
        AdminWaybleZoneUpdateDto validatedDto = new AdminWaybleZoneUpdateDto(
            id,
            updateDto.zoneName(),
            updateDto.contactNumber(),
            updateDto.zoneType(),
            updateDto.state(),
            updateDto.city(),
            updateDto.district(),
            updateDto.streetAddress(),
            updateDto.detailAddress(),
            updateDto.latitude(),
            updateDto.longitude(),
            updateDto.mainImageUrl()
        );
        
        try {
            Long waybleZoneId = adminWaybleZoneService.updateWaybleZone(validatedDto);
            redirectAttributes.addFlashAttribute("successMessage", "웨이블존이 성공적으로 수정되었습니다.");
            return "redirect:/admin/wayble-zones/" + waybleZoneId;
        } catch (Exception e) {
            log.error("웨이블존 수정 실패", e);
            model.addAttribute("errorMessage", "웨이블존 수정에 실패했습니다: " + e.getMessage());
            model.addAttribute("waybleZoneTypes", WaybleZoneType.values());
            model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
            return "admin/wayblezone/wayble-zone-edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteWaybleZone(HttpSession session, 
                                  @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        // 로그인 확인
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin";
        }
        
        try {
            adminWaybleZoneService.deleteWaybleZone(id);
            redirectAttributes.addFlashAttribute("successMessage", "웨이블존이 성공적으로 삭제되었습니다.");
            log.info("웨이블존 삭제 완료 - ID: {}, 관리자: {}", id, session.getAttribute("adminUsername"));
            return "redirect:/admin/wayble-zones";
        } catch (Exception e) {
            log.error("웨이블존 삭제 실패 - ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "웨이블존 삭제에 실패했습니다: " + e.getMessage());
            return "redirect:/admin/wayble-zones/" + id;
        }
    }
}