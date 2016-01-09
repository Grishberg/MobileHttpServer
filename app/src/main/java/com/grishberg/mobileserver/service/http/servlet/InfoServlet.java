package com.grishberg.mobileserver.service.http.servlet;

import org.apache.http.BaseApacheServlet;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.nio.charset.Charset;

/**
 * Created by grishberg on 09.01.16.
 */
public class InfoServlet extends BaseApacheServlet {
    @Override
    protected void doGet(HttpRequest request, HttpResponse response, HttpContext context) {
        super.doGet(request, response, context);
        HttpParams params = request.getParams();
        int id = params.getIntParameter("id", 0);
        NStringEntity responseEntity = null;
        String generatedBody = String.format("<html><body>info page...id=%d</body></html>", id);
        if (generatedBody != null) {
            responseEntity = new NStringEntity(generatedBody, Charset.forName("UTF-8"));
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
            responseEntity.setContentEncoding("UTF-8");
            response.setEntity(responseEntity);
        }

    }

    @Override
    protected void doPut(HttpRequest request, HttpResponse response, HttpContext context) {
        super.doPut(request, response, context);
        HttpParams params = request.getParams();
        int id = params.getIntParameter("id",0);

    }
}
