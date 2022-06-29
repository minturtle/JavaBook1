package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    public enum FileType{
        HTML("text/html"), CSS("text/css"), JS("text/javascript");
        private String contentType;

        public String getContentType() {
            return contentType;
        }

        FileType(String contentType) {
            this.contentType = contentType;
        }
    }

    private Map<String, String> headers;
    private DataOutputStream dos;

    public HttpResponse(OutputStream os) {
        headers = new HashMap<>();
        this.dos = new DataOutputStream(os);
    }

    public HttpResponse addHeader(String key, String value){
        if(!headers.containsKey(key)) headers.put(key, value);
        return this;
    }

    //filePath를 입력받아 그 file을 전송 하는 메서드
    public void forward(String filePath,FileType fileType) throws IOException {
        byte[] body;
        String head;
        try{
            body = Files.readAllBytes(Path.of("./webapp", filePath));
            head = "HTTP/1.1 200 OK \r\n";
        }catch (NoSuchFileException e){
            body = "파일을 찾을 수 없습니다".getBytes(StandardCharsets.UTF_8);
            head = "HTTP/1.1 404 Not Found \r\n";
            fileType = FileType.HTML;
        }

        headers.put("Content-Type", fileType.getContentType());
        headers.put("Content-Length", Integer.toString(body.length));

        sendResponse(head, body);
    }
    public void forward(byte[] body) throws IOException {
        String head="HTTP/1.1 200 OK \r\n";

        headers.put("Content-Type", "text/html");
        headers.put("Content-Length", Integer.toString(body.length));

        sendResponse(head, body);
    }



    private void sendResponse(String head, byte[] body) throws IOException {
        // 헤더 작성
        dos.writeBytes(head);
        for(String key : headers.keySet()){
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
        dos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        if(body != null) dos.write(body, 0, body.length);
        dos.flush();

    }

    //302 redirect를 응답으로 보내는 메서드
    public void sendRedirect(String redirectURI) throws IOException {
        addHeader("Location", redirectURI);
        sendResponse("HTTP/1.1 302 Redirect \r\n", null);
    }


}
