package com.aestasit.gradle.plugins.ssh

import org.apache.sshd.SshServer
import org.apache.sshd.server.command.ScpCommandFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.sftp.SftpSubsystem
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.aestasit.gradle.plugins.ssh.mocks.MockCommandFactory
import com.aestasit.gradle.plugins.ssh.mocks.MockFileSystemFactory
import com.aestasit.gradle.plugins.ssh.mocks.MockShellFactory
import com.aestasit.gradle.plugins.ssh.mocks.MockUserAuthFactory

/**
 * SSH DSL test case that implements internal Gradle project to test different DSL syntax in different tasks.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslTest {

  static SshServer sshd
  static Project project

  @BeforeClass
  def static void startSshd() {
    sshd = SshServer.setUpDefaultServer()
    sshd.with {
      port = 2222
      keyPairProvider = new SimpleGeneratorHostKeyProvider()
      commandFactory = new ScpCommandFactory( new MockCommandFactory() )
      shellFactory = new MockShellFactory()
      userAuthFactories = [new MockUserAuthFactory()]
      fileSystemFactory = new MockFileSystemFactory()
      subsystemFactories = [
        new SftpSubsystem.Factory()
      ]
    }
    sshd.start()
  }

  @BeforeClass
  def static void buildProject() {
    project = ProjectBuilder.builder().build()
    project.logging.level = LogLevel.INFO
    project.with {

      apply plugin: 'secureShell'

      sshOptions {

        defaultHost = '127.0.0.1'
        defaultUser = 'user1'
        defaultPassword = '123456'
        defaultPort = 2222

        hostProperty = 'host'
        userProperty = 'user'
        passwordProperty = 'password'

        trustUnknownHosts = true

        execOptions {
          showOutput = true
          failOnError = false
          succeedOnExitStatus = 0
          maxWait = 30000
          outputFile = file("output.file")
          appendFile = true
        }

        scpOptions { verbose = true }
      }

      task('testDefaultSettings') << {
        // Test with default session settings.
        remoteSession {

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testUrlAndOverriding') << {
        // Test overriding default connection settings through URL.
        remoteSession {

          url = 'user2:654321@localhost:2222'

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testMethodOverriding') << {
        // Test overriding default connection settings through method parameter.
        remoteSession('user2:654321@localhost:2222') {

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testPropertyOverriding') << {
        // Test overriding default connection settings through delegate parameters.
        remoteSession {

          host = 'localhost'
          username = 'user2'
          password = '654321'
          port = 2222

          exec 'whoami'
          exec 'du -s'
          exec 'rm -rf /tmp/test.file'
          scp testFile, '/tmp/test.file'

        }
      }

      task('testOutputScripting') << {
        // Test saving the output and setting exec parameters through a builder.
        remoteSession {
          println ">>>>> COMMAND: whoami"
          def output = exec(command: 'whoami', showOutput: false)
          output.output.eachLine { line -> println ">>>>> OUTPUT: ${line.reverse()}" }
          println ">>>>> EXIT: ${output.exitStatus}"
        }
      }

      task('testExecClosure') << {
        // Test closure based builder for exec.
        remoteSession { exec { command = 'whoami' } }
      }

      task('testFailOnError') << {
        remoteSession {
          exec(command: 'abcd', failOnError: false)
        }
      }

      task('testTimeout') << {
        remoteSession {
          exec(command: 'timeout', maxWait: 1000)
        }
      }

      task('testCopy') << {
        remoteSession {
          scp {
            from {
              localDir new File(getCurrentDir(), 'test-settings')
            }
            into { remoteDir '/tmp/puppet' }
          }
        }
      }

      task('testMultiExec') << {
        remoteSession {
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

      task('testPrefix') << {
        remoteSession {
          prefix('sudo') {
            exec([
              'ls -la',
              'whoami'
            ])
          }
        }
      }

      task('testRemoteFile') << {
        remoteSession {
          remoteFile('/etc/init.conf').text = 'content'
        }
      }

    }

  }

  def static File getCurrentDir() {
    return new File(".").getAbsoluteFile()
  }

  def static File getTestFile() {
    return new File("input.file").getAbsoluteFile()
  }

  @AfterClass
  def static void stopSshd() {
    sshd?.stop(true)
  }

  @Test
  def void testDefaultSettings() throws Exception {
    project.tasks.'testDefaultSettings'.execute()
  }

  @Test
  def void testUrlAndOverriding() throws Exception {
    project.tasks.'testUrlAndOverriding'.execute()
  }

  @Test
  def void testMethodOverriding() throws Exception {
    project.tasks.'testMethodOverriding'.execute()
  }

  @Test
  def void testPropertyOverriding() throws Exception {
    project.tasks.'testPropertyOverriding'.execute()
  }

  @Test
  def void testOutputScripting() throws Exception {
    project.tasks.'testOutputScripting'.execute()
  }

  @Test
  def void testFailOnError() throws Exception {
    project.tasks.'testFailOnError'.execute()
  }

  @Test
  def void testTimeout() throws Exception {
    try {
      project.tasks.'testTimeout'.execute()
    } catch (TaskExecutionException e) {
      assert e.cause.message.contains('timeout')
    }
  }

  @Test
  def void testExecClosure() throws Exception {
    project.tasks.'testExecClosure'.execute()
  }

  @Test
  def void testCopy() throws Exception {
    project.tasks.'testCopy'.execute()
  }

  @Test
  def void testMultiExec() throws Exception {
    project.tasks.'testMultiExec'.execute()
  }

  @Test
  def void testPrefix() throws Exception {
    project.tasks.'testPrefix'.execute()
  }

  @Test
  def void testRemoteFile() throws Exception {
    project.tasks.'testRemoteFile'.execute()
  }

}
