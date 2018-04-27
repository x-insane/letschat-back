package com.xinsane.letschat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerThread extends Thread {
    private static ServerThread serverThread;
    static ServerThread getServerThread() {
        return serverThread;
    }

    private Map<Socket, IOThread> map = new ConcurrentHashMap<>();
    private int port;

    public ServerThread(int port) {
        this.port = port;
        serverThread = this;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                IOThread io = new IOThread(socket);
                io.start();
                map.put(socket, io);
                System.out.println("a user connected: " + socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void remove(Socket socket) {
        map.remove(socket);
        System.out.println("a user disconnected.");
    }

    void eachSocket(SenderCallback callback, Socket sourceSocket) {
        for (Socket socket : map.keySet()) {
            if (sourceSocket != socket && !callback.each(socket, map.get(socket))) {
                map.remove(socket);
                System.out.println("a user can not access, remove it.");
            }
        }
    }

    interface SenderCallback {
        /**
         * 历遍所有连接的socket
         * @param socket 回调当前处理的socket
         * @param io 对应的IOTread
         * @return 如果处理成功则返回true，返回false时会移除该socket
         */
        boolean each(Socket socket, IOThread io);
    }
}
