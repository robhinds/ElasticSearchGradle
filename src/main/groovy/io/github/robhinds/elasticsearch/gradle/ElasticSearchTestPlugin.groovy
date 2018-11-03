package io.github.robhinds.elasticsearch.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import de.gesellix.gradle.docker.tasks.*

class ElasticSearchTestPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('generator', ElasticSearchTestPluginExtension)
        project.plugins.apply('de.gesellix.docker')
        stopElasticSearchTask(project)
        removeElasticSearchContainerTask(project)
        startElasticSearchTask(project)
        elasticSearchConnectionDetailsTask(project)
        waitForElasticSearchAvailableTask(project)

        String testPhase = project.extensions.generator.testPhaseTask
        project.tasks[testPhase].dependsOn project.tasks.waitForElasticSearchAvailable
        project.tasks.removeElasticSearchContainer.mustRunAfter project.tasks[testPhase]
    }

    protected DockerRunTask startElasticSearchTask(Project project) {
        project.tasks.create("startElasticSearch", DockerRunTask) {
            project.afterEvaluate {
                imageName = project.extensions.generator.dockerImageName
                containerName = project.extensions.generator.dockerContainerName
                dockerHost = project.extensions.generator.localDockerHost
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
                containerId = project.extensions.generator.dockerContainerName
                dockerHost = project.extensions.generator.localDockerHost
            }
        }
    }

    protected DockerRmTask removeElasticSearchContainerTask(Project project) {
        project.tasks.create("removeElasticSearchContainer", DockerRmTask) {
            project.afterEvaluate {
                containerId = project.extensions.generator.dockerContainerName
                dockerHost = project.extensions.generator.localDockerHost
            }
            dependsOn project.tasks.stopElasticSearch
        }
    }

    protected DockerInspectContainerTask elasticSearchConnectionDetailsTask(Project p) {
        p.tasks.create("elasticSearchConnectionDetails", DockerInspectContainerTask) {
            p.afterEvaluate {
                containerId = p.extensions.generator.dockerContainerName
                dockerHost = project.extensions.generator.localDockerHost
            }
            dependsOn p.tasks.startElasticSearch

            doLast {
                def port = containerInfo.content.NetworkSettings.Ports["9200/tcp"][0].HostPort
                project.extensions.generator.port = port
                test {
                    systemProperties['es.port'] = port
                }
            }
        }
    }

    protected Exec waitForElasticSearchAvailableTask(Project project) {
        project.tasks.create("waitForElasticSearchAvailable", Exec) {
            project.afterEvaluate {
                doFirst {
                    commandLine 'bash', '-c', "while ! curl -s localhost:${project.extensions.generator.port}; do sleep 1; done > /dev/null"
                }
            }
            dependsOn project.tasks.elasticSearchConnectionDetails
        }
    }
}