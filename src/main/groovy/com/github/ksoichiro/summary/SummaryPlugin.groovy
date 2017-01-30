package com.github.ksoichiro.summary

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin

class SummaryPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(ReportingBasePlugin)
        project.extensions.create(SummaryExtension.NAME, SummaryExtension)
        project.tasks.create(GenerateSummaryTask.NAME, GenerateSummaryTask)
    }
}
