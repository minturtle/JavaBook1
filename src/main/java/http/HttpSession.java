package http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    Map<String, Object> sessionMap = new HashMap<>();
    private final String name;

    public HttpSession() {
        name = UUID.randomUUID().toString();
    }

    public String getId(){
        return name;
    }

    public void setAttribute(String name, Object value){
        sessionMap.put(name, value);
    }

    public Object getAttribute(String name){
        return sessionMap.get(name);
    }

    void removeAttribute(String name){
        sessionMap.remove(name);
    }

    void invalidate(){
        sessionMap.clear();
    }

}
