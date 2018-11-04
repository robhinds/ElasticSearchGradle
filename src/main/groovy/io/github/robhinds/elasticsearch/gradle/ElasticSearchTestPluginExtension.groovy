package io.github.robhinds.elasticsearch.gradle

class ElasticSearchTestPluginExtension {
    String dockerImageName = "elasticsearch:5.6.12-alpine"
    String dockerContainerName = "gradle-elasticsearch-test"
    String testPhaseTask = "test"
    String port
}
