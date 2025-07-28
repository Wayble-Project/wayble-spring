package com.wayble.server.user.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoDto {
    private Long id;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}