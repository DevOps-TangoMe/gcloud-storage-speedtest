package me.tango.devops;

import me.tango.devops.data.DataBytes;
import me.tango.devops.google.CredentialsManager;
import me.tango.devops.google.StorageManager;
import me.tango.devops.task.UploadTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Google Cloud Storage upload speed test - By Tango");
            System.out.println("Usage: client_secret_file project_id ROUNDS SMALL|MEDIUM|BIG|HUGE");
            return;
        }

        try {
            CredentialsManager.setup(args[0]);
        } catch (Exception e) {
            LOGGER.error("CredentialsManager.setup() failed");
            LOGGER.error(e.getMessage());
            return;
        }

        try {
            StorageManager.setup(args[1]);
        } catch (Exception e) {
            LOGGER.error("StorageManager.setup() failed");
            LOGGER.error(e.getMessage());
            return;
        }

        StorageManager.initBuckets(true);

        final int rounds = Integer.parseInt(args[2]);
        final byte[] data = DataBytes.getData(DataBytes.Size.valueOf(args[3]));

        final UploadTaskType uploadType = UploadTaskType.valueOf("SDK");

        LOGGER.debug("Starting test");

        final SpeedTest speedTest = new SpeedTest(rounds, data, uploadType);
        speedTest.start();

        LOGGER.debug("Test finished");

        StorageManager.deleteBuckets();

        LOGGER.debug("Upload results");
        printResults(speedTest.getUploadTimings());
        LOGGER.debug("Download results");
        printResults(speedTest.getDownloadTimings());
    }

    private static void printResults(Map<String, List<Long>> timings) {
        for (final String region : StorageManager.REGIONS) {
            LOGGER.debug("RegionName: '{}'", region);

            long sum = 0;

            List<Long> regionTimings = timings.get(region);

            if (regionTimings.isEmpty()) {
                LOGGER.debug("Skipping: No results for region {}", region);
                continue;
            }

            if (regionTimings.size() > 1) {
                Collections.sort(regionTimings);
            }

            // remove min and max
            if (regionTimings.size() > 2) {
                regionTimings.remove(0);
                regionTimings.remove(regionTimings.size() - 1);
            }

            int timingsCount = regionTimings.size();

            for (Long time : timings.get(region)) {
                sum += time.longValue();
            }

            double avg = sum / (double) timingsCount;
            double median;
            {
                int middle = timingsCount / 2;
                if (timingsCount == 1) {
                    median = regionTimings.get(0);
                } else if (timingsCount % 2 == 1) {
                    median = regionTimings.get(middle);
                } else {
                    median = (regionTimings.get(middle - 1) + regionTimings.get(middle)) / 2.0;
                }
            }

            LOGGER.info("Region {}: {} valid tasks. lowest: {} ms, highest: {} ms. Average: {} ms, "
                    + "median: {} ms.", region, timingsCount, regionTimings.get(0),
                regionTimings.get(timingsCount - 1), avg, median);
        }
    }
}
