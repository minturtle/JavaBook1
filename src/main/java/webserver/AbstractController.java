package webserver;

import java.io.IOException;

public abstract class AbstractController implements Controller{
    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        if(req.getMethod().equals("POST")){
            doPost(req, res);
        }
        else{
            doGet(req, res);
        }
    }

    public void doPost(HttpRequest req, HttpResponse res){};
    public void doGet(HttpRequest req, HttpResponse res){};
}
