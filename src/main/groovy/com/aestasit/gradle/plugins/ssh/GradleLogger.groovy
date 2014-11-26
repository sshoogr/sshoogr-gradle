/*
 * Copyright (C) 2011-2014 Aestas/IT
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

import com.aestasit.infrastructure.ssh.log.Logger
import org.gradle.api.Project

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

  void debug(String message) {
    project.logger.debug(message)
  }

  void info(String message) {
    if (verbose) {
      project.logger.quiet(message)
    } else {
      project.logger.info(message)
    }
  }

  void warn(String message) {
    project.logger.warn(message)
  }
}
