package com.xinsane.letschat;


import com.xinsane.letschat.server.ServerThread;

public class Server {
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].endsWith("/"))
                Config.filePath = args[0];
            else
                Config.filePath = args[0] + "/";
        }
        int port = 7214;
        if (args.length > 1)
            port = Integer.parseInt(args[1]);
        new ServerThread(port).start();
    }
}
