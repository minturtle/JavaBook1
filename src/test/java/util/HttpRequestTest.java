package util;
import org.assertj.core.api.Assertions.*;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HttpRequestTest {

    @Test
    public void HTTP_GET요청_파싱() throws IOException {
        InputStream in = new FileInputStream("./src/main/resources/http_get.txt");
        HttpRequest req = new HttpRequest(in);

        assertThat(req.getUri()).isEqualTo("/index.html");
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getHeader("Accept")).isEqualTo("text/html,application/xml,application/xhtml+xml");
        assertThat(req.getHeader("Accept-Encoding")).isEqualTo("gzip, deflate, br");
        assertThat(req.getHeader("Host")).isEqualTo("localhost:8080");
    }

    @Test
    public void HTTP_POST요청_파싱() throws IOException{
        InputStream in = new FileInputStream("./src/main/resources/http_post.txt");
        HttpRequest req = new HttpRequest(in);

        assertThat(req.getUri()).isEqualTo("/user/login");
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getParameter("userId")).isEqualTo("kim");
        assertThat(req.getParameter("password")).isEqualTo("1111");
    }

}
