JSON Blob
========

JSON Blob was created to help parallelize client/server development. Mock JSON responses can be defined and stored using the online editor and then clients can use the JSON Blob API to retrieve and update the mock responses.

[![Build Status](https://travis-ci.org/tburch/jsonblob.svg?branch=master)](https://travis-ci.org/tburch/jsonblob)

##Building & Running JSON Blob
1. To run JSON Blob, you'll need the following things installed:
   - Java (version 1.8+)
   - Maven
1. Build the JSON Blob jar - from the command line run `mvn clean package`.
1. Start JSON Blob - from the command line run `java -Ddw.blobManager.fileSystemBlogDataDirectory=<PATH TO STORE BLOBS ON THE FILESYSTEM> -jar target/jsonblob.jar server target/config/jsonblob.yml`. You'll need to replace `<PATH TO STORE BLOBS ON THE FILESYSTEM>` with the path where you want to store blobs on the file system.
