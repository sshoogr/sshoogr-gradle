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

package com.aestasit.gradle.plugins.ssh.tasks

import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task type for remote command execution.
 *
 * @author Andrey Adamovich
 *
 */
class Exec extends DefaultTask {

  @Input
  String command

  @TaskAction
  void doExec() {
    SshDslEngine dslEngine = new SshDslEngine(project.sshOptions)
    dslEngine.remoteSession {
      exec(command)
    }
  }
}
