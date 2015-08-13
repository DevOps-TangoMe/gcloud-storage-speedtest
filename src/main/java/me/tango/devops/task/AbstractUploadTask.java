package me.tango.devops.task;

/** Base class of all upload tasks. */
@SuppressWarnings({"PMD.CommentRequired","PMD.BeanMembersShouldSerialize"})
public abstract class AbstractUploadTask implements Runnable {
    protected final String region;
    protected final String bucket;
    protected final byte[] data;

    protected UploadTaskResult result;


    /**
     * Constructor.
     *
     * @param region Region
     * @param bucket Bucket
     * @param data  Data
     */
    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    public AbstractUploadTask(final String region, final String bucket, final byte[] data) {
        this.region = region;
        this.bucket = bucket;
        this.data = data;
    }

    public UploadTaskResult getResult() {
        return result;
    }

    @Override
    public abstract void run();


    public static class UploadTaskResult {
        private final boolean success;
        private final long uploadTime;
        private final long downloadTime;

        /** Result of upload task. */
        public UploadTaskResult(final boolean success, final long uploadTime,
            final long downloadTime) {
            this.success = success;
            this.uploadTime = uploadTime;
            this.downloadTime = downloadTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public long getUploadTime() {
            return uploadTime;
        }

        public long getDownloadTime() {
            return downloadTime;
        }
    }
}
