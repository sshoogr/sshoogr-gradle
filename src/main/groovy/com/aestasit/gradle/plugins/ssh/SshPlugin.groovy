/*
 * Copyright (C) 2011-2019 Aestas/IT
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

import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plug-in that injects SSH functionality into the build script.
 *
 * @author Aestas/IT
 *
 */
class SshPlugin implements Plugin<Project> {

  void apply(Project project) {
    project.extensions.create("sshOptions", SshPluginSettings)
    project.sshOptions.logger = new GradleLogger(project, false)
    injectSshDslSupport(project)
  }

  void injectSshDslSupport(Project project) {
    project.metaClass.with {
      remoteSession << { Closure cl ->
        setLogLevel(project)
        new SshDslEngine(project.sshOptions).remoteSession(cl)
      }
      remoteSession << { String url, Closure cl ->
        setLogLevel(project)
        new SshDslEngine(project.sshOptions).remoteSession(url, cl)
      }
    }
  }

  static void setLogLevel(Project project) {
    if (project?.sshOptions?.logger instanceof GradleLogger) {
      ((GradleLogger) project.sshOptions.logger).verbose = project.sshOptions.verbose
    }
  }
}
