package com.ljw.socket.socket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.util.concurrent.Executors;

/**
 * @author: lujunwei
 * @time: 9:24 2019/5/31
 * @des:
 */
@Component
@ServerEndpoint("/log/{command}")
public class WebSocketWs {

    private Process process;
    private InputStream inputStream;

    @OnOpen
    public void onOpen(@PathParam("command") String command, Session session) throws IOException {
        try {
            String[] commands = {"cmd", "/C", command.replaceAll("\\^", "/").replaceAll("`", "\n")};//windows  linux使用时，可以创建shell脚本执行
            process = Runtime.getRuntime().exec(commands);
            inputStream = process.getInputStream();
            Executors.newFixedThreadPool(4).execute(new Runnable() {
                private BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            session.getBasicRemote().sendText(line + "</br>");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("============= " + e.getMessage() + " ==============");
                        try {
                            session.getBasicRemote().sendText(e.getMessage() + "</br>");
                            System.out.println("============= " + e.getMessage() + " ==============");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            System.out.println("============= " + e1.getMessage() + " ==============");
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("============= " + e.getMessage() + " ==============");
            session.getBasicRemote().sendText(e.getMessage() + "</br>");
        }
    }

    @OnClose
    public void onClose() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            System.out.println("============= WebSocket资源已经关闭 ==============");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (process != null) {
            process.destroy();
            System.out.println("============= WebSocket资源已经销毁 ==============");
        }
    }

    @OnError
    public void onError(Throwable thr) {
        thr.printStackTrace();
        System.out.println("============= " + thr.getMessage() + " ==============");
    }
}

