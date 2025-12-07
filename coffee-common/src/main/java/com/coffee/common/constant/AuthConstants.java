package com.coffee.common.constant;

public class AuthConstants {
    public static final String JWT_SECRET = "smartbrew-cloud-secret-key-for-jwt-signing-make-it-longer-than-64-bytes-please";
    public static final long JWT_EXPIRATION = 86400000; // 24小时
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}