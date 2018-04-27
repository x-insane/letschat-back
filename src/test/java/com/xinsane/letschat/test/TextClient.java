package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;

import java.io.*;
import java.net.Socket;

public class TextClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("192.168.1.188", 7214);
        socket.setSoTimeout(10000);
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        new ReceiveThread(in).start();
        while (true) {
            String str = input.readLine();
            if (str.equals("exit"))
                break;
            System.out.println("sending...size=" + str.length() + ", str=" + str);
            out.writeByte(MessageType.TEXT);
            byte[] data = str.getBytes();
            out.writeInt(data.length);
            out.write(data);
            out.flush();
            System.out.println("send complete");
        }
        input.close();
        socket.close();
    }
}
