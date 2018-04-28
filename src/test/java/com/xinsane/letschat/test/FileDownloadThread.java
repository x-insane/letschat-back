package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.util.DataIOUtil;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class FileDownloadThread extends Thread {

    private String token;

    FileDownloadThread(String token) {
        this.token = token;
    }

    @Override
    public void run() {
        Socket socket;
        try {
            socket = new Socket("192.168.1.188", 7214);
            socket.setSoTimeout(10000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out.writeByte(MessageType.FILE_ID);
            DataIOUtil.sendString(out, token);
            out.flush();
            byte type = in.readByte();
            if (type != MessageType.FILE)
                System.err.println("wrong type(expect FILE): " + type);
            String ext = DataIOUtil.receiveString(in);
            System.out.print("FILE|" + ext + "|");
            File file = new File("cache/" +
                    UUID.randomUUID().toString().replace("-", "") + "." + ext);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int size = DataIOUtil.receiveFile(in, fileOutputStream);
            fileOutputStream.close();
            System.out.println(size + "|" + file.getName());
            out.writeByte(MessageType.EXIT);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
