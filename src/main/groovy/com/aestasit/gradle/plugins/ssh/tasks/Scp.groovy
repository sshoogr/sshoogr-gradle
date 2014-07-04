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

package com.aestasit.gradle.plugins.ssh.tasks

import com.aestasit.ssh.dsl.FileSetDelegate
import com.aestasit.ssh.dsl.ScpOptionsDelegate
import com.aestasit.ssh.dsl.SshDslEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Gradle task type for remote file uploading/downloading.
 *
 * @author Andrey Adamovich
 *
 */
class Scp extends DefaultTask {

  private final ScpOptionsDelegate copySpec = new ScpOptionsDelegate()

  public void from(@DelegatesTo(strategy = DELEGATE_FIRST, value = FileSetDelegate) Closure cl) {
    copySpec.from(cl)
  }

  public void into(@DelegatesTo(strategy = DELEGATE_FIRST, value = FileSetDelegate) Closure cl) {
    copySpec.into(cl)
  }

  @TaskAction
  void doCopy() {
    SshDslEngine dslEngine = new SshDslEngine(project.sshOptions)
    dslEngine.remoteSession {
      scp(copySpec)
    }
  }
}
