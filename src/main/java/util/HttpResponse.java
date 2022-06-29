package util;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    public HttpResponse(DataOutputStream dos) {
        headers = new HashMap<>();
        this.dos = dos;
    }

    public HttpResponse addHeader(String key, String value){
        if(!headers.containsKey(key)) headers.put(key, value);
        return this;
    }

    //filePath를 입력받아 그 file을 전송 하는 메서드
    public void forward(String filePath,FileType fileType) throws IOException {
        byte[] body = Files.readAllBytes(Path.of("./webapp", filePath));
        headers.put("Content-Type", fileType.getContentType());
        headers.put("Content-Length", Integer.toString(body.length));

        sendResponse("HTTP/1.1 200 OK \r\n", body);
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
