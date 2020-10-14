/*
 * Copyright (C) 2011-2020 Aestas/IT
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

import com.aestasit.gradle.plugins.ssh.SshPluginSettings
import com.aestasit.infrastructure.ssh.dsl.SessionDelegate
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 */
@CompileStatic
class RemoteSession extends DefaultTask {
    @Optional
    @Input
    final Property<String> url = project.objects.property(String)
    @Optional
    @Input
    Action<? extends SessionDelegate> sessionAction

    void action(Action<? extends SessionDelegate> c) {
        setSessionAction(c)
    }

    void action(String url, Action<? extends SessionDelegate> c) {
        this.url.set(url)
        setSessionAction(c)
    }

    void action(Property<String> url, Action<? extends SessionDelegate> c) {
        this.url.set(url.map { u -> u })
        setSessionAction(c)
    }

    @TaskAction
    void executeRemoteSession() {
        if (sessionAction != null) {
            // wrap Action with a Closure as SsDslEngine expects a closure
            Closure<Void> invoker = {
                getSessionAction().execute((SessionDelegate) delegate)
            }
            if (!url.present) {
                new SshDslEngine(project.extensions.findByType(SshPluginSettings))
                    .remoteSession(invoker)
            } else {
                new SshDslEngine(project.extensions.findByType(SshPluginSettings))
                    .remoteSession(getUrl().get(), invoker)
            }
        } else {
            throw new IllegalArgumentException("You must invoke 'action(Action)' on :$path at least once")
        }
    }
}