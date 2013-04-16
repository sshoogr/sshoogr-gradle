# gradle-ssh-plugin

## Overview

The `gradle-ssh-plugin` is a Gradle plugin for working with remote SSH servers. It allows connecting, executing remote commands, coping files and directories, 
creating tunnels in a simple and concise way.

The plugin was jointly developed by *Aestas/IT* (http://aestasit.com) and *NetCompany A/S* (http://www.netcompany.com/) 
to support quickly growing operations and hosting department.

### Quick example

    task remoteTask << {
      remoteSession("user:password@localhost:22") {
        exec 'rm -rf /tmp/cache/*'
        scp "$buildDir/cache.content", '/tmp/cache/cache.content'        
      }
    }

## Usage guide

### Adding plugin to the build

The first thing you need to do in order to use the plugin is to define a build script dependency to a remote repository 
(https://oss.sonatype.org/content/repositories/snapshots), which contains plugin's library:

    buildscript {
      repositories {
        maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
      }
      dependencies {
        classpath 'com.aestasit.gradle:gradle-ssh-plugin:0.8-SNAPSHOT'
      }
    }

And then apply the plugin:
    
    apply plugin: 'secureShell'

Plugin can be configured with the help of `sshOptions` structure:

    sshOptions {
      ...
    }

It also gives access to a set of methods (`remoteSession`, `exec`, `scp` etc.) defined by Groovy SSH DSL. 

For documentation on Groovy SSH DSL, please, refer to https://github.com/aestasit/groovy-ssh-dsl.






 