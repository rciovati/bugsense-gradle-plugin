package io.github.rciovati.bugsense

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.InputStreamBody
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

public class UploadMappingTask extends DefaultTask {

  @Input
  File mappingFile

  @Input
  String apiKey

  @Input
  String authToken

  @Input
  String appVersion

  @Input
  @Optional
  boolean failOnUploadError = true

  @Input
  @Optional
  String endpoint = "https://symbolicator.splkmobile.com/upload/mapping"

  @TaskAction
  def uploadMapping() {
    validateTaskParameters()

    logger.info("Performing request to: ${endpoint}")
    logger.info("Mapping file: ${mappingFile.getAbsolutePath()}")

    HTTPBuilder http = new HTTPBuilder(endpoint)

    MultipartEntity content = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
    content.addPart("file", new InputStreamBody(new FileInputStream(mappingFile),
            "text/plain",
            "mapping.txt"))

    http.request(Method.POST) { request ->

      headers.'X-Bugsense-apikey' = apiKey
      headers.'X-Bugsense-auth-token' = authToken
      headers.'X-Bugsense-appver' = appVersion

      requestContentType = 'multipart/form-data'

      request.setEntity(content)

      response.success = { response, reader ->
        logger.info("Upload successful: $response.status")
        return true
      }

      response.failure = { response, reader ->
        logger.info("Response failure: ${response}")

        if (failOnUploadError) {
          def e = new Exception(
                  "Upload failed, response code: ${response.status}. Aborting the build")
          throw new TaskExecutionException(this, e)
        }

        return false
      }
    }
  }

  private void validateTaskParameters() {
    if (StringUtils.isBlank(apiKey)) {
      throw new IllegalArgumentException("apiKey cannot be null or empty")
    }

    if (StringUtils.isBlank(authToken)) {
      throw new IllegalArgumentException("authToken cannot be null or empty")
    }

    if (StringUtils.isBlank(appVersion)) {
      throw new IllegalArgumentException("appVersion cannot be null or empty")
    }

    if (mappingFile == null) {
      throw new IllegalArgumentException("mappingFile should be a valid file")
    }

    if (!mappingFile.exists() || !mappingFile.isFile()) {
      throw new IllegalArgumentException(
              "${mappingFile.getAbsolutePath()} doesn't exist or is not a file")
    }
  }
}
