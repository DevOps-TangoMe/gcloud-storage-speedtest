package me.tango.devops.google;


import static me.tango.devops.google.CredentialsManager.*;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/** Manage Google Cloud Storage client. */
public final class StorageManager {
    /** Available regions, US-WEST2 is not valid. */
    public static final String[] REGIONS =
        new String[] {"ASIA-EAST1", "US-CENTRAL1", "US-CENTRAL2", "US-EAST1", "US-EAST2",
            "US-EAST3", "US-WEST1"};
    /** Region->Bucket mappings. */
    public static final Map<String, String> BUCKETS = new HashMap<String, String>();
    /** Buckets' prefix. **/
    private static final String BUCKET_PREFIX = "upload-speed-test-a34c4e0c-";
    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManager.class);
    /** Project ID. */
    private static String projectId;
    /** Java client to communicate with Google cloud storage. */
    private static Storage client;

    // Make it a utility class
    private StorageManager() {}

    /** Configuration. */
    public static void setup(final String projectId) throws IOException {
        StorageManager.projectId = projectId;
        client = new Storage.Builder(httpTransport, JSON_FACTORY, authorize())
            .setApplicationName(StorageManager.projectId).build();
    }

    /** return region->bucket mappings. */
    public static Map<String, String> getBuckets() {
        return BUCKETS;
    }

    /** Get all bucket names and create them. */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static void initBuckets(final boolean create) {
        for (final String region : REGIONS) {
            final String bucketName = BUCKET_PREFIX + region.toLowerCase(Locale.ENGLISH);
            BUCKETS.put(region, bucketName);

            if (create) {
                try {
                    LOGGER.debug("Creating bucket  '{}'", bucketName);
                    client.buckets().insert(projectId,
                        new Bucket().setName(bucketName).setLocation(region)
                            .setStorageClass("DURABLE_REDUCED_AVAILABILITY")).execute();
                } catch (IOException e) {
                    LOGGER.error("Create bucket exception", e);
                }
            }
        }
    }

    /** Delete BUCKETS. */
    public static void deleteBuckets() {
        for (final String bucketName : BUCKETS.values()) {
            try {
                LOGGER.debug("Deleting bucket " + bucketName);
                client.buckets().delete(bucketName).execute();
            } catch (IOException e) {
                LOGGER.error("Delete bucket exception", e);
            }
        }
    }

    /** Upload data. */
    public static boolean putBytes(final String bucket, final String key, final byte[] bytes) {
        final InputStreamContent mediaContent =
            new InputStreamContent("application/octet-stream", new ByteArrayInputStream(bytes));
        mediaContent.setLength(bytes.length);

        try {
            final Storage.Objects.Insert insertObject =
                client.objects().insert(bucket, null, mediaContent);
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

    /** Download data. */
    public static byte[] getBytes(final String bucket, final String key) {
        try {
            final Storage.Objects.Get getRequest = client.objects().get(bucket, key);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getRequest.executeMediaAndDownloadTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Error downloading data", e);
            return null;
        }
    }

    /** Delete data. */
    public static void deleteBytes(final String bucket, final String key) {
        try {
            client.objects().delete(bucket, key).execute();
        } catch (IOException e) {
            LOGGER.error("Error deleting data", e);
        }
    }
}
