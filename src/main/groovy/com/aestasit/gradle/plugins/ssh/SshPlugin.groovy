/*
 * Copyright (C) 2011-2013 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.gradle.plugins.ssh

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.aestasit.ssh.dsl.SshDslEngine

/**
 * Gradle plug-in that injects SSH functionality into the build script.
 *
 * @author Aestas/IT
 *
 */
class SshPlugin implements Plugin<Project> {

  def void apply(Project project) {
    project.extensions.create("sshOptions", SshPluginSettings)
    project.sshOptions.logger = new GradleLogger(project, false)
    injectSshDslSupport(project)
  }

  def injectSshDslSupport(Project project) {
    SshDslEngine dslEngine = new SshDslEngine(project.sshOptions)
    project.metaClass.with {

      remoteSession << { Closure cl ->
        setLogLevel(project)
        dslEngine.remoteSession(cl)
      }

      remoteSession << { String url, Closure cl ->
        setLogLevel(project)
        dslEngine.remoteSession(url, cl)
      }
    }
  }
  def void setLogLevel(Project project) {
    if (project?.sshOptions?.logger instanceof GradleLogger) {
      project.sshOptions.logger.verbose = project.sshOptions.verbose
    }
  }
}
