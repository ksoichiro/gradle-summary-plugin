package com.github.ksoichiro.summary

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SummaryPluginSpec extends Specification {
    static final String PLUGIN_ID = 'com.github.ksoichiro.summary'

    def apply() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: PLUGIN_ID

        then:
        notThrown(Exception)
        project.tasks."${GenerateSummaryTask.NAME}" instanceof GenerateSummaryTask
    }
}
