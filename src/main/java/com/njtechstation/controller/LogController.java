package com.njtechstation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.njtechstation.aliyun.SendSms;
import com.njtechstation.domain.Message;
import com.njtechstation.domain.User;
import com.njtechstation.mapper.UserMapper;
import com.njtechstation.service.UserService;
import com.njtechstation.service.impl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Controller
public class LogController {

    @Autowired
    private UserService userService;

    @Resource
    private SendSms sendSms;

    @PostMapping(value = "/signin")
    @ResponseBody
    public String signIn(HttpServletRequest request, String phone, String userPwd) {
        System.out.println(phone + userPwd);
        if (phone != null && userPwd != null) {
            HttpSession httpSession = request.getSession();
            System.out.println("phone:" + phone);
            System.out.println("userPwd:" + userPwd);
            System.out.println("登录时候的httpsession" + httpSession);
            String password = userService.queryPassword(phone);
            String email = userService.queryMailByPhone(phone);
            if (email != null)
                httpSession.setAttribute("email", email);
            if (userPwd.equals(password)) {
                httpSession.setAttribute("phone", phone);
                System.out.println("httpsession中的phone" + httpSession.getAttribute("phone"));
                return "true";
            }
        }
        return "false";
    }


    /**
     * @author Liucheng
     * @function 完成注册功能
     * @param request
     * @param username 用户名称
     * @param userPwd   用户密码
     * @param phone 用户手机号
     * @return 成功注册，返回1 验证码失效返回0 验证码错误返回-1 和注册码时发送的手机号不一样返回-2
     */

    @RequestMapping("/signup")
    @ResponseBody
    public int signUP(HttpServletRequest request, String username, String userPwd, String phone, String code) {
        // 查看验证码是否以过期
        HttpSession httpSession = request.getSession();
        System.out.println("注册时候的httpsession" + httpSession);
        String check = (String) httpSession.getAttribute("checkCode");
        String phoneNum = (String) httpSession.getAttribute("phone");
        if (phoneNum != null && !phoneNum.equals(phone))
            return -2;
        if (check == null)
            return 0;
        if (!check.equals(code))
            return -1;
        User user = new User();
        user.setUserName(username);
        user.setUserPwd(userPwd);
        user.setPhone(phone);
        return userService.insertUser(user);
    }

    /**
     * @author Liucheng
     * @function 完成返回验证码功能
     * @param request
     * @param phone
     * @return 1：成功 0：此用户已存在
     */
    @RequestMapping("/phonecode")
    @ResponseBody
    public int getPhoneCode(HttpServletRequest request, String phone) {
        String isExist = userService.queryPassword(phone);
        if (isExist != null)
            return 0;
        String code = this.randomCode();
        sendSms.sendMessage(phone, code);
        HttpSession httpSession = request.getSession();
        System.out.println("获取验证码时候的httpsession" + httpSession);
        httpSession.setAttribute("checkCode", code);
        httpSession.setAttribute("phone", phone);
        String check = (String) httpSession.getAttribute("checkCode");
        String phoneNum = (String) httpSession.getAttribute("phone");

        // 实现5分钟的定时器，验证码5分钟后失效
        final Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                httpSession.removeAttribute("checkCode");
                httpSession.removeAttribute("phone");
                timer.cancel();
            }
        },5 * 60 * 1000);
        return 1;
    }

    public String randomCode() {
        String code = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code += random.nextInt(10);
        }
        return code;
    }


    @RequestMapping(value = "/sendmailcode")
    @ResponseBody
    public String sendMailCode(HttpServletRequest request, String email) throws JsonProcessingException, UnsupportedEncodingException, MessagingException {

        Message message = new Message();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("!!!!!!!!!" + email);

        if (email == null) {
            message.setType(-2);
            message.setContent("未知错误，请刷新后再次尝试。");
            return objectMapper.writeValueAsString(message);
        }

        if (!email.endsWith("@njtech.edu.cn")) {

            message.setType(-1);
            message.setContent("只支持南京工业大学邮箱验证。");
            return objectMapper.writeValueAsString(message);
        }

        String mailQuery = userService.queryNameByEmail(email);
        if (mailQuery != null) {
            message.setType(0);
            message.setContent("邮箱已被注册，若不是您的操作，请与管理员联系。");
            return objectMapper.writeValueAsString(message);
        }

        // 获取6位数验证码，然后发送到用户的学校邮箱
        String codeSix = randomCode();
        EmailServiceImpl emailService = new EmailServiceImpl();
        emailService.setReceieveMail(email);
        emailService.setInfo("【南工驿站】您的验证码为：" + codeSix +"，验证码五分钟内有效。");
        emailService.send();

        // 将验证码写入session中的域属性中，五分钟后将之移除
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("emailCode", codeSix);
        httpSession.setAttribute("email", email);
        final Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                httpSession.removeAttribute("emailCode");
                httpSession.removeAttribute("email");
                timer.cancel();
            }
        },5 * 60 * 1000);

        message.setType(1);
        message.setContent("验证码发送成功，请到学校邮箱中查看。");
        return objectMapper.writeValueAsString(message);
    }

    @RequestMapping(value = "/confirmmailcode")
    @ResponseBody
    public String confirmMailCode(HttpServletRequest request, String code) throws JsonProcessingException {
        Message message = new Message();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpSession httpSession = request.getSession();

        String checkCode = (String) httpSession.getAttribute("emailCode");
        if (checkCode == null) {
            message.setType(-1);
            message.setContent("验证码失效，请重新获取验证码。");
            return objectMapper.writeValueAsString(message);
        }

        if(!checkCode.equals(code)) {
            message.setType(0);
            message.setContent("验证码错误，请输入正确的验证码。");
            return objectMapper.writeValueAsString(message);
        }

        int num = userService.insertEmailByphone((String) httpSession.getAttribute("email"), (String) httpSession.getAttribute("phone"));

        if (num != 0) {
            message.setType(1);
            message.setContent("学校邮箱验证成功。");
            return objectMapper.writeValueAsString(message);
        }

        message.setType(-2);
        message.setContent("未知错误，请刷新后再次尝试。");
        return objectMapper.writeValueAsString(message);
    }

}
