package com.wayble.server.admin.controller;

import com.wayble.server.admin.dto.log.ErrorLogDto;
import com.wayble.server.admin.service.LogService;
import com.wayble.server.admin.service.LogService.ErrorLogStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final LogService logService;

    /**
     * 에러 로그 관리 페이지
     */
    @GetMapping("/error")
    public String errorLogPage(
            @RequestParam(defaultValue = "100") int limit,
            Model model
    ) {
        List<ErrorLogDto> errorLogs = logService.getRecentErrorLogs(limit);
        ErrorLogStats stats = logService.getErrorLogStats();
        
        model.addAttribute("errorLogs", errorLogs);
        model.addAttribute("stats", stats);
        model.addAttribute("limit", limit);
        
        return "admin/log/error-logs";
    }

    /**
     * 에러 로그 Ajax 조회
     */
    @GetMapping("/error/data")
    @ResponseBody
    public List<ErrorLogDto> getErrorLogs(@RequestParam(defaultValue = "100") int limit) {
        return logService.getRecentErrorLogs(limit);
    }

    /**
     * 에러 로그 통계 Ajax 조회
     */
    @GetMapping("/error/stats")
    @ResponseBody
    public ErrorLogStats getErrorLogStats() {
        return logService.getErrorLogStats();
    }
}