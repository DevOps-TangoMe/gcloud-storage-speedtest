package me.tango.devops.task;


import me.tango.devops.google.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

public class GoogleSDKUploadTask extends UploadTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSDKUploadTask.class);

    public GoogleSDKUploadTask(String region, String bucket, byte[] data) {
        super(region, bucket, data);
    }

    @Override
    public void run() {
        final String key = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();

        boolean success = StorageManager.putBytes(bucket, key, data);

        long finish = System.currentTimeMillis();

        long uploadTime = finish - start;

        if (!success) {
            result = new UploadTaskResult(success, uploadTime, 0);
            return;
        }

        start = System.currentTimeMillis();

        byte[] returnData = StorageManager.getBytes(bucket, key);

        finish = System.currentTimeMillis();

        long downloadTime = finish - start;

        LOGGER.debug("Download task to {} finished in {} ms", bucket, downloadTime);

        result =
            new UploadTaskResult(success && returnData != null && Arrays.equals(data, returnData),
                uploadTime, downloadTime);

        StorageManager.deleteBytes(bucket, key);

        LOGGER.debug("Delete task to {} finished", bucket);
    }

}
