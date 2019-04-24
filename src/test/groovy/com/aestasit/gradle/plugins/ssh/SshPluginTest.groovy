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

import com.aestasit.gradle.plugins.ssh.tasks.RemoteSession
import com.aestasit.ssh.mocks.MockSshServer
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * SSH DSL test case that implements internal Gradle project to test different DSL syntax in different tasks.
 *
 * @author Andrey Adamovich
 *
 */
class SshPluginTest {

  static Project project

  @BeforeClass
  static void createServer() {
    MockSshServer.with {
    
      // Create command expectations.
      command('^ls.*$') { inp, out, err, callback, env ->
        out << '''total 20
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:52 .
drwxr-xr-x 8 1100 1100 4096 Aug  1 17:53 ..
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:49 examples
'''
        callback.onExit(0)
      }

      command('^whoami.*$') { inp, out, err, callback, env ->
        out << "root\n"
        callback.onExit(0)
      }

      command('^du.*$') { inp, out, err, callback, env ->
        out << "100\n"
        callback.onExit(0)
      }

      command('^rm.*$') { inp, out, err, callback, env ->
        out << "/tmp/test.file\n"
        callback.onExit(0)
      }

      command('timeout') { inp, out, err, callback, env ->
        sleep(2000)
        callback.onExit(0)
      }

      command('^sudo.*$') { inp, out, err, callback, env ->
        callback.onExit(0)
      }

      // Create file expectations.
      dir('.')
      dir('/tmp')

      // Start server
      startSshd(27921)

    }
  }

  @BeforeClass
  static void buildProject() {
    project = ProjectBuilder.builder().build()
    project.logging.captureStandardOutput LogLevel.INFO
    project.with {

      apply plugin: 'com.aestasit.sshoogr'

      sshOptions {

        defaultHost = '127.0.0.1'
        defaultUser = 'user1'
        defaultPassword = '123456'
        defaultPort = 27921

        trustUnknownHosts = true

        verbose = true

        execOptions {
          showOutput = true
          failOnError = true
          succeedOnExitStatus = 0
          maxWait = 30000
        }

      }

      task('testDefaultSettings', type: RemoteSession) {
        // Test with default session settings.
        action {

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testUrlAndOverriding', type: RemoteSession) {
        // Test overriding default connection settings through URL.
        action {

          url = 'user2:654321@localhost:27921'

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testMethodOverriding', type: RemoteSession) {
        // Test overriding default connection settings through method parameter.
        action('user2:654321@localhost:27921') {

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testPropertyOverriding', type: RemoteSession) {
        // Test overriding default connection settings through delegate parameters.
        action {

          host = 'localhost'
          user = 'user2'
          password = '654321'
          port = 27921

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testOutputScripting', type: RemoteSession) {
        // Test saving the output and setting exec parameters through a builder.
        action {
          println ">>>>> COMMAND: whoami"
          def output = exec(command: 'whoami', showOutput: false)
          output.output.eachLine { line -> println ">>>>> OUTPUT: ${line.reverse()}" }
          println ">>>>> EXIT: ${output.exitStatus}"
        }
      }

      task('testExecClosure', type: RemoteSession) {
        // Test closure based builder for exec.
        action { exec { command = 'whoami' } }
      }

      task('testFailOnError', type: RemoteSession) {
        action {
          exec(command: 'abcd', failOnError: false)
        }
      }

      task('testTimeout', type: RemoteSession) {
        action {
          exec(command: 'timeout', maxWait: 1000)
        }
      }

      task('testCopy', type: RemoteSession) {
        action {
          scp {
            from {
              localDir new File(getCurrentDir(), 'test-settings')
            }
            into { remoteDir '/tmp/puppet' }
          }
        }
      }

      task('testMultiExec', type: RemoteSession) {
        action {
          exec([
            'ls -la',
            'whoami'
          ])
          exec(failOnError: false, showOutput: true, command: [
            'ls -la',
            'whoami'
          ])
        }
      }

      task('testPrefix', type: RemoteSession) {
        action {
          prefix('sudo') {
            exec([
              'ls -la',
              'whoami'
            ])
          }
        }
      }

      task('testRemoteFile', type: RemoteSession) {
        action {
          remoteFile('/etc/init.conf').text = 'content'
        }
      }

    }
  }

  static File getCurrentDir() {
    return new File(".").getAbsoluteFile()
  }

  static File getTestFile() {
    return new File("input.file").getAbsoluteFile()
  }

  @AfterClass
  static void destroyServer() {
    MockSshServer.stopSshd()
  }

  @Test
  void testDefaultSettings() throws Exception {
    project.tasks.'testDefaultSettings'.executeRemoteSession()
  }

  @Test
  void testUrlAndOverriding() throws Exception {
    project.tasks.'testUrlAndOverriding'.executeRemoteSession()
  }

  @Test
  void testMethodOverriding() throws Exception {
    project.tasks.'testMethodOverriding'.executeRemoteSession()
  }

  @Test
  void testPropertyOverriding() throws Exception {
    project.tasks.'testPropertyOverriding'.executeRemoteSession()
  }

  @Test
  void testOutputScripting() throws Exception {
    project.tasks.'testOutputScripting'.executeRemoteSession()
  }

  @Test
  void testFailOnError() throws Exception {
    project.tasks.'testFailOnError'.executeRemoteSession()
  }

  @Test
  void testTimeout() throws Exception {
    try {
      project.tasks.'testTimeout'.executeRemoteSession()
    } catch (Exception e) {
      assert e.message.contains('timeout')
    }
  }

  @Test
  void testExecClosure() throws Exception {
    project.tasks.'testExecClosure'.executeRemoteSession()
  }

  @Test
  void testCopy() throws Exception {
    project.tasks.'testCopy'.executeRemoteSession()
  }

  @Test
  void testMultiExec() throws Exception {
    project.tasks.'testMultiExec'.executeRemoteSession()
  }

  @Test
  void testPrefix() throws Exception {
    project.tasks.'testPrefix'.executeRemoteSession()
  }

  @Test
  void testRemoteFile() throws Exception {
    project.tasks.'testRemoteFile'.executeRemoteSession()
  }
}
