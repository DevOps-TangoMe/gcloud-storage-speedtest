# gcloud-storage-speedtest

A tool to test upload speeds to Google Cloud Storage regions.

## Compile

    mvn clean package

## Usage

    java -jar gcloud-storage-speedtest-0.7.jar client_secret_file project_id ROUNDS SIZE
    
You will need your Google client secret file to run this tool.

* ROUNDS Number of iterations to test, usually 12 (since we remove the best and worst scores before averaging)
* SIZE SMALL=1KB, MEDIUM=5MB, BIG=10MB, HUGE=100MB

This tool will automatically create a bucket in each region, upload a file to each bucket to test the upload speed. After that, it will automatically delete all files and all buckets.
