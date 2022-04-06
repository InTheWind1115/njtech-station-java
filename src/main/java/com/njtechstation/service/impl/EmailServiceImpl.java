package com.njtechstation.service.impl;

import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

public class EmailServiceImpl {

    private final String myEmail = "1528623056@qq.com"; //发送人的邮箱
    private final String myEmailPwd = ""; //发送人的授权码
    private String  receieveMail = null;
    private String info = null;

    public void setReceieveMail(String receieveMail) {
        this.receieveMail = receieveMail;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    private String myEmailSMTPServer = "smtp.qq.com";

    public void send() throws MessagingException, UnsupportedEncodingException {

        // 用来配置参数的
        Properties props = new Properties();
        // 使用的协议
        props.setProperty("mail.transport.protocol", "smtp");
        // 发件人邮箱的SMTP服务器地址
        props.setProperty("mail.smtp.host", myEmailSMTPServer);
        // 需要请求认证
        props.setProperty("mail.smtp.auth", "true");

        // 某些邮箱服务器要求SMTP连接需要使用SSL安全认证，下面代码是开启SSL安全认证
        final String smtpPort = "465";
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);


        // 根据配置创建会话对象，用于和邮件服务器交互
        Session session = Session.getDefaultInstance(props);
        // 设置为DEBUG模式，可以查看详细的发送log
        session.setDebug(true);


        // 创建一封邮件
        MimeMessage message = createMessage(session, myEmail, receieveMail, info);

        // 根据Session获取邮件传输对象
        Transport transport = session.getTransport();
        transport.connect(myEmail, myEmailPwd);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
    public  MimeMessage createMessage(Session session, String sendMail, String receieveMail, String info) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sendMail, "南工驿站", "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receieveMail, "xx用户", "UTF-8"));
        message.setSubject("欢迎来到南工驿站", "UTF-8");
        message.setContent(info, "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

}
