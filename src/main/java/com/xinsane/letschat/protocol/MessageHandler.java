package com.xinsane.letschat.protocol;

import java.nio.ByteBuffer;

public interface MessageHandler {
    /**
     * 一次性处理消息
     * @param buffer 缓冲器
     */
    void handleMessage(ByteBuffer buffer);

    /**
     * 处理一次性无法传输的消息分片
     * @param buffer 缓冲器
     * @return 消息被完整的读取完毕时返回true，否则返回false
     */
    boolean handleMessageFrame(ByteBuffer buffer);
}
