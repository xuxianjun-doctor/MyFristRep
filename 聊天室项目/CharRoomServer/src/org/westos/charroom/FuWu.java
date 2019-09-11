package org.westos.charroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class FuWu {
    public static void main(String[] args) {
        //这个集合用来，把用户名存储一份
        ArrayList<String> list = new ArrayList<>();
        //创建双列集合，用来保存，用户名，和他对应的管道
        HashMap<String, Socket> hm = new HashMap<>();
       Properties properties = new Properties();
        try {
            ServerSocket socket = new ServerSocket(8888);
            System.out.println("服务器已经开启...");
            int i=1;
            //监听客户端
            while (true){
                Socket sk = socket.accept();
                //获取通道中的输入输出流
                System.out.println((i++)+"个客户端连接上来了");
                //保存连接上来的Socket
                //list.add(sk);
                //InputStream in = sk.getInputStream();
                //OutputStream out = sk.getOutputStream();
                //服务器一上来先开 注册用户的线程
                new SaveUserThread(sk,hm,properties,list).start();


               // new ServerThread(sk,list).start();
            }
            //下面的代码不要，服务端不负责回复消息
            //Scanner sc = new Scanner(System.in);
           /* while (true){

                //回复消息
                System.out.println("请输入要回复的消息");
                String msg = sc.nextLine();
                out.write(msg.getBytes());
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
