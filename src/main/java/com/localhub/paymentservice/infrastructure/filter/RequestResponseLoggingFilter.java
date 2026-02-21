package com.localhub.paymentservice.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "x-api-key"
    );

    private static final String SENSITIVE_FIELD_PATTERN =
            "(\"(?:password|token|cardNumber|cvv|bankAccount|iban|secret|apiKey)\"\\s*:\\s*)\"[^\"]*\"";

    private static final int MAX_BODY_LOG_LENGTH = 1000;

    private static final String RESET = "\033[0m";
    private static final String CYAN = "\033[36m";
    private static final String MAGENTA = "\033[35m";
    private static final String BRIGHT_YELLOW = "\033[93m";
    private static final String FAINT = "\033[2m";

    @Value("${LOG_COLOR_ENABLED:true}")
    private boolean colorEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            logRequest(wrappedRequest);
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequestBody(wrappedRequest);
            logResponse(wrappedRequest, wrappedResponse, startTime);
            logResponseBody(wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request) {
        String query = request.getQueryString() != null ? request.getQueryString() : "";
        String userId = request.getHeader("X-User-Id") != null ? request.getHeader("X-User-Id") : "anonymous";
        String clientIp = getClientIp(request);
        String headers = getSafeHeaders(request);

        String msg = color("\u2192 ", CYAN)
                + color(request.getMethod() + " " + request.getRequestURI(), CYAN)
                + color(" | ", FAINT) + "query=" + query
                + color(" | ", FAINT) + "userId=" + userId
                + color(" | ", FAINT) + "clientIp=" + clientIp
                + color(" | ", FAINT) + "headers=" + headers;

        log.info("{}", msg);
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        if (!log.isDebugEnabled()) {
            return;
        }

        String method = request.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
            return;
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart")) {
            log.debug("{}", color("  Body: [multipart content]", CYAN));
            return;
        }

        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        body = body.replaceAll(SENSITIVE_FIELD_PATTERN, "$1\"***\"");

        if (body.length() > MAX_BODY_LOG_LENGTH) {
            body = body.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated]";
        }

        log.debug("{}", color("  Body: ", CYAN) + body);
    }

    private void logResponse(HttpServletRequest request,
                             ContentCachingResponseWrapper response,
                             long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        String msg = color("\u2190 ", MAGENTA)
                + color(request.getMethod() + " " + request.getRequestURI(), MAGENTA)
                + color(" | ", FAINT) + "status=" + response.getStatus()
                + color(" | ", FAINT) + "duration=" + color(duration + "ms", BRIGHT_YELLOW);

        log.info("{}", msg);
    }

    private void logResponseBody(ContentCachingResponseWrapper response) {
        if (!log.isDebugEnabled()) {
            return;
        }

        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return;
        }

        String contentType = response.getContentType();
        if (contentType != null && !contentType.toLowerCase().contains("json") && !contentType.toLowerCase().contains("text")) {
            log.debug("{}", color("  Response Body: ", MAGENTA) + "[binary content]");
            return;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        body = body.replaceAll(SENSITIVE_FIELD_PATTERN, "$1\"***\"");

        if (body.length() > MAX_BODY_LOG_LENGTH) {
            body = body.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated]";
        }

        log.debug("{}", color("  Response Body: ", MAGENTA) + body);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getSafeHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !SENSITIVE_HEADERS.contains(name.toLowerCase()))
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String color(String text, String ansiColor) {
        return colorEnabled ? ansiColor + text + RESET : text;
    }
}
