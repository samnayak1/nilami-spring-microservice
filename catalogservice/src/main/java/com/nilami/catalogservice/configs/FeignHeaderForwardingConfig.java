package com.nilami.catalogservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignHeaderForwardingConfig {

    @Bean
    public RequestInterceptor headerForwardingInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                    HttpServletRequest request = servletRequestAttributes.getRequest();

           
                    String userId = request.getHeader("X-User-Id");
                    String roles = request.getHeader("X-User-Roles");

                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }
                    if (roles != null) {
                        template.header("X-User-Roles", roles);
                    }
                }
            }
        };
    }
}