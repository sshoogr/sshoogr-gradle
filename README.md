# gradle-ssh-plugin

## Overview

The `sshoogr-gradle` is a **Gradle** plugin for working with remote **SSH** servers. It allows connecting, executing 
remote commands, coping files and directories, creating tunnels in a simple and concise way.

The plugin was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) 
to support quickly growing operations and hosting department.

### Quick example

This is a simple example of some **SSH** features available in the plugin:

    task remoteTask << {
      remoteSession("user:password@localhost:22") {
        exec 'rm -rf /tmp/cache/'
        scp "$buildDir/cache.content", '/tmp/cache/cache.content'        
      }
    }

### Adding plugin to the build

The first thing you need to do in order to use the plugin is to define a build script dependency to a remote repository 
(https://oss.sonatype.org/content/repositories/snapshots), which contains plugin's library:

    buildscript {
      repositories { mavenCentral() }
      dependencies {
        classpath 'com.aestasit.infrastructure.sshoogr:sshoogr-gradle:0.9.15 '
      }
    }

And then apply the plugin:
    
    apply plugin: 'secureShell'

Plugin can be configured with the help of `sshOptions` structure:

    sshOptions {
      ...
    }

It also gives access to a set of methods (`remoteSession`, `exec`, `scp` etc.) defined by **Sshoogr** - **Groovy SSH DSL**. 

For documentation on **Sshoogr DSL**, please, refer to https://github.com/aestasit/sshoogr.
 

