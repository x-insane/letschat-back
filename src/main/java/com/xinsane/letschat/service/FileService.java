package com.xinsane.letschat.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FileService {

    private static Map<String, String> map = new ConcurrentHashMap<>();

    public static String generateFileToken(String filepath) {
        String token = UUID.randomUUID().toString().replace("-", "");
        map.put(token, filepath);
        return token;
    }

    public static String getFilePathByToken(String token) {
        return map.get(token);
    }

}
