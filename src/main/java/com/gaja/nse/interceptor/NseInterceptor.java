package com.gaja.nse.interceptor;

import com.gaja.nse.utils.NSECookieHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;

@Component
public class NseInterceptor implements ClientHttpRequestInterceptor {
    private final NSECookieHandler nseCookieHandler;
    private final RetryTemplate retryTemplate;

    @Autowired
    public NseInterceptor(NSECookieHandler nseCookieHandler, RetryTemplate retryTemplate) {
        this.nseCookieHandler = nseCookieHandler;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        return retryTemplate.execute(retryContext -> handleRequest(request, body, execution));
    }

    private ClientHttpResponse handleRequest(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        List<String> cookie = nseCookieHandler.getCookie(request.getURI().getPath());
        request.getHeaders().put("Cookie", cookie != null ? cookie : nseCookieHandler.getHomePageCookie());
        try {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is5xxServerError()
                    || response.getStatusCode().is4xxClientError()) {
                throw new RestClientException(response.getStatusText());
            }
            return response;
        } catch (RestClientException | IOException e) {
            System.out.println("Failed with msg: " + e.getMessage());
            nseCookieHandler.reInitialize(request.getURI().getPath());
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
