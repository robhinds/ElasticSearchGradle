package io.github.robhinds.elasticsearch.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import de.gesellix.gradle.docker.tasks.*

class ElasticSearchTestPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('elasticsearchdocker', ElasticSearchTestPluginExtension)
        project.plugins.apply('de.gesellix.docker')
        stopElasticSearchTask(project)
        removeElasticSearchContainerTask(project)
        startElasticSearchTask(project)
        elasticSearchConnectionDetailsTask(project)
        waitForElasticSearchAvailableTask(project)

        project.afterEvaluate {
            String testPhase = project.extensions.elasticsearchdocker.testPhaseTask
            project.tasks[testPhase].dependsOn project.tasks.waitForElasticSearchAvailable
            project.tasks.removeElasticSearchContainer.mustRunAfter project.tasks[testPhase]
        }
    }

    protected DockerRunTask startElasticSearchTask(Project project) {
        project.tasks.create("startElasticSearch", DockerRunTask) {
            project.afterEvaluate {
                imageName = project.extensions.elasticsearchdocker.dockerImageName
                containerName = project.extensions.elasticsearchdocker.dockerContainerName
                containerConfiguration = [
                        "HostConfig": [
                                "PublishAllPorts": true
                        ]
                ]
            }
            finalizedBy project.tasks.removeElasticSearchContainer
        }
    }

    protected DockerStopTask stopElasticSearchTask(Project project) {
        project.tasks.create("stopElasticSearch", DockerStopTask) {
            project.afterEvaluate {
                containerId = project.extensions.elasticsearchdocker.dockerContainerName
            }
        }
    }

    protected DockerRmTask removeElasticSearchContainerTask(Project project) {
        project.tasks.create("removeElasticSearchContainer", DockerRmTask) {
            project.afterEvaluate {
                containerId = project.extensions.elasticsearchdocker.dockerContainerName
            }
            dependsOn project.tasks.stopElasticSearch
        }
    }

    protected DockerInspectContainerTask elasticSearchConnectionDetailsTask(Project p) {
        p.tasks.create("elasticSearchConnectionDetails", DockerInspectContainerTask) {
            p.afterEvaluate {
                containerId = p.extensions.elasticsearchdocker.dockerContainerName
            }
            dependsOn p.tasks.startElasticSearch

            doLast {
                def port = containerInfo.content.NetworkSettings.Ports["9200/tcp"][0].HostPort
                project.extensions.elasticsearchdocker.port = port
            }
        }
    }

    protected Exec waitForElasticSearchAvailableTask(Project project) {
        project.tasks.create("waitForElasticSearchAvailable", Exec) {
            project.afterEvaluate {
                doFirst {
                    String testPhase = project.extensions.elasticsearchdocker.testPhaseTask
                    project.tasks."$testPhase" {
                        systemProperties['es.port'] = project.extensions.elasticsearchdocker.port
                    }
                    commandLine 'bash', '-c', "while ! curl -s localhost:${project.extensions.elasticsearchdocker.port}; do sleep 1; done > /dev/null"
                }
            }
            dependsOn project.tasks.elasticSearchConnectionDetails
        }
    }
}