/**
 *  Copyright 2015 TangoMe Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
package me.tango.devops.data;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;


/** Download data. */
//CHECKSTYLE.OFF: IllegalCatch
public final class DataBytes {
    /** URL of files to download from. */
    private static final String URL = "ipv4.download.thinkbroadband.com";
    /** User agent. */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 "
        + "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    // Make it a utility class
    private DataBytes() {}

    /**
     * Download data.
     * @param size the size to download
     * @return an byte array
     * @throws IOException Network IO issue
     */
    public static byte[] getData(final Size size) throws IOException {
        switch (size) {
            case SMALL:
                return get1KBData();
            case MEDIUM:
                return get5MBData();
            case BIG:
                return get10MBData();
            case HUGE:
                return get100MBData();
            default:
                throw new IllegalArgumentException("Unrecognized size: " + size.toString());
        }
    }

    private static byte[] get1KBData() {
        final byte[] bytes = new byte[1024];
        final Random random = new Random();
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

    private static byte[] download(final String file) throws IOException {
        final URL url = new URL("http", URL, "/" + file);
        final HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.addRequestProperty("User-Agent", USER_AGENT);
        final InputStream input = httpConn.getInputStream();

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];

        for (int n = -1; (n = input.read(buffer)) != -1;) {
            output.write(buffer, 0, n);
        }

        input.close();
        httpConn.disconnect();

        return output.toByteArray();
    }

    /** Sizes of files. */
    public static enum Size {
        SMALL, MEDIUM, BIG, HUGE
    }
}
