package com.wayble.server.aws.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "uuid_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UuidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String fileName;

    private String s3Url;

    private String contentType;

    private Long fileSize;

    private String uploadedAt;
}
