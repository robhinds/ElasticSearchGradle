package io.github.robhinds.elasticsearch.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test


class ElasticSearchTestPluginTest extends GroovyTestCase {

    def plugin = new ElasticSearchTestPlugin()

    @Test void testAddCodeGenConfig() {
        Project p = new ProjectBuilder().build()
    }


}
