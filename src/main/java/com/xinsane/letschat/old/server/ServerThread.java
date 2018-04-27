package com.xinsane.letschat.old.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

public final class ServerThread extends Thread {
    private static ServerThread serverThread;
    public static ServerThread getServerThread() {
        return serverThread;
    }
	
	private int port;
	private Selector selector;
	private IOThread[] io_thread_pool;
	private static final int pool_size = 20;
	private Random rand = new Random();
	
	public ServerThread(int port) {
	    serverThread = this;
		this.port = port;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port));
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			// 启动IO线程
			io_thread_pool = new IOThread[pool_size];
			for (int i=0;i<pool_size;i++) {
				io_thread_pool[i] = new IOThread();
				io_thread_pool[i].start();
			}
			System.out.println(pool_size + " io_thread started!\nListen on port " + port + "...");
			while (true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					switch (key.readyOps()) {
					case SelectionKey.OP_ACCEPT:
						dealAccept(key);
						break;
					}
					it.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void dealAccept(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			IOThread ioThread = io_thread_pool[rand.nextInt(pool_size)];
			ioThread.accept(server.accept());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void eachSocketChannel(SenderCallback callback) {
	    for (IOThread thread : io_thread_pool) {
            for (SocketChannel socketChannel : thread.readHandlerMap.keySet()) {
                if (!callback.each(socketChannel)) {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    thread.readHandlerMap.remove(socketChannel);
                }
            }
        }
    }

	public interface SenderCallback {
        /**
         * 历遍所有连接的socket
         * @param socketChannel 回调当前处理的socket
         * @return 如果处理成功则返回true，返回false时会移除该socket
         */
	    boolean each(SocketChannel socketChannel);
    }
}
