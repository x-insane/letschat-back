package com.xinsane.letschat.server;

import com.xinsane.letschat.Config;
import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.service.FileService;
import com.xinsane.letschat.util.DataIOUtil;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class IOThread extends Thread {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket;
    private String user = "";

    IOThread(Socket socket) {
        try {
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        try {
            ServerThread.getServerThread().remove(socket);
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("can not close socket");
        }
    }

    @Override
    public void run() {
        root: while (true) {
            try {
                byte type = inputStream.readByte();
                switch (type) {
                    case MessageType.LOGIN: {
                        user = DataIOUtil.receiveString(inputStream);
                        System.out.println("LOGIN|" + user);
                        ServerThread.getServerThread().eachSocket((socket, io) -> {
                            try {
                                io.outputStream.writeByte(MessageType.LOGIN);
                                DataIOUtil.sendString(io.outputStream, user);
                                io.outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;
                        }, socket);
                        break;
                    }
                    case MessageType.TEXT: {
                        String text = DataIOUtil.receiveString(inputStream);
                        System.out.println("TEXT|" + user + "|" + text);
                        ServerThread.getServerThread().eachSocket((socket, io) -> {
                            try {
                                io.outputStream.writeByte(MessageType.TEXT);
                                DataIOUtil.sendString(io.outputStream, user.isEmpty() ? "匿名用户" : user);
                                DataIOUtil.sendString(io.outputStream, text);
                                io.outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;
                        }, socket);
                        break;
                    }
                    case MessageType.FILE: {
                        // 接收文件后缀
                        String ext = DataIOUtil.receiveString(inputStream);
                        System.out.print("FILE|" + user + "|" + ext + "|");
                        if (!ext.equals("jpg"))
                            exit();

                        // 创建文件
                        File file = new File(Config.filePath +
                                UUID.randomUUID().toString().replace("-", "") + "." + ext);
                        System.out.println(file.getName());

                        // 写入文件
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        DataIOUtil.receiveFile(inputStream, fileOutputStream);
                        fileOutputStream.close();

                        // 获取并发送token
                        String token = FileService.generateFileToken(file.getAbsolutePath(), ext);
                        System.out.println("generate a file token: " + token);
                        ServerThread.getServerThread().eachSocket((socket, io) -> {
                            try {
                                io.outputStream.writeByte(MessageType.FILE_ID);
                                DataIOUtil.sendString(io.outputStream, user.isEmpty() ? "匿名用户" : user);
                                DataIOUtil.sendString(io.outputStream, token);
                                io.outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;
                        }, socket);
                        break;
                    }
                    case MessageType.FILE_ID: {
                        // 接收token
                        String token = DataIOUtil.receiveString(inputStream);

                        // 获取文件信息
                        FileService.FileInfo fileInfo = FileService.getFileInfoByToken(token);
                        File file = new File(fileInfo.filepath);
                        if (!file.exists())
                            System.err.println("file not found.");

                        // 接收文件
                        FileInputStream fileInputStream = new FileInputStream(file);
                        outputStream.writeByte(MessageType.FILE);
                        DataIOUtil.sendString(outputStream, fileInfo.ext);
                        DataIOUtil.sendFile(outputStream, fileInputStream);
                        fileInputStream.close();
                    }
                    case MessageType.EXIT:
                        if (user!= null && !user.isEmpty()) {
                            System.out.println("EXIT|" + user);
                            sendExit();
                        }
                        exit();
                        break root;
                    default:
                        System.err.println("receive wrong message type.");
                }
            } catch (IOException e) {
                if (e instanceof EOFException && !user.isEmpty()) {
                    System.out.println("EXIT|" + user);
                    sendExit();
                }
                else
                    e.printStackTrace();
                exit();
                break;
            }
        }
    }

    private void sendExit() {
        ServerThread.getServerThread().eachSocket((socket, io) -> {
            try {
                io.outputStream.writeByte(MessageType.EXIT);
                DataIOUtil.sendString(io.outputStream, user.isEmpty() ? "某匿名用户" : user);
                io.outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }, socket);
    }
}
