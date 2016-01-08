package com.grishberg.mobileserver.service.http.multipart;

/**
 * Created by g on 19.11.15.
 */
public class MultipartContainer {
    private byte[] data;
    private String name;
    private String fileName;

    public MultipartContainer(byte[] data, String name, String fileName) {
        this.data = data;
        this.name = name;
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }
}
