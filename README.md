# Gradle ElasticSearch Test Container plugin

Gradle plugin that attempts to spin up the the official ES docker container for the duration of the test phase. 

Obviously, it doesnt do anything but spin it up, pass the port to the tests, then tear it down. No clean up etc between any kind of testing.

To apply the plugin:

```
buildscript {
    repositories {
        maven {
            url "https://dl.bintray.com/robhinds/snapshots"
        }
    }
    dependencies {
        classpath "io.github.robhinds.elasticsearch-gradle:ElasticSearchTestDockerPlugin:0.0.1"
    }
}

apply plugin: "com.github.robhinds.elasticsearch-gradle"
```

By default, the plugin will start up the `elasticsearch:5.6.12-alpine` docker image before the `test` phase and then tear it down again after the test phase. 

You can then access the ES port that it is running on from an env variable in the tests like so (scala example using the elastic4s library)

```
val clientUri = ElasticsearchClientUri("127.0.0.1", System.getProperty("es.port").toInt)
val mockHttpClient: HttpClient = HttpClient(clientUri)
 ```
