package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpRequest {
    private Map<String, String> headerMap;
    private String method;
    private String uri;
    private Map<String, String> body;

    public HttpRequest(InputStream request)throws IOException {
        BufferedReader requestReader = new BufferedReader(new InputStreamReader(request));
        parseHeader(requestReader);
        parseBody(requestReader);
    }
    //바디 파싱
    private void parseBody(BufferedReader requestReader) throws IOException {
        if(method.equals("GET")) return;

        int bodyLength = Integer.parseInt(headerMap.get("Content-Length"));
        String bodyString = IOUtils.readData(requestReader, bodyLength);
        body = HttpRequestUtils.parseQueryString(bodyString);
    }

    //헤더 파싱
    private void parseHeader(BufferedReader requestReader) throws IOException {
        String line = requestReader.readLine();
        if(line == null) return;

        String[] temp = line.split(" ");
        method = temp[0].trim().toUpperCase(Locale.ROOT);
        uri = temp[1].trim().equals("/") ? "/index.html" : temp[1].trim();

        headerMap = new HashMap<>();
        while(true){
            line = requestReader.readLine();
            if(line == null || line.isEmpty()) break;
            String[] header = line.split(": ");
            headerMap.put(header[0].trim(), header[1].trim());
        }
    }

    //헤더에서 입력받은 key로 조회해 value를 획득하는 메서드
    public String getHeader(String header){
        return headerMap.get(header);
    }

    //body에서 입력받은 key로 조회해 value를 획득하는 메서드
    public String getParameter(String key){
        if(body==null)return null;
        return body.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
