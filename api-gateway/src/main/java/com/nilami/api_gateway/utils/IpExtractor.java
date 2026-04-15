package com.nilami.api_gateway.utils;
import java.net.InetSocketAddress;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class IpExtractor {
    public String extract(ServerHttpRequest request) {

        //if sitting behind a load balancer or reverse proxy, the client's IP might be in the X-Forwarded-For header
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
}