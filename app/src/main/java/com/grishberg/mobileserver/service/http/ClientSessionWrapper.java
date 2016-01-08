package com.grishberg.mobileserver.service.http;

/**
 * Created by grishberg on 08.01.16.
 */

import com.grishberg.mobileserver.service.http.multipart.MultipartContainer;
import com.grishberg.mobileserver.service.http.multipart.MultipartParser;

import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Обрабатывает запрос клиента.
 */
public class ClientSessionWrapper implements HttpAsyncRequestHandler<org.apache.http.HttpRequest> {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final int STATE_READ_HEADERS = 1;
    public static final int STATE_READ_CONTENT = 2;
    private String mCurrentDir;

    public ClientSessionWrapper() {
        super();
        mCurrentDir = System.getProperty("user.dir");
    }

    public HttpAsyncRequestConsumer<org.apache.http.HttpRequest> processRequest(
            final org.apache.http.HttpRequest request,
            final HttpContext context) {
        // Buffer request content in memory for simplicity
        return new BasicAsyncRequestConsumer();
    }

    public void handle(
            final org.apache.http.HttpRequest request,
            final HttpAsyncExchange httpexchange,
            final HttpContext context) throws HttpException, IOException {
        HttpResponse response = httpexchange.getResponse();
        handleInternal(request, response, context);
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(
            final org.apache.http.HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        if (checkCommands(request, response, context)) {
            return;
        }
    }

    private boolean checkCommands(org.apache.http.HttpRequest request
            , HttpResponse response, HttpContext context) throws IOException {
        String target = request.getRequestLine().getUri();
        List<MultipartContainer> multipart = MultipartParser.parse(request);
        /*
        if (mBodyGenerator != null) {
            String generatedBody = mBodyGenerator.generateBody(target, multipart);
            NStringEntity responseEntity = null;
            if (generatedBody != null) {
                responseEntity = new NStringEntity(generatedBody, Charset.forName("UTF-8"));
                response.setHeader("Content-Type", "text/html; charset=UTF-8");
                responseEntity.setContentEncoding("UTF-8");
                response.setEntity(responseEntity);
            } else {
                // Попытаться прочитать файл
                if (target.startsWith("/")) {
                    target = target.substring(1);
                }
                final File file = new File(mCurrentDir, URLDecoder.decode(target, "UTF-8"));
                if (file.exists() && file.canRead()) {
                    HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                    HttpConnection conn = coreContext.getConnection(HttpConnection.class);
                    response.setStatusCode(HttpStatus.SC_OK);
                    NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
                    response.setEntity(body);
                } else {
                    // ничего не найдено
                    responseEntity = new NStringEntity("{\"success\":false}", Charset.forName("UTF-8"));
                    response.setHeader("Content-Type", "text/html; charset=UTF-8");
                    responseEntity.setContentEncoding("UTF-8");
                    response.setEntity(responseEntity);
                }
            }
            return true;
        }
*/
        return false;
    }
}