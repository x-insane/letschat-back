package com.xinsane.letschat.protocol;

/**
 * type of message from client
 */
public class MessageType {
    public static final byte TEXT = 0x01; // 文本消息
    public static final byte FILE = 0x02; // 文件消息
    public static final byte FILE_ID = 0x03; // 文件ID，或者通过文件ID请求文件
    public static final byte EXIT = 0x04; // 断开连接
}
