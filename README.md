# Gradle ElasticSearch Test Container plugin

Gradle plugin that attempts to spin up the the official ES docker container for the duration of the test phase. 

Obviously, it doesnt do anything but spin it up, pass the port to the tests, then tear it down. No clean up etc between any kind of testing.
