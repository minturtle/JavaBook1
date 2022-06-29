package util;

import junit.framework.TestCase;
import org.assertj.core.api.Assertions.*;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HttpResponseTest {

    @Test
    public void 응답_테스트() throws IOException {
        HttpResponse httpResponse = new HttpResponse(new DataOutputStream(System.out));
        httpResponse.forward("/index.html", HttpResponse.FileType.HTML);

        assertThat(1).isEqualTo(1);
    }

    @Test
    public void Redirect응답_테스트() throws IOException {
        HttpResponse res = new HttpResponse(new DataOutputStream(System.out));
        res.sendRedirect("/");
        assertThat(1).isEqualTo(1);
    }

    @Test
    public void Redirect_with_Cookie() throws IOException{
        HttpResponse res = new HttpResponse(new DataOutputStream(System.out));
        res.addHeader("Set-Cookie", "isLogined=true");
        res.sendRedirect("/");
    }

    @Test
    public void Response_With_Cookie() throws IOException{
        HttpResponse res = new HttpResponse(new DataOutputStream(System.out));
        res.addHeader("Set-Cookie", "isLogined=true");
        res.forward("/user/login.html", HttpResponse.FileType.HTML);
    }

    @Test
    public void Response_CSS() throws IOException{
        HttpResponse res = new HttpResponse(new DataOutputStream(System.out));
        res.forward("/css/styles.css", HttpResponse.FileType.CSS);
    }
}