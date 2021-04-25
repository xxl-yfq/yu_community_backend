package com.douyuehan.doubao.controller;


import com.douyuehan.doubao.config.ApplicationContextUtil;
import com.douyuehan.doubao.model.entity.ChatMessage;
import com.douyuehan.doubao.model.entity.SysUser;
import com.douyuehan.doubao.model.vo.MsgVO;
import com.douyuehan.doubao.service.IUmsUserService;
import com.douyuehan.doubao.service.ChatMessageService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author KK
 */
@ServerEndpoint(value = "/websocket/{nickname}")
@Component
public class ApplicationWebSocket {

    private String nickname;
    private Session session;

    //用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<ApplicationWebSocket> webSocketSet = new CopyOnWriteArraySet<ApplicationWebSocket>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    //用来记录sessionId和该session进行绑定
    private static Map<String, Session> map = new HashMap<String, Session>();

    /**
     *
     * 连接建立成功调用的方法
     */
    //你要注入的service或者dao
    private  IUmsUserService iUmsUserService;
    private ChatMessageService chatMessageService;
    @OnOpen
    public void onOpen(Session session, @PathParam("nickname") String nickname) {
        Map<String,Object> message=new HashMap<String, Object>();
        this.session = session;
        this.nickname = nickname;
        map.put(nickname, session);
        webSocketSet.add(this);//加入set中
        System.out.println("有新连接加入:" + nickname + ",当前在线人数为" + webSocketSet.size());
        //this.session.getAsyncRemote().sendText("恭喜" + nickname + "成功连接上WebSocket(其频道号：" + session.getId() + ")-->当前在线人数为：" + webSocketSet.size());
        message.put("type",0); //消息类型，0-连接成功，1-用户消息
        message.put("people",webSocketSet.size()); //在线人数
        message.put("name",nickname); //昵称
        message.put("aisle",session.getId()); //频道号
        this.session.getAsyncRemote().sendText(new Gson().toJson(message));
    }

    /**
     * 连接关闭调用的方法    
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this); //从set中删除
        System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("nickname") String nickname) {
        System.out.println("来自客户端的消息-->" + nickname + ": " + message);

        //从客户端传过来的数据是json数据，所以这里使用jackson进行转换为SocketMsg对象，
        // 然后通过socketMsg的type进行判断是单聊还是群聊，进行相应的处理:
        ObjectMapper objectMapper = new ObjectMapper();
        MsgVO socketMsg;

        try {
            socketMsg = objectMapper.readValue(message, MsgVO.class);
            if (socketMsg.getType() == 1) {
                //单聊.需要找到发送者和接受者.
                Session fromSession = map.get(socketMsg.getFromUser());
                Session toSession = map.get(socketMsg.getToUser());
                //发送给接受者.
                iUmsUserService = ApplicationContextUtil.getBean(IUmsUserService.class);
                chatMessageService = ApplicationContextUtil.getBean(ChatMessageService.class);
                if (toSession != null) {
                    //发送给发送者.
                    Map<String,Object> m=new HashMap<String, Object>();
                    m.put("type",1);
                    m.put("name",nickname);
                    m.put("msg",socketMsg.getMsg());
                    SysUser sysUser = iUmsUserService.getUserByUsername(socketMsg.getFromUser());
                    m.put("fromUserDetail",sysUser);
                    SysUser sysUser1 = iUmsUserService.getUserByUsername(socketMsg.getToUser());
                    m.put("toUserDetail",sysUser1);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setFromId(sysUser.getId());
                    chatMessage.setToId(sysUser1.getId());
                    chatMessage.setContent(EmojiParser.parseToAliases(socketMsg.getMsg()));
                    chatMessage.setCreateTime(new Date());
                    chatMessage.setStatus(0);
                    if(sysUser.getId().compareTo(sysUser1.getId())>0){
                        chatMessage.setConversationId(sysUser.getId()+"_"+sysUser1.getId());//大的放前面小的放后面
                    }else{
                        chatMessage.setConversationId(sysUser1.getId()+"_"+sysUser.getId());//大的放前面小的放后面
                    }

                    chatMessageService.addSysMessage(chatMessage);


                    fromSession.getAsyncRemote().sendText(new Gson().toJson(m));
                    toSession.getAsyncRemote().sendText(new Gson().toJson(m));
                } else {
                    SysUser sysUser = iUmsUserService.getUserByUsername(socketMsg.getFromUser());
                    SysUser sysUser1 = iUmsUserService.getUserByUsername(socketMsg.getToUser());
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setFromId(sysUser.getId());
                    chatMessage.setToId(sysUser1.getId());
                    chatMessage.setContent(EmojiParser.parseToAliases(socketMsg.getMsg()));
                    chatMessage.setCreateTime(new Date());
                    chatMessage.setStatus(0);
                    if(sysUser.getId().compareTo(sysUser1.getId())>0){
                        chatMessage.setConversationId(sysUser.getId()+"_"+sysUser1.getId());//大的放前面小的放后面
                    }else{
                        chatMessage.setConversationId(sysUser1.getId()+"_"+sysUser.getId());//大的放前面小的放后面
                    }

                    chatMessageService.addSysMessage(chatMessage);
                    Map<String,Object> m=new HashMap<String, Object>();
                    m.put("type",3);
                    m.put("text","系统消息：对方不在线!");
                    //发送给发送者.
                    fromSession.getAsyncRemote().sendText(new Gson().toJson(m));
                }
            } else {
                //群发消息
                broadcast(nickname + ": " + socketMsg.getMsg());
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用   
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 群发自定义消息
     */
    public void broadcast(String message) {
        for (ApplicationWebSocket item : webSocketSet) {
            item.session.getAsyncRemote().sendText(message);//异步发送消息.
        }
    }

}
