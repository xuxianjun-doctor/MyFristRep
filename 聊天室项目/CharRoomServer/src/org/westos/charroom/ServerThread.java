package org.westos.charroom;

import org.westos.config.MsgType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ServerThread extends Thread{

    Socket sk;
    HashMap<String, Socket> hm;
    String username;
    ArrayList<String> list;
    boolean isHidden=true;  //true代表在线 false 隐身

    public ServerThread(HashMap<String, Socket> hm, String username, Socket sk, ArrayList<String> list) {
        this.hm=hm;
        this.sk=sk;
        this.username=username;
        this.list=list;
    }

    @Override
    public void run() {
        try {
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            while (true){
                //服务端接收消息
                byte[] bytes = new byte[1024*10];
                int len = in.read(bytes);
                String msg = new String(bytes, 0, len).trim();//去掉空格
                //客户端发来的消息格式：接收者:消息内容:消息类型
                //System.out.println(msg);
                //截取客户端发来的消息，根据消息类型做不同处理
                String[] msgs = msg.split(":");
                String reciver=msgs[0];
                String msgContent=msgs[1];
                int msgType=Integer.parseInt(msgs[2]);
                if(msgType== MsgType.MSG_PRIVATE){
                    //私聊：拿出接收者的管道中的输出流，写给他，写给他之前，我们 把消息重新组拼一下
                    //转发格式： 发送者:消息内容:消息类型:时间
                    String zfMsg=username+":"+msgContent+":"+msgType+":"+System.currentTimeMillis();
                    if (hm.get(reciver) != null) {
                        hm.get(reciver).getOutputStream().write(zfMsg.getBytes());
                    }
                }else if(msgType == MsgType.MSG_PUBLIC){
                    //公聊：逻辑：取出所有人的管道，发给每一个人
                    Set<String> keySet = hm.keySet();
                    for (String key : keySet) {
                        //排除自己
                        if (key.equals(username)) {
                            continue;
                        }
                        //取出每个人的管道
                        Socket socket = hm.get(key);
                        //转发格式： 发送者:消息内容:消息类型:时间
                        String zfMsg = username + ":" +msgContent + ":" + MsgType.MSG_PUBLIC + ":" + System.currentTimeMillis();
                        socket.getOutputStream().write(zfMsg.getBytes());
                    }
                }else if(msgType == MsgType.MSG_ONLINELIST){
                    //得给客户端返回在线列表
                    //Set<String> keySet = hm.keySet();
                    StringBuffer sb = new StringBuffer();
                    int i=1;
                    for (String key :list) {
                        if (key.equals(username)) {
                            continue;
                        }
                        //1.张三
                        //2.李四
                        //3.王五
                        sb.append((i++)).append(".").append(key).append("\n");

                    }
                    String zfMsg = username + ":" + sb.toString() + ":" + MsgType.MSG_ONLINELIST+ ":" + System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfMsg.getBytes());

                }else if(msgType == MsgType.MSG_EXIT){
                    //给其他人发送下线提醒
                    Set<String> keySet = hm.keySet();
                    for (String key : keySet) {
                        //排除自己
                        if (key.equals(username)) {
                            continue;
                        }
                        //取出每个人的管道
                        Socket socket = hm.get(key);
                        //转发格式： 发送者:消息内容:消息类型:时间
                        String zfMsg = username + ":" + "下线了" + ":" + MsgType.MSG_EXIT + ":" + System.currentTimeMillis();
                        socket.getOutputStream().write(zfMsg.getBytes());
                    }
                    break;

                }else if(msgType == MsgType.MSG_FILE){
                    //服务端处理文件：文本信息要拆分组拼，再把文件数据再拼接上，转发给客户端
                    String[] fileInfo = msgContent.split("#");
                    String fileName=fileInfo[0];
                    long fileLength=Long.parseLong(fileInfo[1]);
                    //要转发回去的文本数据，组拼好  发送者:消息内容:消息类型:时间
                    String zfMsg=username+":"+msgContent+":"+msgType+":"+System.currentTimeMillis();
                    byte[] msgBytes = zfMsg.getBytes();
                    byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
                    //读取文件
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] cathchBytes=new byte[1024*8];
                    int catcheLen=0;
                    while (true){
                        int lens = in.read(cathchBytes);
                        bos.write(cathchBytes,0,lens);
                        catcheLen+=lens;
                        if(catcheLen==fileLength){
                            break;
                        }

                    }

                    //把文件字节数组取出来
                    byte[] fileBytes = bos.toByteArray();
                    bos.reset();//重置一下

                    bos.write(msgBytes);
                    bos.write(emptyBytes);
                    bos.write(fileBytes);
                    //取出大的字节数组
                    byte[] allBytes = bos.toByteArray();
                    //转发回去
                    hm.get(reciver).getOutputStream().write(allBytes);
                }else if(msgType == MsgType.MSG_SWITCHSTATUS){
                    //隐身/上线
                    if(isHidden){
                        //在线--->隐身
                        list.remove(username);
                        //isHidden=false;
                    }else{
                        //隐身---->在线
                        list.add(username);
                        Set<String> keySet = hm.keySet();
                        for (String key : keySet) {
                            //排除自己
                            if (key.equals(username)) {
                                continue;
                            }
                            //取出每个人的管道
                            Socket socket = hm.get(key);
                            //转发格式： 发送者:消息内容:消息类型:时间
                            String zfMsg = username + ":" + "又上线了" + ":" + MsgType.MSG_ONLINE + ":" + System.currentTimeMillis();
                            socket.getOutputStream().write(zfMsg.getBytes());
                        }

                        //isHidden=true;
                    }

                    isHidden=!isHidden;//更改开关状态
                }else if (msgType==MsgType.MSG_SILIAO){

                    String zfmsg=reciver+":"+msgContent+":"+msgType+":"+System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfmsg.getBytes());
                }else if (msgType==MsgType.MSG_GONGLIAO){
                    String zfmsg="PUBLIC聊天"+":"+"这是你和大家的聊天记录"+":"+msgType+":"+System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfmsg.getBytes());
                }

            }
            //关闭集合中下线者的管道
            hm.get(username).close();
            hm.remove(username);
            //下线时把单列集合也移除一下
            list.remove(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
