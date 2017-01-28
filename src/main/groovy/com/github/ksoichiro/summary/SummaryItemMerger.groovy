package com.github.ksoichiro.summary

import org.gradle.api.Project

class SummaryItemMerger {
    Project project

    SummaryItemMerger(Project project) {
        this.project = project
    }

    def merge(List<List<Summary>> items) {
        def merged = [:]
        project.rootProject.subprojects.each { Project p ->
            items.each { List<Summary> summaries ->
                summaries.findAll { it.name == p.name }.each { Summary summary ->
                    if (merged[p.name] == null) {
                        merged[p.name] = []
                    }
                    merged[p.name] += summary
                }
            }
        }
        merged.collect { k, v -> v }
    }
}
