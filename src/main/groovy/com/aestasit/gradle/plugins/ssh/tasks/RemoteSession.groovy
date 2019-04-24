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
package com.aestasit.gradle.plugins.ssh.tasks

import com.aestasit.gradle.plugins.ssh.GradleLogger
import com.aestasit.infrastructure.ssh.dsl.SessionDelegate
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @author Andres Almiray
 */
class RemoteSession extends DefaultTask {
    private Property<String> url = project.objects.property(String)
    private Closure<Void> sessionClosure
    private Action<? extends SessionDelegate> sessionAction

    @Optional
    @Input
    void setUrl(String url) {
        this.url.set(url)
    }

    String getUrl() {
        url.orNull
    }

    @Optional
    @Input
    void setAction(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure<Void> c) {
        sessionClosure = c
    }

    @Optional
    @Input
    void setAction(Action<? extends SessionDelegate> c) {
        sessionAction = c
    }

    void action(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure<Void> c) {
        setAction(c)
    }

    void action(Action<? extends SessionDelegate> c) {
        setAction(c)
    }

    void action(String url, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure<Void> c) {
        setUrl(url)
        setAction(c)
    }

    void action(String url, Action<? extends SessionDelegate> c) {
        setUrl(url)
        setAction(c)
    }

    @TaskAction
    void executeRemoteSession() {
        if (sessionClosure != null) {
            setLogLevel(project)
            if (url.present) {
                new SshDslEngine(project.sshOptions).remoteSession(getUrl(), sessionClosure)
            } else {
                new SshDslEngine(project.sshOptions).remoteSession(sessionClosure)
            }
        } else if (sessionAction != null) {
            setLogLevel(project)
            // wrap Action with a Closure as SsDslEngine expects a closure
            Closure<Void> invoker = {
                sessionAction.execute((SessionDelegate) this.delegate)
            }
            if (url.present) {
                new SshDslEngine(project.sshOptions).remoteSession(invoker)
            } else {
                new SshDslEngine(project.sshOptions).remoteSession(getUrl(), invoker)
            }
        } else {
            throw IllegalStateException("Either 'action(Closure)' or 'action(Action)' must be invoked on :$path")
        }
    }

    private static void setLogLevel(Project project) {
        if (project.extensions.sshOptions.logger instanceof GradleLogger) {
            ((GradleLogger) project.sshOptions.logger).verbose = project.sshOptions.verbose
        }
    }
}
