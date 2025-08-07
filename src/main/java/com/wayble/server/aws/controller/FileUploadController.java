package com.wayble.server.aws.controller;

import com.wayble.server.aws.AmazonS3Manager;
import com.wayble.server.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final AmazonS3Manager amazonS3Manager;

    @PostMapping("/images")
    public CommonResponse<List<String>> uploadImageFiles(
            @RequestParam("images") List<MultipartFile> files
    ) {
        List<String> imageUrls = files.stream()
                .map(amazonS3Manager::uploadFile)
                .toList();

        return CommonResponse.success(imageUrls);
    }

}
