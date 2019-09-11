package org.westos.charroom;


import org.westos.config.MsgType;
import org.westos.uilts.InputAndOutputUtil;
import org.westos.uilts.TimeUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ClientThread extends Thread {
    InputStream in;
    private volatile boolean isSave=true;
    private volatile boolean isColse=false;

    public ClientThread(InputStream in) {
        this.in = in;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public boolean isColse() {
        return isColse;
    }

    public void setColse(boolean colse) {
        isColse = colse;
    }

    @Override
    public void run() {//子线程，用来负责，读取服务器转发回来的消息，
        //接收消息
        try {
            while (true) {
                // 读取服务器转发回来的消息，
                byte[] bytes = new byte[1024 * 10];
                int len = in.read(bytes);
                String msg = new String(bytes, 0, len).trim();
                // System.out.println(msg);
                //转发格式： 发送者:消息内容:消息类型:时间
                String[] msgs = msg.split(":");
                String sender = msgs[0];
                String msgContent = msgs[1];
                int msgType = Integer.parseInt(msgs[2]);
                long date = Long.parseLong(msgs[3]);
                //Date date = new Date(time);
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String timer = dateFormat.format(date);
                String time = TimeUtil.changeMils2Date(date, "yyyy-MM-dd HH:mm:ss");
                //根据消息类型展现消息
                if (msgType == MsgType.MSG_PRIVATE) {
                    System.out.println(time);
                    System.out.println(sender + "-对你说：" + msgContent);

                } else if (msgType == MsgType.MSG_PUBLIC) {

                    System.out.println(time);
                    System.out.println(sender + "-对大家说：" + msgContent);


                } else if (msgType == MsgType.MSG_ONLINE) {
                    System.out.println(time);
                    System.out.println(sender + ":" + msgContent);
                } else if (msgType == MsgType.MSG_ONLINELIST) {
                    System.out.println(time);
                    System.out.println(msgContent);
                } else if (msgType == MsgType.MSG_EXIT) {
                    System.out.println(time);
                    System.out.println(sender + ":" + msgContent);
                } else if (msgType == MsgType.MSG_FILE) {
                    System.out.println(time);
                    String[] fileInfo = msgContent.split("#");
                    String fileName = fileInfo[0];
                    long fileLens = Long.parseLong(fileInfo[1]);
                    System.out.println(sender + "给你发来一个文件-" + fileName + " 大小" + fileLens / 1024.0 + "kb");
                    System.out.println("你是否接收y/n");
                    while (isSave()){
                        if(isColse()){
                           break;
                        }
                    }
                    //读取文件数据保存到本地
                    //读取文件
                    //保存：isSave=false; isColse=false;
                    //不保存：isSave=true;isColse=true
                    if(isColse){
                        //不保存，也要把通道中的数据读完，只是不要保存到本地就行了
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] cathchBytes = new byte[1024 * 8];
                        int catcheLen = 0;
                        while (true) {
                            int lens = in.read(cathchBytes);
                            bos.write(cathchBytes, 0, lens);
                            catcheLen += lens;
                            if (catcheLen == fileLens) {
                                break;
                            }
                        }

                        //把文件字节数组取出来
                       // byte[] fileBytes = bos.toByteArray();

                    }else{
                        //保存
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] cathchBytes = new byte[1024 * 8];
                        int catcheLen = 0;
                        while (true) {
                            int lens = in.read(cathchBytes);
                            bos.write(cathchBytes, 0, lens);
                            catcheLen += lens;
                            if (catcheLen == fileLens) {
                                break;
                            }

                        }

                        //把文件字节数组取出来
                        byte[] fileBytes = bos.toByteArray();
                        boolean b = InputAndOutputUtil.writeFile("F:\\" + fileName, fileBytes);
                        if (b) {
                            System.out.println("文件保存成功！在" + "F:\\" + fileName);
                        } else {
                            System.out.println("文件保存失败");
                        }
                    }
                    //重置
                   isSave = true;
                   isColse = false;
                }else if (msgType==MsgType.MSG_ZHUCE){





                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
