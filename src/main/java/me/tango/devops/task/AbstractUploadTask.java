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
