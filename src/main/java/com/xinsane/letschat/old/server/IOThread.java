package com.xinsane.letschat.old.server;

import com.xinsane.letschat.old.server.handler.StringHandler;
import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.protocol.MessageHandler;
import com.xinsane.letschat.protocol.MessageInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IOThread extends Thread {
	
	private Selector selector;
	Map<SocketChannel, ReadHandler> readHandlerMap = new ConcurrentHashMap<>();
	
	IOThread() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				selector.select(1000);
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					switch (key.readyOps()) {
					case SelectionKey.OP_READ:
						dealRead(key);
						break;
					}
					it.remove();
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	private void dealRead(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
		    ReadHandler handler = readHandlerMap.get(socketChannel);
		    if (handler != null)
		        handler.dealRead(socketChannel);
        } catch (IOException e) {
            try {
                socketChannel.close();
                System.out.println("A user disconnected.");
                readHandlerMap.remove(socketChannel);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
	}
	
	void accept(SocketChannel sc) throws IOException {
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);
        readHandlerMap.put(sc, new ReadHandler());
		System.out.println("A user connected: " + sc.socket().getRemoteSocketAddress().toString());
	}

	class ReadHandler {
	    private static final int SIZE = 1024;

	    private MessageInfo message;
        private ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        private MessageHandler handler;

	    void dealRead(SocketChannel socketChannel) throws IOException {
	        int len, total = 0;
	        do {
                len = socketChannel.read(buffer);
                total += len;
            }
	        while(len > 0);
	        System.out.println("len: " + total);

	        // 开始准备读
            if (message == null)
                buffer.flip();
            else
                buffer.reset();
            System.out.println("reading message...");

            // 读取消息类型
            if (message == null) {
                if (buffer.remaining() < 1) {
                    buffer.mark().position(buffer.limit()); // 暂时无法读取消息类型
                    System.out.println("incomplete type");
                    return;
                }
	            message = new MessageInfo();
                message.type = buffer.get();
            }

            // 读取消息体长度
            if (message.size == 0) {
                if (buffer.remaining() < 4) {
                    buffer.mark().position(buffer.limit()); // 暂时无法读取消息体长度
                    System.out.println("incomplete size");
                    return;
                }
                message.size = buffer.getInt();
            }

            // 绑定处理器
            switch (message.type) {
                case MessageType.TEXT:
                    handler = new StringHandler(socketChannel, message.size);
                    break;
                default:
                    System.err.println("receive wrong type!");
                    socketChannel.close();
                    readHandlerMap.remove(socketChannel);
                    return;
            }

            // 一次性读取数据
            if (message.size <= SIZE - 5) {
                if (buffer.remaining() < message.size) {
                    buffer.mark().position(buffer.limit()); // 暂时无法读取完整消息
                    return;
                }
                handler.handleMessage(buffer);
                buffer.clear();
                message = null;
                handler = null;
                return;
            }

            // 分片读取数据
            if (handler.handleMessageFrame(buffer)) {
                buffer.clear();
                message = null;
                handler = null;
                System.out.println("completed");
            } else
                System.out.println("incomplete body");
        }
    }
	
}
