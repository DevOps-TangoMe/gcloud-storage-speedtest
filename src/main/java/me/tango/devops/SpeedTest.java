package me.tango.devops;

import me.tango.devops.google.StorageManager;
import me.tango.devops.task.GoogleSDKUploadTask;
import me.tango.devops.task.UploadTask;
import me.tango.devops.task.UploadTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by programmer on 8/10/15.
 */
public class SpeedTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeedTest.class);

    private final int rounds;
    private final byte[] data;
    private final UploadTaskType uploadType;

    // region -> time
    private final Map<String, List<Long>> uploadTimings;
    private final Map<String, List<Long>> downloadTimings;

    public SpeedTest(int rounds, byte[] data, UploadTaskType uploadType) {
        this.rounds = rounds;
        this.data = data;
        this.uploadType = uploadType;

        this.uploadTimings = new HashMap<String, List<Long>>();
        this.downloadTimings = new HashMap<String, List<Long>>();
    }


    public void start() {
        init();

        final Map<String, String> buckets = StorageManager.getBuckets();

        for (int i = 0; i < rounds; i++) {
            LOGGER.debug("*** Round {}/{} ***", i + 1, rounds);

            List<String> regions = Arrays.asList(StorageManager.REGIONS);
            Collections.shuffle(regions);

            for (final String region : regions) {
                LOGGER.debug("About to upload in region {}", region);

                final UploadTask uploadTask =
                    new GoogleSDKUploadTask(region, buckets.get(region), data);

                uploadTask.run();

                UploadTask.UploadTaskResult result = uploadTask.getResult();

                if ((result != null) && (result.isSuccess())) {
                    uploadTimings.get(region).add(Long.valueOf(result.getUploadTime()));
                    downloadTimings.get(region).add(Long.valueOf(result.getDownloadTime()));
                }
            }
        }
    }

    public Map<String, List<Long>> getUploadTimings() {
        return uploadTimings;
    }

    public Map<String, List<Long>> getDownloadTimings() {
        return downloadTimings;
    }

    private void init() {
        for (String region : StorageManager.REGIONS) {
            List<Long> list = new ArrayList<Long>();
            this.uploadTimings.put(region, list);
            this.downloadTimings.put(region, list);
        }

        StorageManager.initBuckets(false);
    }
}
