package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;

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
            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();
            if (file.length() > size) {
                System.err.println("file too large.");
                fileInputStream.close();
                continue;
            }
            System.out.println("file size: " + size);
            out.writeByte(MessageType.FILE);

            byte[] filenameBytes = file.getName().getBytes();
            int filenameSize = filenameBytes.length;
            out.writeInt(filenameSize);
            out.write(filenameBytes);
            System.out.println("send filename ok.");

            out.writeInt(size);
            byte[] buffer = new byte[1024];
            int realSize, total = 0;
            System.out.println("start sending...");
            while (true) {
                realSize = fileInputStream.read(buffer);
                if (realSize <= 0)
                    break;
                out.write(buffer, 0, realSize);
                total += realSize;
            }
            if (total != size)
                System.err.println("transfer wrong! " + total + " bytes has been transferred, expect " + size);
            out.flush();
            fileInputStream.close();
            System.out.println("end sending.");
        }
        input.close();
        socket.close();
    }
}
