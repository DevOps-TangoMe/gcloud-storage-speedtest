package me.tango.devops.task;

public abstract class UploadTask implements Runnable {
    protected final String region;
    protected final String bucket;
    protected final byte[] data;

    protected UploadTaskResult result;


    public UploadTask(String region, String bucket, byte[] data) {
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

        public UploadTaskResult(boolean success, long uploadTime, long downloadTime) {
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
