package com.github.ksoichiro.summary

import org.gradle.api.Project

abstract class SummaryItemBuilder {
    Project project

    SummaryItemBuilder(Project project) {
        this.project = project
    }

    abstract Object[] dependsOn()
    abstract List<File> getInputFiles()
    abstract List<Summary> build()
}
