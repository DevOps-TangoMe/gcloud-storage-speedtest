package me.tango.devops.google;


import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static me.tango.devops.google.CredentialsManager.*;

public class StorageManager {
    // US-WEST2 is not valid
    public final static String[] REGIONS =
        new String[] {"ASIA-EAST1", "US-CENTRAL1", "US-CENTRAL2", "US-EAST1", "US-EAST2",
            "US-EAST3", "US-WEST1"};
    public static final Map<String, String> buckets = new HashMap<String, String>();
    private final static String BUCKET_PREFIX = "upload-speed-test-a34c4e0c-";
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManager.class);
    private static String PROJECT_ID;
    private static Storage CLIENT;

    public static void setup(String projectId) throws Exception {
        PROJECT_ID = projectId;
        CLIENT = new Storage.Builder(httpTransport, JSON_FACTORY, authorize())
            .setApplicationName(PROJECT_ID).build();
    }

    public static Map<String, String> getBuckets() {
        return buckets;
    }

    public static void initBuckets(boolean create) {
        for (final String region : REGIONS) {
            final String bucketName = BUCKET_PREFIX + region.toLowerCase();
            buckets.put(region, bucketName);

            if (create) {
                try {
                    LOGGER.debug("Creating bucket  '{}'", bucketName);
                    Bucket newBucket = CLIENT.buckets().insert(PROJECT_ID,
                        new Bucket().setName(bucketName).setLocation(region)
                            .setStorageClass("DURABLE_REDUCED_AVAILABILITY")).execute();
                } catch (IOException e) {
                    LOGGER.error("Create bucket exception", e);
                }
            }
        }
    }

    public static void deleteBuckets() {
        for (final String bucketName : buckets.values()) {
            try {
                LOGGER.debug("Deleting bucket " + bucketName);
                CLIENT.buckets().delete(bucketName).execute();
            } catch (IOException e) {
                LOGGER.error("Delete bucket exception", e);
            }
        }
    }

    public static boolean putBytes(String bucket, String key, byte[] bytes) {
        final InputStreamContent mediaContent =
            new InputStreamContent("application/octet-stream", new ByteArrayInputStream(bytes));
        mediaContent.setLength(bytes.length);

        try {
            final Storage.Objects.Insert insertObject =
                CLIENT.objects().insert(bucket, null, mediaContent);
            insertObject.setName(key);
            if (mediaContent.getLength() > 0
                && mediaContent.getLength() <= 2 * 1000 * 1000 /* 2MB */) {
                insertObject.getMediaHttpUploader().setDirectUploadEnabled(true);
            }
            insertObject.execute();
            return true;
        } catch (IOException e) {
            LOGGER.error("Error uploading data", e);
            return false;
        }
    }

    public static byte[] getBytes(String bucket, String key) {
        try {
            Storage.Objects.Get getRequest = CLIENT.objects().get(bucket, key);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getRequest.executeMediaAndDownloadTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Error downloading data", e);
            return null;
        }
    }

    public static void deleteBytes(String bucket, String key) {
        try {
            CLIENT.objects().delete(bucket, key).execute();
        } catch (IOException e) {
            LOGGER.error("Error deleting data", e);
        }
    }
}
