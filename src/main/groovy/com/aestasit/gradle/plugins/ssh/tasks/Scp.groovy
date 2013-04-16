package com.aestasit.gradle.plugins.ssh.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task type for remote file uploading/downloading.
 *
 * @author Andrey Adamovich
 *
 */
class Scp extends DefaultTask {

  @TaskAction
  def void doCopy() {
    // TODO:
  }
}
