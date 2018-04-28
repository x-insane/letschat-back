package com.xinsane.letschat.protocol;

/**
 * type of message from client
 */
public class MessageType {
    public static final byte LOGIN = 0x01; // 发送用户识别信息
    public static final byte TEXT = 0x02; // 文本消息
    public static final byte FILE = 0x03; // 文件消息
    public static final byte FILE_ID = 0x04; // 文件ID，或者通过文件ID请求文件
    public static final byte EXIT = 0x05; // 断开连接
}
