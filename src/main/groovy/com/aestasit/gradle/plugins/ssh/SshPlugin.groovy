package com.aestasit.gradle.plugins.ssh

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.aestasit.ssh.dsl.SshDslEngine

/**
 * Gradle plug-in that injects SSH functionality into the build script.
 *
 * @author Andrey Adamovich
 *
 */
class SshPlugin implements Plugin<Project> {

  def void apply(Project project) {
    project.extensions.create("sshOptions", SshPluginSettings)
    project.sshOptions.logger = new GradleLogger(project)
    injectSshDslSupport(project)
  }

  def injectSshDslSupport(Project project) {
    SshDslEngine dslEngine = new SshDslEngine(project.sshOptions)
    project.metaClass.with {

      remoteSession << { Closure cl ->
        dslEngine.remoteSession(cl)
      }

      remoteSession << { String url, Closure cl ->
        dslEngine.remoteSession(url, cl)
      }
    }
  }
}
