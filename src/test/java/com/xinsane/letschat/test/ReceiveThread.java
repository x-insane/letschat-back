package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class ReceiveThread extends Thread {

    private DataInputStream in;

    ReceiveThread(DataInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte type = in.readByte();
                switch (type) {
                    case MessageType.TEXT: {
                        System.out.print("TEXT|");
                        int size = in.readInt();
                        System.out.print("[" + size + "]");
                        byte[] bin = new byte[size];
                        if (size != in.read(bin))
                            System.err.print("[wrong size]");
                        System.out.println(new String(bin));
                        break;
                    }
                    case MessageType.FILE_ID: {
                        System.out.print("FILE_ID|");
                        int size = in.readInt();
                        System.out.print("[" + size + "]");
                        byte[] bin = new byte[size];
                        if (size != in.read(bin))
                            System.err.print("[wrong size]");
                        String token = new String(bin);
                        System.out.println("token=" + token);
                        new FileDownloadThread(token).start();
                        break;
                    }
                    default:
                        System.err.println("receive wrong message type: " + type);
                }
            } catch (SocketTimeoutException e) {
//                System.out.print(".");
            } catch (IOException e) {
                System.out.println("error IO, exit.");
                System.exit(-1);
            }
        }
    }
}
