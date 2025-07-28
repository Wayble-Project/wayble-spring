package com.wayble.server.common.config.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

public class JwtAuthentication extends AbstractAuthenticationToken {
    private final Long userId;


    public JwtAuthentication(Long userId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override public Object getCredentials() { return ""; }
    @Override public Object getPrincipal() {  return userId; }
}