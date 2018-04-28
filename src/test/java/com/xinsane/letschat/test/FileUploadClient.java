package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.util.DataIOUtil;

import java.io.*;
import java.net.Socket;

public class FileUploadClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("192.168.1.188", 7214);
        socket.setSoTimeout(10000);
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        new ReceiveThread(in).start();
        while (true) {
            String filename = input.readLine();
            if (filename.equals("exit"))
                break;
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("file not exists.");
                continue;
            }

            // 打开并发送文件
            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();
            if (file.length() > size) {
                System.err.println("file too large.");
                fileInputStream.close();
                continue;
            }
            System.out.println("send file " + file.getName() + ", size: " + size);
            out.writeByte(MessageType.FILE);

            // 发送文件后缀
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex == -1)
                DataIOUtil.sendString(out, "");
            else
                DataIOUtil.sendString(out, filename.substring(dotIndex + 1));

            // 发送文件
            DataIOUtil.sendFile(out, fileInputStream);

            // 结束发送
            out.flush();
            fileInputStream.close();
        }
        input.close();
        socket.close();
    }
}
