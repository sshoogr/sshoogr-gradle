package com.aestasit.gradle.plugins.ssh

import org.gradle.api.Project

import com.aestasit.ssh.log.Logger

/**
 * Gradle-based logger.
 *
 * @author Aestas/IT
 *
 */
class GradleLogger implements Logger {

  Project project
  boolean verbose

  GradleLogger(Project project, boolean verbose) {
    super()
    this.verbose = verbose
    this.project = project
  }

  def void debug(String message) {
    project.logger.debug(message)
  }

  def void info(String message) {
    if (verbose) {
      project.logger.quiet(message)
    } else {
      project.logger.info(message)
    }
  }

  def void warn(String message) {
    project.logger.warn(message)
  }
}
