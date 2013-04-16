# A Gradle plugin for working with remote SSH servers using Groovy-based DSL

## Overview

The `gradle-ssh-plugin` was jointly developed by *Aestas/IT* (http://aestasit.com) and *NetCompany A/S* (http://www.netcompany.com/) 
to support quickly growing operations and hosting department.

The plugin allows connecting, executing remote commands, coping files and directories, 
creating tunnels in a simple and concise way.

## Usage

### Adding plugin to the build

The first thing you need to do in order to use the plugin is to define a build script dependency to a remote repository 
(https://oss.sonatype.org/content/repositories/snapshots), which contains plugin's library and then apply the plugin:

    buildscript {
      repositories {
        maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
      }
      dependencies {
        classpath 'com.aestasit.gradle:gradle-ssh-plugin:0.8-SNAPSHOT'
      }
    }
    
    apply plugin: 'secureShell'

Plugin can be configured with the help of `sshOptions` structure (see "Configuration options" section):

    sshOptions {
      ...
    }

It also gives access to a set of methods (`remoteSession`, `exec`, `scp` etc.) that are described in the following sections.

### Remote connections

After plugin has been applied you can create remote connection with the help of `remoteSession` method, which accepts an 
SSH URL and a closure, for example: 

    task remoteTask << {
      remoteSession("user:password@localhost:22") {
        ...
      }
    }

Inside the closure you can execute remote commands, access remote file content, upload and download files, create tunnels. 
All these capabilities are described in the following sections. 

If your connection settings were set with the help of default configuration (see "Configuration options" section), then you can omit URL parameter: 

    remoteSession {
      ...
    }

You can also override the defaults in each session by directly assigning `host`, `username`, `password` and `port` properties:

    remoteSession {

      host = 'localhost'
      username = 'user2'
      password = '654321'
      port = 2222
    
      ...
    
    }

Also you can assign SSH URL to the `url` property instead:

    remoteSession {
    
      url = 'user2:654321@localhost:2222'
    
      ...
    
    }

Actual connection to the remote host will be made upon first command or file access, and, naturally, connection will be 
automatically closed after code block finishes. But you can explicitly call `connect` or `disconnect` methods to control this:

    remoteSession {
    
      // explicitly call connect 
      connect()     
      
      // do some stuff
      ...
    
      // explicitly disconnect
      disconnect()
    
      // explicitly connect again
      connect()     
    
      ...
     
    }

### Configuration options

All the global plugin settings should be located in a top-level `sshOptions` structure inside your build script:

    sshOptions {
      failOnError = false
      defaultHost = 'localhost'
      ...
    }

The following list gives an overview of the available configuration options:

 - `defaultHost`, `defaultUser`, `defaultPassword`, `defaultPort` (22) - Default host, user name, password or port to use in remote connection in case they are not specified in some other way (through `url`, `host`, `port`, `user` or `password` properties inside `remoteSession` method).
 - `defaultKeyFile` - Default key file to use in remote connection in case it is not specified through keyFile property inside remoteSession method. Key file is an alternative mechanism to using passwords.
 - `failOnError` (true) - If set to true, failed remote commands and file operations will fail the build.
 - `verbose` (false) - If set to true, plugin produces more debug output.
 
The `sshOptions` may also contain a nested `execOptions` structure, which defines remote command execution (see "Executing commands" section) options. It has the following properties:

 - `showOutput` (true) - If set to true, remote command output is printed.
 - `showCommand` (true) - If set to true, remote command is printed.
 - `maxWait` (0) - Number of milliseconds to wait for command to finish. If it is set to 0, then plugin will wait forever.
 - `succeedOnExitStatus` (0) - Exit code that indicates commands success. If command returns different exit code, then build will fail.
 - `outputFile` - File, to which to send command's output. 
 - `appendFile` (false) - If outputFile is specified, then this option indicates if data should be appended or file should be created from scratch.
 - `failOnError` (true) - If set to true, failed remote commands will fail the build.
 - `verbose` (false) - If set to true, plugin produces more debug output.
 - `prefix` - String to prepend to each executed command, for example, "`sudo`".
 - `suffix` - String to append to each executed command, for example, "`>> output.log`".

There is also a nested `scpOptions` structure, which defines remote file copying (see "File uploading" section) options. It has the following properties:

 - `failOnError` (true) - If set to true, failed file operations will fail the build.
 - `showProgress` (false) - If set to true, plugin shows additional information regarding file upload/download progress.
 - `verbose` (false) - If set to true, plugin produces more debug output.

Example configuration:

    sshOptions {
      verbose = true
      execOptions {
        verbose = true
      }
      scpOptions {
        verbose = true
      }
    }

### Executing commands

The simplest way to execute a command within a remote session is by using exec method that just takes a command string:

    remoteSession {
      exec 'ls -la'
    }

You can also pass a list of commands in an array:

    exec([
     'ls -la', 
     'date'
    ])

The `exec` behavior can also be controlled with additional named parameters given to the method. For example, in order to hide commands output you can use the following syntax:

    exec(command: 'ls –la', showOutput: false)

Parameter names match the ones specified in "2.2 Configuration options" for the global execOptions, and all can be used to override default settings for specific commands.
In the same way you can also define common parameters for a block of commands passed as an array:

    exec(showOutput: false, command: [
     'ls -la', 
     'date'
    ])

Also you can get access to command output, exit code and exception thrown during command execution. This can be useful for implementing logic based on result returned by remote command and/or parsing the output. For example,

    def result = exec(command: '/usr/bin/mycmd', faileOnError: false, showOutput: false)
    if (result.exitStatus == 1) {
      result.output.eachLine { line ->
        if (line.contains('WARNING')) {
          throw new GradleException("Warning!!!")
        }
      }
    }
 
Another 2 methods that you can use around your commands are prefix and suffix. They are similar to using prefix and suffix options in execOptions or named parameters to exec method.

    prefix("sudo") {
      exec 'ls -la'
      exec 'df -h'
    }

And with suffix:

    suffix(">> output.log") {
      exec 'ls -la'
      exec 'df -h'
      exec 'date'
      exec 'facter'
    }

 
### File uploading/downloading

The simplest way to modify remote text file content is by using remoteFile method, which returns remote file object instance, and assign some string to the text property:

    remoteFile('/etc/yum.repos.d/puppet.repo').text = '''
      [puppet]
      name=Puppet Labs Packages
      baseurl=http://yum.puppetlabs.com/el/$releasever/products/$basearch/
      enabled=0
      gpgcheck=0
    '''

Each line of the input string will be trimmed before it's copied to the remote file.
For text file downloading you can just read the text property:

    println remoteFile('/etc/yum.repos.d/puppet.repo').text

Single file uploading can be done in the following way:

    scp "$buildDir/test.file", '/tmp/test.file'

This method only works for file uploading (from local environment to remote). You can also write the example above in more verbose form with the help of closures: 

    scp {
      from { localFile "$buildDir/test.file" }
      into { remoteFile '/tmp/test.file' }
    }

If you need to upload a directory or a set of several files that you need to use the same closure-based structure, but with the help of remoteDir and localDir methods:

    scp {
      from { localDir "$buildDir/application" }
      into { remoteDir '/var/bea/domain/application' }
    }

In similar way you can download directories and files:

    scp {
      from { remoteDir '/etc/nginx' }
      into { localDir "$buildDir/nginx" }
    }

You can also copy multiple sources into multiple targets:

    scp {
      from { 
        localDir "$buildDir/doc" 
        localFile "$buildDir/readme.txt" 
        localFile "$buildDir/license/license.txt" 
      }
      into { 
        remoteDir '/var/server/application' 
        remoteDir '/repo/company/application'
      }
    }

During upload/download operation target local and remote directories will be created automatically.

### Tunneling

If inside your build script you need to get access to a remote server that is not visible directly from the local machine, then you can create a tunnel to that server by using tunnel method:

    tunnel('1.2.3.4', 8080) { int localPort ->
      ...
    }

All code executed within the closure passed to the tunnel method will have access to server tunnel running on localhost and randomly selected localPort, which is passed as a parameter to the closure. Inside that tunnel code you can, for example, deploy a web application or send some HTTP command to remote server:

    tunnel('1.2.3.4', 8080) { int localPort ->
      def result = new URL("http://localhost:${localPort}/flushCache").text
      if (result == 'OK') {
        println "Cache is flushed!"
      } else {
        throw new GradleException(result)
      }
    }

Tunnel will be closed upon closure completion.
Also you can define local port yourself in the following way:

    tunnel(7070, '1.2.3.4', 8080) { 
      def result = new URL("http://localhost:7070/flushCache").text
      ...
    }

 