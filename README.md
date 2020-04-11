# sshoogr-gradle

![Build Status](https://github.com/sshoogr/sshoogr-gradle/workflows/Build/badge.svg)
![ASL2 Licensed](http://img.shields.io/badge/license-ASL2-blue.svg)
![Latest Version](https://api.bintray.com/packages/sshoogr/sshoogr/sshoogr-gradle/images/download.svg)

## Overview

The `sshoogr-gradle` is a **Gradle** plugin for working with remote **SSH** servers. It allows connecting, executing 
remote commands, coping files and directories, creating tunnels in a simple and concise way.

The plugin was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) 
to support quickly growing operations and hosting department.

### Quick example

This is a simple example of some **SSH** features available in the plugin:

**Using an Action**

    task mySshTask(type: RemoteSession) {
      action("user:password@localhost:22", new Action<SessionDelegate>() {
          @Override
          void execute(SessionDelegate sd) {
            sd.exec 'rm -rf /tmp/cache/'
            sd.scp "$buildDir/cache.content", '/tmp/cache/cache.content'
          }
      })
    }

### Adding plugin to the build

Applying the plugin can be done in 2 ways:

**Option #1**

    buildscript {
        repositories {
            jcenter()
            gradlePluginPortal()
        }
        dependencies {
            classpath 'com.aestasit.infrastructure.sshoogr:sshoogr-gradle:0.9.20'
        }
    }
    apply plugin: 'com.aestasit.sshoogr'

**Option #2**

    plugins {
        id 'com.aestasit.sshoogr' version '0.9.20'
    }


Plugin can be configured with the help of `sshOptions` structure:

    sshOptions {
      ...
    }

It also gives access to a set of task types (`RemoteSession`, `Exec`, `Scp` etc.) defined by **Sshoogr** - **Groovy SSH DSL**.

For documentation on **Sshoogr DSL**, please, refer to https://github.com/sshoogr/sshoogr.
 

