package com.grishberg.httpserver.model;

import java.util.Date;

/**
 * Created by g on 21.12.15.
 */
public class ReportModel {
    public Date date;
    public String screenshot;
    public String message;
    public String email;
    public String device;
    public String platform;
    public String version;

    public ReportModel() {
    }

    public ReportModel(String screenshot, String message, String email
            , String device, String platform, String version) {
        this.screenshot = screenshot;
        this.message = message;
        this.email = email;
        this.device = device;
        this.platform = platform;
        this.version = version;
    }
}
