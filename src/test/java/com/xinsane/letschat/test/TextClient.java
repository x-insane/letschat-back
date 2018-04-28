package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.util.DataIOUtil;

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
            out.writeByte(MessageType.TEXT);
            DataIOUtil.sendString(out, str);
            out.flush();
        }
        input.close();
        socket.close();
    }
}
