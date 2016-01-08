package com.grishberg.mobileserver.service.http.multipart;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by g on 07.01.16.
 */
public class MultipartParser {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final int STATE_READ_HEADERS = 1;
    public static final int STATE_READ_CONTENT = 2;

    public static List<MultipartContainer> parse(HttpRequest request) {
        List<MultipartContainer> multipart = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

            if (entity != null) {
                Header[] headers = request.getHeaders(CONTENT_TYPE);
                String boundary = getBoundary(headers);
                multipart = extractContentFromEntity(boundary, entity);
            }
        }
        return multipart;
    }

    private static List<MultipartContainer> extractContentFromEntity(String boundary, HttpEntity entity) {
        List<MultipartContainer> result = new ArrayList<>();

        byte[] boundaryArray = boundary.getBytes();
        byte[] rnrn = new byte[]{0x0D, 0x0A, 0x0D, 0x0A};
        byte[] rn = new byte[]{0x0D, 0x0A};
        byte[] end = new byte[]{0x2D, 0x2D};

        InputStream is = null;
        byte[] buffer = null;
        try {
            is = entity.getContent();
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int offset = 0;

        int state = 0;
        int len = buffer.length;
        int startContentPos = -1;
        int endContentPos = -1;
        int pos = 0;
        int headerEnd = 0;
        String name = "";
        String fileName = "";
        while (true) {
            if (startContentPos >= 0) {
                int buflen = endContentPos - startContentPos;
                if (buflen > 0) {
                    byte[] buf = new byte[buflen];
                    System.arraycopy(buffer, startContentPos, buf, 0, buflen);
                    result.add(new MultipartContainer(buf, name, fileName));
                    offset = endContentPos;
                }
            }
            // поиск начала блока
            pos = findBoundary(buffer, boundaryArray, offset);
            if (pos < 0) {
                break;
            }
            offset = pos + boundaryArray.length + 2;
            pos += boundaryArray.length + 2;
            state = STATE_READ_HEADERS;

            // поиск начала данных
            headerEnd = findBoundary(buffer, rnrn, offset);
            if (headerEnd < 0) {
                break;
            }
            offset = headerEnd + rnrn.length;
            String fields[] = readMultipartHeaders(buffer, pos, headerEnd);
            if (fields != null && fields.length > 1) {
                name = fields[0];
                fileName = fields[1];
            }
            startContentPos = offset;
            state = STATE_READ_CONTENT;
            endContentPos = findBoundary(buffer, boundaryArray, offset);
            endContentPos -= 2;
        }
        return result;
    }

    private static String getBoundary(Header[] headers) {
        String result = null;
        if (headers == null || headers.length < 1) {
            return null;
        }
        int pos = headers[0].getValue().indexOf('=');
        if (pos >= 0) {
            result = headers[0].getValue().substring(pos + 1);
        }
        return "--" + result;
    }

    private static int findBoundary(byte[] arr, byte[] b, int startPos) {
        int cmp = 0;
        for (int i = startPos; i < arr.length - b.length; i++) {
            cmp = 0;
            for (int j = 0; j < b.length; j++) {
                if (arr[i + j] != b[j]) {
                    break;
                }
                cmp++;
            }
            if (cmp == b.length) {
                return i;
            }
        }
        return -1;
    }

    private static String[] readMultipartHeaders(byte[] buf, int start, int end) {
        String name = "";
        String fileName = "";
        int len = end - start + 1;
        byte[] b = new byte[len];
        System.arraycopy(buf, start, b, 0, len);
        String buffer = new String(b);
        String[] arr = buffer.split("\r\n");
        int pos = 0;
        int endPos = 0;
        int quote = 0;
        for (String s : arr) {
            if (s.startsWith("Content-Disposition:")) {
                String[] values = s.split(";");
                for (String headerValue : values) {
                    if (headerValue.trim().startsWith("name=")) {
                        pos = headerValue.indexOf('=');
                        endPos = headerValue.length() - 1;
                        quote = headerValue.indexOf('"', pos);
                        if (quote >= 0) {
                            pos = quote;
                            endPos = headerValue.indexOf('"', pos + 1);
                        }
                        if (pos >= 0) {
                            name = headerValue.substring(pos + 1, endPos);
                        }
                    }
                    if (headerValue.trim().startsWith("filename=")) {
                        pos = headerValue.indexOf('=');
                        endPos = headerValue.length() - 1;
                        quote = headerValue.indexOf('"', pos);
                        if (quote >= 0) {
                            pos = quote;
                            endPos = headerValue.indexOf('"', pos + 1);
                        }
                        if (pos >= 0) {
                            fileName = headerValue.substring(pos + 1, endPos);
                        }
                    }
                }
            }
        }
        return new String[]{name, fileName};
    }
}
