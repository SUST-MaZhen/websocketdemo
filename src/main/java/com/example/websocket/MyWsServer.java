package com.example.websocket;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description ws的server，相当于controller
 * @ClassName MyWsServer
 * @Author MaZhen
 * @date 2020.05.31 18:24
 */
@ServerEndpoint("/api/v1/websocket/{userId}")
@Component
public class MyWsServer {
    // 引入log4j日志
    static Logger logger = Logger.getLogger(MyWsServer.class);
    // 静态变量，用来记录当前在线连接数
    private static int onlineCount = 0;
    // concurrent包的线程安全Set，用来存放客户端对象
    private static ConcurrentHashMap<String, MyWsServer> clients = new ConcurrentHashMap<>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    // 用户唯一标识符
    private String userId = "";

    /**
     * @return
     * @Description websocket的连接函数
     * @Param {Session} session
     * @Author zhen.ma
     * @Date 2020.05.31 19:13
     **/
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        logger.info("有新的客户端连接进来了，客户端id是： " + session.getId());
        if (clients.containsKey(userId)) {
            clients.remove(userId);
            clients.put(userId, this);
        } else {
            clients.put(userId, this);
            MyWsServer.addOnlineCount();
        }

        logger.info("用户：" + userId + ", 当前在线人数为:" + getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            logger.error("用户:" + userId + ",网络异常!");
        }

    }

    /**
     * @return
     * @Description 关闭事件处理函数
     * @Param
     * @Author zhen.ma
     * @Date 2020.06.12 22:15
     **/
    @OnClose
    public void onClose() {
        if (clients.containsKey(userId)) {
            clients.remove(userId);
            subOnlineCount();
        }
        logger.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * @return
     * @Description 接收消息
     * @Param
     * @Author zhen.ma
     * @Date 2020.06.12 22:29
     **/
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("用户" + userId + "发来消息， 报文：" + message);
    }

    /**
     * @return
     * @Description
     * @Param
     * @Author zhen.ma
     * @Date 2020.06.12 22:31
     **/
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * @return {null}
     * @Description 向客户端发送消息
     * @Param {String} message 要发送的消息
     * @Author zhen.ma
     * @Date 2020.05.31 19:14
     **/
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * @return
     * @Description 发送自定义消息到指定用户或者群发消息
     * @Param
     * @Author zhen.ma
     * @Date 2020.06.12 22:55
     **/
    public static void sendInfo(String message, String fromUserId, String toUserId) {
        logger.info("推送消息给用户" + toUserId + "，推送内容:" + message);
        for (MyWsServer client : clients.values()) {
            try {
                //这里可以设定只推送给这个userId的，为null则全部推送
                if ("every".equals(toUserId) && !client.userId.equals(fromUserId)) {
                    client.sendMessage("来自" + fromUserId + "的群发消息：" + message);
                } else if (client.userId.equals(toUserId)) {
                    client.sendMessage("来自" + fromUserId + "发给" + toUserId + "的消息：" + message);
                }
            } catch (IOException e) {
                System.out.println(e.toString());
                continue;
            }
        }
    }

    /**
     * @return {null}
     * @Description 在线客户端数加一
     * @Param {null}
     * @Author zhen.ma
     * @Date 2020.05.31 19:12
     **/
    public static synchronized void addOnlineCount() {
        MyWsServer.onlineCount++;
    }

    /**
     * @return {null}
     * @Description 在线客户端数减一
     * @Param {null}
     * @Author zhen.ma
     * @Date 2020.05.31 19:12
     **/
    public static synchronized void subOnlineCount() {
        MyWsServer.onlineCount--;
    }

    /**
     * @return
     * @Description 获取在线连接数
     * @Param
     * @Author zhen.ma
     * @Date 2020.05.31 19:12
     **/
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

}
