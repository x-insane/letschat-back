package com.xinsane.letschat.util;

import java.io.*;

public class DataIOUtil {
    public static void sendString(DataOutputStream outputStream, String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
    }
    public static String receiveString(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        byte[] bytes = new byte[size];
        int realSize = inputStream.read(bytes);
        if (realSize != size)
            System.err.println("wrong string size: " + realSize + ", expect " + size);
        return new String(bytes);
    }
    public static void sendFile(DataOutputStream outputStream, FileInputStream inputStream) throws IOException {
        int size = inputStream.available();
        outputStream.writeInt(size);
        byte[] buffer = new byte[1024];
        int realSize, total = 0;
        while (true) {
            realSize = inputStream.read(buffer);
            if (realSize <= 0)
                break;
            total += realSize;
            outputStream.write(buffer, 0, realSize);
        }
        if (total != size)
            System.err.println("wrong file size send: " + total + ", expect " + size);
    }
    public static int receiveFile(DataInputStream inputStream, FileOutputStream outputStream) throws IOException {
        int size = inputStream.readInt();
        byte[] buffer = new byte[1024];
        int realSize, total = 0;
        while (true) {
            realSize = inputStream.read(buffer);
            if (realSize <= 0)
                break;
            total += realSize;
            outputStream.write(buffer, 0, realSize);
            if (total == size)
                break;
        }
        if (total != size)
            System.err.println("wrong file size receive: " + total + ", expect " + size);
        return total;
    }
}
