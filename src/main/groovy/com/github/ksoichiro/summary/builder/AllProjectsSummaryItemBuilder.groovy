package com.github.ksoichiro.summary.builder

import com.github.ksoichiro.summary.Summary
import org.gradle.api.Project

class AllProjectsSummaryItemBuilder extends SummaryItemBuilder {
    AllProjectsSummaryItemBuilder(Project project) {
        super(project)
    }

    @Override
    Object[] dependsOn() {
        return null
    }

    @Override
    List<File> getInputFiles() {
        return null
    }

    @Override
    List<Summary> build() {
        def summaryContent = []
        project.rootProject.subprojects.each {
            summaryContent += new Summary(name: it.name, title: 'Project', content: it.name)
        }
        summaryContent
    }
}
