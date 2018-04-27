package com.xinsane.letschat.server;

import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.service.FileService;

import java.io.*;
import java.net.Socket;

public class IOThread extends Thread {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket;

    IOThread(Socket socket) {
        try {
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        root: while (true) {
            try {
                byte type = inputStream.readByte();
                switch (type) {
                    case MessageType.TEXT: {
                        int size = inputStream.readInt(), realSize;
                        byte[] data = new byte[size];
                        realSize = inputStream.read(data);
                        if (size != realSize)
                            System.err.println("receive wrong message size: " + realSize + ", expect " + size);
                        String text = new String(data);
                        System.out.println("receive text: " + text);
                        ServerThread.getServerThread().eachSocket((socket, io) -> {
                            try {
                                io.outputStream.writeByte(MessageType.TEXT);
                                io.outputStream.writeInt(realSize);
                                io.outputStream.write(data);
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
                        boolean error = false;
                        int filenameSize = inputStream.readInt();
                        byte[] filenameBytes = new byte[filenameSize];
                        int realSize = inputStream.read(filenameBytes);
                        if (filenameSize != realSize) {
                            System.err.println("receive wrong filename size: " + realSize + ", expect " + filenameSize);
                            error = true;
                        }
                        String filename = new String(filenameBytes);
                        File file = new File(filename);
                        if (file.exists())
                            System.err.println("file already exists, will be overwritten");
                        int size = inputStream.readInt(), total = 0;
                        System.out.println("start receive file " + filename + ", size=" + size);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            realSize = inputStream.read(buffer);
                            if (realSize <= 0)
                                break;
                            outputStream.write(buffer, 0, realSize);
                            total += realSize;
                            if (total == size)
                                break;
                        }
                        if (total != size) {
                            System.err.println("receive wrong file size: " + total + ", expect " + size);
                            error = true;
                        }
                        outputStream.flush();
                        outputStream.close();
                        System.out.println("receive ending.");
                        if (!error) {
                            String token = FileService.generateFileToken(file.getAbsolutePath());
                            System.out.println("generate a file token: " + token);
                            byte[] bytes = token.getBytes();
                            ServerThread.getServerThread().eachSocket((socket, io) -> {
                                try {
                                    io.outputStream.writeByte(MessageType.FILE_ID);
                                    io.outputStream.writeInt(bytes.length);
                                    io.outputStream.write(bytes);
                                    io.outputStream.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                                return true;
                            }, socket);
                        }
                        break;
                    }
                    case MessageType.FILE_ID: {
                        int tokenSize = inputStream.readInt();
                        byte[] tokenBytes = new byte[tokenSize];
                        int realTokenSize = inputStream.read(tokenBytes);
                        if (realTokenSize != tokenSize)
                            System.err.println("wrong token size: " + realTokenSize + ", expect " + tokenSize);
                        String token = new String(tokenBytes);
                        String filePath = FileService.getFilePathByToken(token);
                        File file = new File(filePath);
                        if (!file.exists())
                            System.err.println("file not found.");

                        FileInputStream fileInputStream = new FileInputStream(file);
                        int size = fileInputStream.available();
                        outputStream.writeByte(MessageType.FILE);

                        byte[] filenameBytes = file.getName().getBytes();
                        int filenameSize = filenameBytes.length;
                        outputStream.writeInt(filenameSize);
                        outputStream.write(filenameBytes);

                        outputStream.writeInt(size);
                        byte[] buffer = new byte[1024];
                        int realSize, total = 0;
                        System.out.println("start sending file " + file.getName() + "...");
                        while (true) {
                            realSize = fileInputStream.read(buffer);
                            if (realSize <= 0)
                                break;
                            outputStream.write(buffer, 0, realSize);
                            total += realSize;
                        }
                        if (total != size)
                            System.err.println("transfer wrong! " + total + " bytes has been transferred, expect " + size);
                        outputStream.flush();
                        System.out.println("end sending.");
                        fileInputStream.close();
                    }
                    case MessageType.EXIT:
                        ServerThread.getServerThread().remove(socket);
                        break root;
                    default:
                        System.err.println("receive wrong message type.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                ServerThread.getServerThread().remove(socket);
            }
        }
    }

}
