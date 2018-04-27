package com.xinsane.letschat.old.server.handler;

import com.xinsane.letschat.old.server.ServerThread;
import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.protocol.MessageHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class StringHandler implements MessageHandler {

    private int size;
    private SocketChannel socketChannel;
    private StringBuilder builder;

    public StringHandler(SocketChannel socketChannel, int size) {
        this.socketChannel = socketChannel;
        this.size = size;
    }

    @Override
    public void handleMessage(ByteBuffer buffer) {
        String data = buffer.asCharBuffer().toString();
        if (data.length() != size)
            System.err.println("receive wrong data size: " + data.length() + ", expect " + size);
        send(data);
    }

    @Override
    public boolean handleMessageFrame(ByteBuffer buffer) {
        if (builder == null)
            builder = new StringBuilder();
        builder.append(buffer.asCharBuffer().toString());
        if (builder.length() == size) {
            send();
            return true;
        }
        return false;
    }

    private void send() {
        send(builder.toString());
    }

    private void send(String data) {
        System.out.println("receive: [" + data.length() + "]" + data);
        ServerThread.getServerThread().eachSocketChannel(socketChannel -> {
//            if (this.socketChannel != socketChannel) {
                try {
                    byte[] bin = data.getBytes();
                    ByteBuffer buffer = ByteBuffer.allocate(5 + bin.length);
                    buffer.put(MessageType.TEXT);
                    buffer.putInt(data.length());
                    buffer.put(bin);
                    buffer.flip();
                    while (buffer.hasRemaining())
                        socketChannel.write(buffer);
                } catch (IOException e) {
                    System.err.println("can not write to a user, remove it.");
                    return false;
                }
//            }
            return true;
        });
    }

}
