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
