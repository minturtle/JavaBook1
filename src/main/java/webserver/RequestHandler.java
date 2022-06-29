package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(in)); //request 스트림
            DataOutputStream dos = new DataOutputStream(out); //response 응답 스트림

            //헤더 파싱
            String line = requestReader.readLine();
            if(line == null) return;

            String[] temp = line.split(" ");
            String method = temp[0].trim().toUpperCase(Locale.ROOT);
            String uri = temp[1].trim().equals("/") ? "/index.html" : temp[1].trim();

            Map<String, String> headerMap = new HashMap<>();
            while(true){
                line = requestReader.readLine();
                if(line == null || line.isEmpty()) break;
                String[] header = line.split(":");
                headerMap.put(header[0].trim(), header[1].trim());
            }

            //uri에 따른 처리
            if(method.equals("POST") && uri.equals("/user/create")){
                int bodyLength = Integer.parseInt(headerMap.get("Content-Length"));
                String body = IOUtils.readData(requestReader, bodyLength);

                Map<String, String> userDataMap = HttpRequestUtils.parseQueryString(body);
                User user = new User(userDataMap.get("userId"), userDataMap.get("password")
                        , userDataMap.get("name"), userDataMap.get("email"));
                DataBase.addUser(user);
                response302Header(dos, "/");
            }
            else if(method.equals("POST") && uri.equals("/user/login")){
                int bodyLength = Integer.parseInt(headerMap.get("Content-Length"));
                String body = IOUtils.readData(requestReader, bodyLength);
                Map<String, String> userDataMap = HttpRequestUtils.parseQueryString(body);

                User findUser = DataBase.findUserById(userDataMap.get("userId"));
                ArrayList<String> header = new ArrayList<>();
                if(isLoginSuccess(findUser, userDataMap.get("password"))){
                    header.add("Set-Cookie: isLogined=true; Path=/" );
                    response302Header(dos, "/", header);
                }
                else{
                    header.add("Set-Cookie: isLogined=false; Path=/" );
                    response302Header(dos, "/user/login_failed.html", header);
                }
            }
            else if(method.equals("GET") && uri.equals("/user/list")){
                Map<String, String> cookieMap = HttpRequestUtils.parseCookies(headerMap.get("Cookie"));
                String isLogined = cookieMap.get("isLogined");
                if( Boolean.parseBoolean(isLogined)){
                    Collection<User> users = DataBase.findAll();
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
                    byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
                else{
                    response302Header(dos, "/user/login.html");
                }
            }
            else if(method.equals("GET") && uri.endsWith(".css")){
                byte[] bytes = Files.readAllBytes(Path.of("./webapp", uri));
                response200HeaderCSS(dos, bytes.length);
                responseBody(dos, bytes);
            }
            else{
                byte[] bytes = Files.readAllBytes(Path.of("./webapp", uri));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLoginSuccess(User user, String pw){
        if(user == null) return false;
        if(!user.getPassword().equals(pw)) return false;
        return true;
    }
    private void response302Header(DataOutputStream dos, String redirectLocation){
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + redirectLocation + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302Header(DataOutputStream dos, String redirectLocation, ArrayList<String> headers){
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + redirectLocation + "\r\n");
            for (String header : headers) {
                dos.writeBytes(header + "\r\n");
            }
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
