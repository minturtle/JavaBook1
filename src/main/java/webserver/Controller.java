package webserver;

public interface Controller {
    void service(HttpRequest req, HttpResponse res);
}
