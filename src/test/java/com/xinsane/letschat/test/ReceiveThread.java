package com.xinsane.letschat.test;

import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.util.DataIOUtil;

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
                    case MessageType.LOGIN: {
                        System.out.println("LOGIN|" + DataIOUtil.receiveString(in));
                        break;
                    }
                    case MessageType.TEXT: {
                        System.out.println("TEXT|" + DataIOUtil.receiveString(in) + "|" + DataIOUtil.receiveString(in));
                        break;
                    }
                    case MessageType.FILE_ID: {
                        System.out.print("FILE_ID|" + DataIOUtil.receiveString(in) + "|");
                        String ext = DataIOUtil.receiveString(in);
                        String token = DataIOUtil.receiveString(in);
                        System.out.println(ext + "|" + token);
                        new FileDownloadThread(token).start();
                        break;
                    }
                    case MessageType.EXIT: {
                        System.out.print("EXIT|" + DataIOUtil.receiveString(in));
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
