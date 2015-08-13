package me.tango.devops.task;


import me.tango.devops.google.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

/** Upload and download data to test the speed. */
public class GoogleSDKUploadTask extends AbstractUploadTask {
    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSDKUploadTask.class);

    /** Constructor. */
    public GoogleSDKUploadTask(final String region, final String bucket, final byte[] data) {
        super(region, bucket, data);
    }

    /**
     * Upload and download.
     */
    @Override
    public void run() {
        final String key = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();

        final boolean success = StorageManager.putBytes(bucket, key, data);

        long finish = System.currentTimeMillis();

        final long uploadTime = finish - start;

        if (!success) {
            result = new UploadTaskResult(success, uploadTime, 0);
            return;
        }

        start = System.currentTimeMillis();

        final byte[] returnData = StorageManager.getBytes(bucket, key);

        finish = System.currentTimeMillis();

        final long downloadTime = finish - start;

        LOGGER.debug("Download task to {} finished in {} ms", bucket, downloadTime);

        result =
            new UploadTaskResult(success && returnData != null && Arrays.equals(data, returnData),
                uploadTime, downloadTime);

        StorageManager.deleteBytes(bucket, key);

        LOGGER.debug("Delete task to {} finished", bucket);
    }

}
