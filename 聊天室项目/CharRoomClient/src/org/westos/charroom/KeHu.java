package org.westos.charroom;

import org.westos.config.MsgType;
import org.westos.uilts.InputAndOutputUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class KeHu {

    private static InputStream in;
    private static OutputStream out;

    public static void main(String[] args) {

        //实现客户端和服务端的聊天
        //采用TCP
        /*1.实现一个客户端和服务器进行聊天，那么很显然这样设计是不合理的，我们是一个服务端，要连接多个客户端，
         *  然后是多个客户端，之间进行聊天，服务器负责，读取每个客户端，发来的消息，进行转发
         *  我们也发现了一个问题，有时候，服务器和客户端聊天的时候，消息会阻塞
         *  我们可以用多线程，来解决这个问题
         *  我们客户端，开启一个子线程，读消息 主线程发消息
         *  我们服务端，为每一个连接上来的客户端，单独开启一个线程，读取客户端发来的消息，服务器不需要再写回复消息的逻辑
         *2.我们要实现多个客户端之间进行聊天，那么我们要约定一下消息格式 接收者:消息内容:发送者
         *   我们需要在服务端，用一个集合来保存每个客户端的Socket
         *3.我们用单列集合来保存客户端的Socket 通过序号来取不方便，我们采用双列集合来存，这就得要求我们注册一个用户名
         *   怎么注册？我们单独又开了一个线程，专门处理用户名的注册，为了不跟聊天线程耦合，顺便在用户注册成功后，做了一个上线提醒功能
         *
         * */
        try {
            Socket sk = new Socket("192.168.11.101", 8888);
            //获取通道中的输入输出流
            in = sk.getInputStream();
            out = sk.getOutputStream();
            //聊天
            Scanner sc = new Scanner(System.in);
            System.out.println("请选择1.注册  2.登陆");
            int l = sc.nextInt();
            switch (l){
                case 1:
                    zhuCe();
                    break;
                case 2:
                    dengLu();

            }
            //注册用户


            //开启子线程
            ClientThread th = new ClientThread(in);
            th.start();

            //提供功能菜单选项
            boolean falg = true;
            while (falg) {
                System.out.println("请选择：1.私聊 2.公聊 3.在线列表 4.下线 5.隐身/上线 6.在线发送文件 7.查询聊天记录");
                //int num = InputUtil.inputIntType(new Scanner(System.in));
                Scanner scanner = new Scanner(System.in);
                String num = scanner.nextLine();
                switch (num) {
                    case "1": //私聊：两个客户端之间聊天
                        privateTalk();
                        break;
                    case "2": //公聊：取出所有人的管道，发送给每一个人
                        publicTalk();

                        break;
                    case "3": //获取在线列表
                        getOnlineList();
                        break;
                    case "4": //退出
                        //退出：
                        //客户端要做什么工作：0.给服务端发送下线指令 1.关闭客户端的Socket 2.还得停掉客户端读取消息的线程
                        exitTalk();
                        falg = false;
                        //服务端要做什么工作：1.服务端得关闭集合中下线者的管道，2.还得移除掉集合中下线人的名字，3，下线提醒
                        break;
                    case "5"://切换状态 //隐身：别的用户在获取在线列表时，是看不到你，你可以聊天
                        switchSttatus();
                        break;
                    case "6"://发送文件
                        sendFile();
                        break;
                    case "7"://查询聊天记录
                        chaTxt();
                        break;
                    case "y":
                        th.setSave(false);
                        break;
                    case "n":
                        th.setColse(true);
                        break;
                    default:
                        exitTalk();
                        falg = false;
                        break;
                }

            }
            //下线，关闭客户端读取线程，关闭客户端Scoket
            th.stop();
            sk.close();
        } catch (SocketException e) {
            //当我们关闭掉客户端的Socket后，可能会抛出SocketException 捕获一下做空处理
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void chaTxt() throws IOException{
       Scanner sc = new Scanner(System.in);
        System.out.println("查看私聊请按y   查看公聊请按n  返回按-q");
        String h = sc.nextLine();
        if (h=="-q"){
            return;
        }
        if (h.equals("y")){
            System.out.println("请输入要查看记录的用户名，退出请按（-q）");
            String reciver = sc.nextLine();
            //格式：接收人-消息内容-类型
            if (reciver=="-q"){
                return;
            }
            String msg=reciver+":"+"null"+":"+MsgType.MSG_SILIAO;
            out.write(msg.getBytes());
        }else if (h.equals("n")){
            String msg="null"+":"+"null"+":"+MsgType.MSG_GONGLIAO;
            out.write(msg.getBytes());
        }else {
            System.out.println("输入错误");
        }


    }

    //登陆方法
    private static void dengLu() throws IOException{
        while(true){
            Scanner sc2 = new Scanner(System.in);
            System.out.println("请输入用户名");
            String username = sc2.nextLine();
            System.out.println("请输入密码");
            String s1 = sc2.nextLine();
            String S=username+"-"+s1+"-"+MsgType.MSG_DENGLU;
            out.write(S.getBytes());
            byte[] bytes = new byte[1024];
            int len = in.read(bytes);
            String fk = new String(bytes, 0, len);
            if (fk.equals("no")) {
                System.out.println("用户名不存在");
            } else if (fk.equals("yes")) {
                System.out.println("登陆成功");
                break;
            }
        }
    }
    //注册方法
    private static void zhuCe() throws IOException{
        while (true) {
            Scanner sc1 = new Scanner(System.in);
            System.out.println("请输入用户名");

            String username = sc1.nextLine();
            System.out.println("请输入密码");
            String s1 = sc1.nextLine();
            String S=username+"-"+s1+"-"+MsgType.MSG_ZHUCE;
            out.write(S.getBytes());
            //读取服务器对于用户名的反馈
            byte[] bytes = new byte[1024];
            int len = in.read(bytes);
            String fk = new String(bytes, 0, len);
            if (fk.equals("no")) {
                System.out.println("用户名已经存在，请重新是输入");
            } else if (fk.equals("yes")) {
                System.out.println("用户名注册成功");
                break;
            }
        }
    }

    private static void switchSttatus() throws IOException {
        String msg = "null" + ":" + "null" + ":" + MsgType.MSG_SWITCHSTATUS;
        out.write(msg.getBytes());
    }

    private static void sendFile() throws IOException {
        //发送文件：
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入目标用户");
        String receiver = sc.nextLine();
        System.out.println("请输入文件路径");
        String path = sc.nextLine();
        //封装文件
        File file = new File(path);
        //接收者:消息#内容:消息类型                                :文件数据
        String msg = receiver + ":" + file.getName() + "#" + file.length() + ":" + MsgType.MSG_FILE;
        byte[] msgBytes = msg.getBytes();
        //创建一个空字节数组
        byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
        //文件字节数组

        byte[] fileBytes = InputAndOutputUtil.readFile(path);
        //把三个小的字节数组，合并成一个大的字节数组

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(msgBytes);
        bos.write(emptyBytes);
        bos.write(fileBytes);
        //取出大的字节数组
        byte[] allBytes = bos.toByteArray();
        //把大的字节数组发给服务器
        out.write(allBytes);

    }

    private static void exitTalk() throws IOException {
        String msg = "null" + ":" + "null" + ":" + MsgType.MSG_EXIT;
        out.write(msg.getBytes());
    }

    //获取在线列表
    private static void getOnlineList() throws IOException {
        //约定的格式： 接收者:消息内容:消息类型
        String msg = "null" + ":" + "null" + ":" + MsgType.MSG_ONLINELIST;
        out.write(msg.getBytes());
    }

    private static void publicTalk() throws IOException {
        while (true) {
            Scanner sc = new Scanner(System.in);
            //发送消息
            //约定的格式： 接收者:消息内容:消息类型
            System.out.println("你目前处于公聊模式-请输入消息内容 退出当前模式 -q");
            String msg = sc.nextLine();
            if ("-q".equals(msg)) {
                break;
            }
            msg = "null" + ":" + msg + ":" + MsgType.MSG_PUBLIC;
            out.write(msg.getBytes());
        }

    }

    private static void privateTalk() throws IOException {
        while (true) {
            Scanner sc = new Scanner(System.in);
            //发送消息
            //约定的格式： 接收者:消息内容:消息类型
            System.out.println("你目前处于私聊模式-请输入消息内容 格式 接收者:消息内容 退出当前模式 -q");
            String msg = sc.nextLine();
            if ("-q".equals(msg)) {
                break;
            }
            msg = msg + ":" + MsgType.MSG_PRIVATE;
            out.write(msg.getBytes());
        }
    }
}
