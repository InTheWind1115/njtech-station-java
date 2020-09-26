package com.njtechstation.controller.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.njtechstation.controller.chat.config.GetHttpSessionConfigurator;
import com.njtechstation.domain.Message;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndpoint {
    private static Map<String, ChatEndpoint> onlineUsers = new ConcurrentHashMap<>();

    private Session session;

    private HttpSession httpSession;

    // 记录着匹配到的人的手机号
    private String messageTo = "";

    // 记录着我自己的手机号
    private String phone = "";

    // 标志位，是否可以进行匹配
    private boolean isFree = true;

    // 标志位，是否进入匹配
    private boolean wantMatch = false;

    public String getMessageTo() {
        return messageTo;
    }

    public void setMessageTo(String messageTo) {
        this.messageTo = messageTo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public boolean isWantMatch() {
        return wantMatch;
    }

    public void setWantMatch(boolean wantMatch) {
        this.wantMatch = wantMatch;
    }

    /**
     * @author
     * @function 建立连接时使用
     * @param session
     * @param config
     *
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("建立连接的wssession：" + session);
        this.session = session;
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.httpSession = httpSession;
        System.out.println("ws时候的httpsession" + httpSession);
        this.phone = (String) httpSession.getAttribute("phone");
        System.out.println((String) httpSession.getAttribute("phone") + "!!!!!!!!");
        if (phone != null)
            onlineUsers.put(this.phone, this);
    }

    /**
     * @author Liucheng
     * @function 接收到客户端发送的数据时被调用
     * @param message
     *  {type: , content: } type = -1代表着断开连接 type = 0代表着进行匹配 type = 1代表着发来的内容(content)
     * @param session
     */
    @OnMessage
     public void onMessage(String message, Session session) throws IOException {


        ObjectMapper objectMapper = new ObjectMapper();

        Message messRecieved = objectMapper.readValue(message, Message.class);


        int type = messRecieved.getType();
        if (type == -1) {
            ChatEndpoint person = onlineUsers.get(this.messageTo);

            // 下面这里通知对方已离开
            Message leave = new Message();
            leave.setType(-1);
            leave.setContent("对方已经离开，请重新匹配。");
            String jsonLeave = "";
            jsonLeave = objectMapper.writeValueAsString(leave);
            person.session.getBasicRemote().sendText(jsonLeave);
            //这里图省事给自己也发了一个对方离开的请求，这样方便前端了，但耦合性增加了
            leave.setExtra("me"); // 这里通知前端，是自己发送的，不同弹出modal了
            this.session.getBasicRemote().sendText(objectMapper.writeValueAsString(leave));

            person.setMessageTo("");
            person.setFree(true);
            person.setWantMatch(false);
            this.messageTo = "";
            this.isFree = true;
            this.wantMatch = false;
        } else if (type == 0) {

            Message message2 = new Message();
            System.out.println(this.httpSession + "$$$$$$$$$$$$$$$$$$$$$$$");
            System.out.println(this.httpSession.getAttribute("phone"));
            if (this.httpSession.getAttribute("phone") == null) {
                message2.setType(-4);
                message2.setContent("您还没有登录，无法使用此功能。");
                this.session.getBasicRemote().sendText(objectMapper.writeValueAsString(message2));
                return;
            }

            if (this.httpSession.getAttribute("email") == null) {
                message2.setType(-3);
                message2.setContent("您还没有进行南京工业大学邮箱验证，无法使用此功能。");
                this.session.getBasicRemote().sendText(objectMapper.writeValueAsString(message2));
                return;
            }

            this.wantMatch = true;
            if (!match()) {
                message2.setType(-2);
                message2.setContent("正在匹配中...");
                String matchFail= objectMapper.writeValueAsString(message2);
                this.session.getBasicRemote().sendText(matchFail);
            } else {
                message2.setType(0);
                message2.setContent("匹配成功，开始聊天吧！");
                String matchSucc = objectMapper.writeValueAsString(message2);
                this.session.getBasicRemote().sendText(matchSucc);
                this.onlineUsers.get(this.messageTo).session.getBasicRemote().sendText(matchSucc);
            }

        } else if (type == 1) {
            Message messTo = new Message();
            messTo.setType(1);
            messTo.setContent(messRecieved.getContent());
            String messToStr = objectMapper.writeValueAsString(messTo);
            onlineUsers.get(this.messageTo).session.getBasicRemote().sendText(messToStr);
        }
    }


    /**
     * @author Liucheng
     *
     * @return true: 匹配成功 false: 匹配失败
     */
    public boolean match() {
        Iterator entries = this.onlineUsers.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String phoneNum = (String) entry.getKey();
            ChatEndpoint chatEndpoint = (ChatEndpoint) entry.getValue();
            System.out.println("this.phone" + this.phone);
            if (!this.phone.equals(phoneNum) && chatEndpoint.isFree() && chatEndpoint.isWantMatch()) {
                chatEndpoint.setFree(false);
                chatEndpoint.setMessageTo(this.phone);

                this.setFree(false);
                this.setMessageTo(chatEndpoint.getPhone());
                return true;
            }
        }
        return false;
     }
}
