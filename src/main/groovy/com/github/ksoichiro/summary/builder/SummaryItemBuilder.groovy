package com.github.ksoichiro.summary.builder

import com.github.ksoichiro.summary.Summary
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
