package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            // 3.4.3.1 요구사항 1
            // /index.html에 접속 했을 떄 webapp디렉토리의 index.html 파일 읽어 응답하기
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


            byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
            if(uri.equals("/index.html")){
                body = Files.readAllBytes(Path.of("./webapp" + uri));
            }

            response200Header(dos, body.length);
            responseBody(dos, body);
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
