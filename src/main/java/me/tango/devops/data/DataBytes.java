package me.tango.devops.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;


public class DataBytes {
    private final static String URL = "ipv4.download.thinkbroadband.com";
    private final static String USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    public static byte[] getData(Size size) throws IOException {
        switch (size) {
            case SMALL:
                return get1KBData();
            case MEDIUM:
                return get5MBData();
            case BIG:
                return get10MBData();
            case HUGE:
                return get100MBData();
        }

        return null;
    }

    private static byte[] get1KBData() {
        byte[] bytes = new byte[1024];
        Random random = new Random();
        random.nextBytes(bytes);

        return bytes;
    }

    private static byte[] get5MBData() throws IOException {
        return download("5MB.zip");
    }

    private static byte[] get10MBData() throws IOException {
        return download("10MB.zip");
    }

    private static byte[] get100MBData() throws IOException {
        return download("100MB.zip");
    }

    private static byte[] download(String file) throws IOException {
        final URL url = new URL("http", URL, "/" + file);
        final HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.addRequestProperty("User-Agent", USER_AGENT);
        final InputStream in = httpConn.getInputStream();

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        int n = -1;

        while ((n = in.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        in.close();
        httpConn.disconnect();

        return output.toByteArray();
    }

    public static enum Size {
        SMALL, MEDIUM, BIG, HUGE
    }
}
