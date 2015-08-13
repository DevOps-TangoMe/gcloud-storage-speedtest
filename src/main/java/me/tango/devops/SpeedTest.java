package me.tango.devops;

import me.tango.devops.data.DataBytes;
import me.tango.devops.google.CredentialsManager;
import me.tango.devops.google.StorageManager;
import me.tango.devops.task.AbstractUploadTask;
import me.tango.devops.task.GoogleSDKUploadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * The main class to run tests.
 */
@SuppressWarnings({"PMD.ArrayIsStoredDirectly"})
public class SpeedTest {
    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeedTest.class);

    /** Number of iterations to run. */
    private final transient int rounds;
    /** Data used to upload. */
    private final transient byte[] data;

    // region -> time
    /** Upload time of each region. */
    private final Map<String, List<Long>> uploadTimings;
    /** Download time of each regin. */
    private final Map<String, List<Long>> downloadTimings;

    /**
     * Constructor.
     * @param rounds iterations to run
     * @param data  data to be uploaded
     */
    public SpeedTest(final int rounds, final byte[] data) {
        this.rounds = rounds;
        this.data = data;

        this.uploadTimings = new HashMap<String, List<Long>>();
        this.downloadTimings = new HashMap<String, List<Long>>();
    }


    /** start to run tests. */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void start() {
        init();

        final Map<String, String> buckets = StorageManager.getBuckets();

        for (int i = 0; i < rounds; i++) {
            LOGGER.debug("*** Round {}/{} ***", i + 1, rounds);

            final List<String> regions = Arrays.asList(StorageManager.REGIONS);
            Collections.shuffle(regions);

            for (final String region : regions) {
                LOGGER.debug("About to upload in region {}", region);

                final AbstractUploadTask uploadTask =
                    new GoogleSDKUploadTask(region, buckets.get(region), data);

                uploadTask.run();

                final AbstractUploadTask.UploadTaskResult result = uploadTask.getResult();

                if ((result != null) && (result.isSuccess())) {
                    uploadTimings.get(region).add(Long.valueOf(result.getUploadTime()));
                    downloadTimings.get(region).add(Long.valueOf(result.getDownloadTime()));
                }
            }
        }
    }

    /** Get upload time of all regions. */
    public Map<String, List<Long>> getUploadTimings() {
        return uploadTimings;
    }

    /** Get download time of all regions. */
    public Map<String, List<Long>> getDownloadTimings() {
        return downloadTimings;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void init() {
        for (final String region : StorageManager.REGIONS) {
            final List<Long> list = new ArrayList<Long>();
            this.uploadTimings.put(region, list);
            this.downloadTimings.put(region, list);
        }

        StorageManager.initBuckets(false);
    }

    /** Main function. */
    public static void main(final String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Google Cloud Storage upload speed test - By Tango");
            System.out.println("Usage: client_secret_file project_id ROUNDS SMALL|MEDIUM|BIG|HUGE");
            return;
        }

        try {
            CredentialsManager.setup(args[0]);
        } catch (GeneralSecurityException e) {
            LOGGER.error("CredentialsManager.setup() failed");
            LOGGER.error(e.getMessage());
            return;
        }

        StorageManager.setup(args[1]);

        StorageManager.initBuckets(true);

        final int rounds = Integer.parseInt(args[2]);
        final byte[] data = DataBytes.getData(DataBytes.Size.valueOf(args[3]));

        LOGGER.debug("Starting test");

        final SpeedTest speedTest = new SpeedTest(rounds, data);
        speedTest.start();

        LOGGER.debug("Test finished");

        StorageManager.deleteBuckets();

        LOGGER.debug("Upload results");
        printResults(speedTest.getUploadTimings());
        LOGGER.debug("Download results");
        printResults(speedTest.getDownloadTimings());
    }

    private static void printResults(final Map<String, List<Long>> timings) {
        for (final String region : StorageManager.REGIONS) {
            LOGGER.debug("RegionName: '{}'", region);

            long sum = 0;

            final List<Long> regionTimings = timings.get(region);

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

            final int timingsCount = regionTimings.size();

            for (final Long time : timings.get(region)) {
                sum += time.longValue();
            }

            final double avg = sum / (double) timingsCount;
            final double median;
            {
                final int middle = timingsCount / 2;
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
