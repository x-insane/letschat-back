package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;

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
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = token.getBytes();
            out.writeByte(MessageType.FILE_ID);
            out.writeInt(bytes.length);
            out.write(bytes);
            out.flush();
            byte type = in.readByte();
            if (type != MessageType.FILE)
                System.err.println("wrong type(expect FILE): " + type);
            System.out.print("FILE|");
            int filenameSize = in.readInt();
            byte[] filenameBytes = new byte[filenameSize];
            int realFilenameSize = in.read(filenameBytes);
            if (realFilenameSize != filenameSize)
                System.err.println("wrong filename size: " + realFilenameSize + ", expect " + filenameSize);
            String filename = new String(filenameBytes);
            System.out.print("<" + filename + ">");
            int fileSize = in.readInt();
            System.out.print("[" + fileSize + "]");
            byte[] buffer = new byte[1024];
            String tmpFilename = UUID.randomUUID().toString().replace("-", "");
            File file = new File(tmpFilename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int realSize, total = 0;
            while (true) {
                realSize = in.read(buffer);
                if (realSize <= 0)
                    break;
                fileOutputStream.write(buffer, 0, realSize);
                total = total + realSize;
                if (total == fileSize)
                    break;
            }
            System.out.println(tmpFilename);
            if (total != fileSize)
                System.err.println("receive wrong file size: " + total + ", expect " + fileSize);
            System.out.println("receive file ok.");
            fileOutputStream.flush();
            fileOutputStream.close();
            out.writeByte(MessageType.EXIT);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
