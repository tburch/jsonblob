JSON Blob
========

JSON Blob was created to help parallelize client/server development. Mock JSON responses can be defined and stored using the online editor and then clients can use the JSON Blob API to retrieve and update the mock responses.

[![Build Status](https://travis-ci.org/tburch/jsonblob.svg?branch=master)](https://travis-ci.org/tburch/jsonblob)

##Building & Running JSON Blob
To run JSON Blob, you'll need the following things installed:

- Java (version 7+)
- Maven 
- MongoDB

###Running JSON Blob
1. From the command line run `mvn clean package`.
1. From the command run `java -Ddw.mongo.type=uri -Ddw.mongo.uri=<MONGODB_INSTANCE_URL> -jar target/jsonblob.jar server target/config/jsonblob.yml`. You'll need to replace `<MONGODB_INSTANCE_URL>` with the actual URL (something like `mongodb://username:password@localhost:27017/jsonblob`)
