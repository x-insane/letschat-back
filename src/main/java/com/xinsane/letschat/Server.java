package com.xinsane.letschat;


import com.xinsane.letschat.server.ServerThread;

public class Server {
    public static void main(String[] args) {
        new ServerThread(7214).start();
    }
}
