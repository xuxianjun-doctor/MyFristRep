package org.westos.consig;


public interface MsgType { //消息类型
    //私聊消息类型
   public static final int MSG_PRIVATE=100; //私聊
    public static final int MSG_PUBLIC = 200;//公聊

    int MSG_ONLINE = 300;//上线提醒
    int MSG_ONLINELIST=400;//在线列表
    int MSG_EXIT=500;
    int MSG_FILE=600;
    int MSG_SWITCHSTATUS=700;//切换映射上线
    int MSG_ZHUCE=800;//注册用户
    int MSG_DENGLU=900;//登陆用户
    int MSG_SILIAO=1000;//私聊聊天记录
    int MSG_GONGLIAO=1100;//公聊聊天记录
}
