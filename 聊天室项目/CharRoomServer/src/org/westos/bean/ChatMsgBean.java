package org.westos.bean;

import java.io.Serializable;

public class ChatMsgBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public String reciver;// 接收者
	public String sender;// 发送者
	public String content;// 消息内容
	public long time;// 时间
	public int msgType;// 消息类型

	public String fileName;// 文件名
	public long fileLength;// 文件长度
	public byte fileData[];// 文件字节数据

	public ChatMsgBean(String reciver, String sender, String content,
                       long time, int msgType) {
		super();
		this.reciver = reciver;
		this.sender = sender;
		this.content = content;
		this.time = time;
		this.msgType = msgType;
	}

	public ChatMsgBean(String reciver, String sender, String content,
                       long time, int msgType, String fileName, long fileLength,
                       byte[] fileData) {
		super();
		this.reciver = reciver;
		this.sender = sender;
		this.content = content;
		this.time = time;
		this.msgType = msgType;
		this.fileName = fileName;
		this.fileLength = fileLength;
		this.fileData = fileData;
	}

}
