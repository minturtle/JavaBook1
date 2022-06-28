package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
            // 3.4.3
            // POST 방식으로 회원가입 하기
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(in));

            ArrayList<String> headers = new ArrayList<>();

            //헤더를 모두 읽어 오기
            while(true){
                String line = requestReader.readLine();
                if(line == null || line.isEmpty()) break;
                headers.add(line);
            }
            String uri = headers.get(0).split(" ")[1];

            if(uri.equals("/")){
                uri = "/index.html";
                byte[] body = Files.readAllBytes(Path.of("./webapp" + uri));
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            else if(uri.contains("/user/create")){
                String s = headers.stream().filter(x -> x.contains("Content-Length")).findFirst().get();
                int len = Integer.parseInt(s.split(":")[1].trim());
                String body = IOUtils.readData(requestReader, len);
                Map<String, String> userFormData = HttpRequestUtils.parseQueryString(body);

                User user = new User(userFormData.get("userId"), userFormData.get("password")
                        , userFormData.get("name"), userFormData.get("email"));

                responseHeader(dos, 302, null);
                dos.flush();
            }

            byte[] body = Files.readAllBytes(Path.of("./webapp" + uri));
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseHeader(DataOutputStream dos, int status, ArrayList<String> headers){
        String httpStatus = status + " ";
        if(status == 200) httpStatus += "OK";
        else if(status == 302) httpStatus += "Found";

        try {
            dos.writeBytes("HTTP/1.1 " + httpStatus + "/r/n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");

            if(status == 302) dos.writeBytes("Location: http://localhost:8080/");

            dos.writeBytes("\r\n");
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
