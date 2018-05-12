package com.xinsane.letschat.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FileService {

    private static Map<String, FileInfo> map = new ConcurrentHashMap<>();

    public static String generateFileToken(String filepath, String ext) {
        String token = UUID.randomUUID().toString().replace("-", "");
        map.put(token, new FileInfo(filepath, ext));
        return token;
    }

    public static FileInfo getFileInfoByToken(String token) {
        return map.get(token);
    }

    public static class FileInfo {
        public String filepath;
        public String ext;

        FileInfo(String filepath, String ext) {
            this.filepath = filepath;
            this.ext = ext;
        }
    }
}
