package org.westos.bean;

import java.io.Serializable;

public class ChatMsgBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ���b��Ϣ
	public String reciver;//发送者
	public String sender;// 接收者
	public String content;// 内容
	public long time;// 时间
	public int msgType;// 类型

	public String fileName;// �ļ���
	public long fileLength;// �ļ���С
	public byte fileData[];// �ļ�����

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
