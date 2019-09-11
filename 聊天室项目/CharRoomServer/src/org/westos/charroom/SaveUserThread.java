package org.westos.charroom;

import org.westos.config.MsgType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class SaveUserThread extends Thread{
    Socket sk;
    int type;
    HashMap<String, Socket> hm;
    private String username;
    ArrayList<String> list;
    String s1;
    Properties properties;

    public SaveUserThread(Socket sk, HashMap<String, Socket> hm, Properties properties, ArrayList<String> list) {
        this.sk=sk;
        this.hm=hm;
        this.list=list;
        this.properties=properties;

    }//这个线程用来处理注册用户
    @Override
    public void run() {
        try {
            //注册用户的逻辑完了以后，再开聊天线程
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            File file = new File("x.txt");//用来存放客户端和密码
            if (!file.exists()){
                file.createNewFile();
            }
            while (true){
                //读取客户端发来的用户名
                byte[] bytes = new byte[1024];
                int len=0;
                int read = in.read(bytes);
                String s = new String(bytes, 0, read);
                String[] split = s.split("-");
                username=split[0];
                s1=split[1];
                type=Integer.parseInt(split[2]);
                properties.load(new FileReader(file));
                if (type==MsgType.MSG_ZHUCE){
                    if (!properties.containsKey(username)){
                        properties.setProperty(username,s1);
                        properties.store(new FileOutputStream(file),"用户名和密码");
                        list.add(username);
                        hm.put(username,sk);
                        out.write("yes".getBytes());
                        break;
                    }else {
                        out.write("no".getBytes());
                    }


                }else if (type==MsgType.MSG_DENGLU){
                    if (properties.containsKey(username)){
                        String property = properties.getProperty(username);
                        if (s1.equals(property)){
                            hm.put(username,sk);
                            list.add(username);
                            out.write("yes".getBytes());
                            break;
                        }else {
                            out.write("no".getBytes());
                            break;
                        }
                    }
                }
            }

            //上线提醒
            //上线逻辑：遍历集合，取出每个人管道中输出流，写给他
            Set<String> keySet = hm.keySet();
            for (String key : keySet) {
                //排除自己
                if(key.equals(username)){
                    continue;
                }
                //取出每个人的管道
                Socket socket = hm.get(key);
                //转发格式： 发送者:消息内容:消息类型:时间
                String zfMsg=username+":"+"上线了"+":"+ MsgType.MSG_ONLINE+":"+System.currentTimeMillis();
                socket.getOutputStream().write(zfMsg.getBytes());
            }

            //最后再开服务端的读取消息线程
            new ServerThread(hm,username,sk,list).start();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
