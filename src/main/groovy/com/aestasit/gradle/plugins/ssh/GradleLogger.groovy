package com.aestasit.gradle.plugins.ssh

import org.gradle.api.Project

import com.aestasit.ssh.log.Logger

class GradleLogger implements Logger {

  Project project

  GradleLogger(Project project) {
    super();
    this.project = project;
  }

  void debug(String message) {
    project.logger.debug(message)
  }

  void info(String message) {
    project.logger.info(message)
  }

  void warn(String message) {
    project.logger.warn(message)
  }
}
