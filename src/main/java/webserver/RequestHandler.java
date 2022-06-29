package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static Map<String, Controller> controllerMap;
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    static{
        controllerMap = new ConcurrentHashMap<>();
        controllerMap.put("/user/create", new AbstractController() {
            @Override
            public void doPost(HttpRequest req, HttpResponse res) {
                try {
                    User user = new User(req.getParameter("userId"), req.getParameter("password")
                            , req.getParameter("name"), req.getParameter("email"));
                    DataBase.addUser(user);
                    res.sendRedirect("/");
                }catch (IOException e){log.error(e.getMessage());}
            }
        });

        controllerMap.put("/user/login", new AbstractController() {
            @Override
            public void doPost(HttpRequest req, HttpResponse res) {
                try{
                    User findUser = DataBase.findUserById(req.getParameter("userId"));
                    if (isLoginSuccess(findUser, req.getParameter("password"))) {
                        res.addHeader("Set-Cookie", "isLogined=true; Path=/");
                        res.sendRedirect("/");
                    } else {
                        res.addHeader("Set-Cookie", "isLogined=false; Path=/");
                        res.sendRedirect("/user/login_failed.html");
                    }
                }catch (IOException e){log.error(e.getMessage());}

            }
        });
        controllerMap.put("/user/list", new AbstractController() {
            @Override
            public void doGet(HttpRequest req, HttpResponse res) {
                try{
                    String isLogined = req.getCookie("isLogined");
                    if(Boolean.parseBoolean(isLogined)){
                        Collection<User> users = DataBase.findAll();
                        StringBuilder sb = createHTMLData(users);
                        res.forward(sb.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    else{
                        res.sendRedirect("/user/login.html");
                    }
                }catch (IOException e){log.error(e.getMessage());}
            }
        });
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest req = new HttpRequest(in);
            HttpResponse res = new HttpResponse(out);

            Controller controller = controllerMap.get(req.getUri());
            if(controller == null){
                if(req.getUri().endsWith(".css")) res.forward(req.getUri(), HttpResponse.FileType.CSS);
                else if(req.getUri().equals(".js")) res.forward(req.getUri(),  HttpResponse.FileType.JS);
                else res.forward(req.getUri(), HttpResponse.FileType.HTML);
                return;
            }
            controller.service(req, res);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static StringBuilder createHTMLData(Collection<User> users) {

        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for(User user : users){
            sb.append("<tr>")
            .append("<td>").append(user.getUserId()).append("<td>")
            .append("<td>").append(user.getName()).append("<td>")
            .append("<td>").append(user.getEmail()).append("<td>")
            .append("</tr>");
        }
        sb.append("</table>");
        return sb;
    }

    private static boolean isLoginSuccess(User user, String pw){
        if(user == null) return false;
        if(!user.getPassword().equals(pw)) return false;
        return true;
    }
}
