package com.startup.campusmate.global.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final String provider;
    private final String providerId;
    private final String name;
    private final String email;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(String provider, String providerId, String name, String email,
                            Map<String, Object> attributes,
                            Collection<? extends GrantedAuthority> authorities) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public String getEmail() { return email; }
    @Override public String getName() { return name; }
    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
}
