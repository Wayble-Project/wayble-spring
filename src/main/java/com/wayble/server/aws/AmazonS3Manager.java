package com.wayble.server.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wayble.server.aws.repository.UuidRepository;
import com.wayble.server.common.config.AmazonConfig;
import com.wayble.server.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager{

    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final UuidRepository uuidRepository;

    public String uploadFile(MultipartFile file){
        String fileName = createFileName(file);
        String fileUrl = getFileUrl(fileName);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            throw new ApplicationException(AwsErrorCase.IMAGE_UPLOAD_ERROR);
        }

        log.info("file url : " + fileUrl + " has upload to S3");
        return fileUrl;
    }

    public void deleteImageFileFromS3(String imageUrl) {
        String splitStr = ".com/";
        String fileName = imageUrl.substring(imageUrl.lastIndexOf(splitStr) + splitStr.length());

        log.info("file url : " + imageUrl + " has removed from S3");

        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    private String createFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 추출

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")); // 날짜 기반 폴더
        String randomFileName = UUID.randomUUID().toString(); // 랜덤 UUID

        // 예: image/2025/02/12/랜덤값.png
        return "image/" + datePath + "/" + randomFileName + extension;
    }


    private String getFileUrl(String fileName) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }
}
