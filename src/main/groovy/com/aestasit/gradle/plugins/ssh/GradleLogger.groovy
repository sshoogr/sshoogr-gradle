package com.aestasit.gradle.plugins.ssh

import org.gradle.api.Project

import com.aestasit.ssh.log.Logger

class GradleLogger implements Logger {

  Project project

  public GradleLogger(Project project) {
    super();
    this.project = project;
  }

  public void debug(String message) {
    project.logger.debug(message)
  }

  public void info(String message) {
    project.logger.info(message)
  }

  public void warn(String message) {
    project.logger.warn(message)
  }
}
