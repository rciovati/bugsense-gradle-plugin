package io.github.rciovati.bugsense

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class UploadMappingTaskTest extends Specification {

  Project project
  MockWebServer server
  File target

  def setup() {
    project = ProjectBuilder.builder().build()
    target = new File("file.txt")
    PrintWriter writer = new PrintWriter("file.txt", "UTF-8");
    writer.write("Hello!")
  }

  def cleanup() {
    target.delete()
  }

  def "Invoke the task without parameters throws IllegalArgumentException"() {
    setup:
    Task task = project.tasks.create(name: 'test', type: UploadMappingTask) {}

    when:
    task.uploadMapping()

    then:
    thrown(IllegalArgumentException)
  }

  def "Invoke the task with a non-existing file throws IllegalArgumentException"() {
    setup:
    Task task = project.tasks.create(name: 'test', type: UploadMappingTask) {
      apiKey = 'aaa'
      authToken = 'aaa'
      appVersion = 'aaa'
      mappingFile = new File('aaaaa')
    }

    when:
    task.uploadMapping()

    then:
    thrown(IllegalArgumentException)
  }

  def "Upload file successful"() {
    server = new MockWebServer()

    setup:

    MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{}")

    server.enqueue(response)
    server.play()

    Task task = project.tasks.create(name: 'test', type: UploadMappingTask) {
      apiKey = 'api-key'
      authToken = 'auth-token'
      appVersion = '1.0.0'
      mappingFile = target
      endpoint = server.getUrl('/')
    }

    when:
    task.uploadMapping()

    then:
    def request = server.takeRequest()

    request.path == '/'
    request.getHeader('X-Bugsense-apikey') == 'api-key'
    request.getHeader('X-Bugsense-auth-token') == 'auth-token'
    request.getHeader('X-Bugsense-appver') == '1.0.0'
    request.getMethod().equalsIgnoreCase('post')

    notThrown(TaskExecutionException)

    cleanup:
    server.shutdown()
  }

  def "Upload file error throw TaskExecutionException"() {
    server = new MockWebServer()

    setup:

    MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(500)
            .setBody("{}")

    server.enqueue(response)
    server.play()

    Task task = project.tasks.create(name: 'test', type: UploadMappingTask) {
      apiKey = 'api-key'
      authToken = 'auth-token'
      appVersion = '1.0.0'
      mappingFile = target
      endpoint = server.getUrl('/')
    }

    when:
    task.uploadMapping()

    then:
    thrown(TaskExecutionException)

    cleanup:
    server.shutdown()
  }

  def "Upload file error doesn't throw TaskExecutionException when failOnUploadError is false"() {
    server = new MockWebServer()

    setup:

    MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(500)
            .setBody("{}")

    server.enqueue(response)
    server.play()

    Task task = project.tasks.create(name: 'test', type: UploadMappingTask) {
      apiKey = 'api-key'
      authToken = 'auth-token'
      appVersion = '1.0.0'
      mappingFile = target
      endpoint = server.getUrl('/')
      failOnUploadError = false
    }

    when:
    task.uploadMapping()

    then:
    notThrown(TaskExecutionException)

    cleanup:
    server.shutdown()
  }
}
