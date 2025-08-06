package com.wayble.server.logging.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "user_action_logs")
public class UserActionLog {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Long)
    private Long userId;
    
    @Field(type = FieldType.Keyword)
    private String action;
    
    @Field(type = FieldType.Text)
    private String userAgent;
    
    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
    
    @Field(type = FieldType.Keyword)
    private String loginType;
    
    @Field(type = FieldType.Keyword)
    private String userType;
    
    public enum ActionType {
        USER_REGISTER,
        USER_TOKEN_REFRESH
    }
}