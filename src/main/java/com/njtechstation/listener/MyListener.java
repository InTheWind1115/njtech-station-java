package com.njtechstation.listener;

import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/**
 * 监听器类:主要任务是用ServletRequest将我们的HttpSession携带过去
 * 否则websocket会报错
 * @author Liucheng
 */
@Component
public class MyListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpSession session = ((HttpServletRequest) sre.getServletRequest()).getSession();
    }

    @Override
    public void requestDestroyed(ServletRequestEvent arg0)  {}
}
