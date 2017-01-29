package com.github.ksoichiro.summary.builder.jacoco

import com.github.ksoichiro.summary.CoverageReportClassConverter
import com.github.ksoichiro.summary.Summary
import com.github.ksoichiro.summary.builder.SummaryItemBuilder
import org.gradle.api.Project

class JacocoCoverageSummaryItemBuilder extends SummaryItemBuilder {
    JacocoCoverageSummaryItemBuilder(Project project) {
        super(project)
    }

    @Override
    Object[] dependsOn() {
        project.rootProject.allprojects.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }
    }

    @Override
    List<File> getInputFiles() {
        def reports = []
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('jacoco') }.each {
            it.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }.each {
                if (!it.reports.xml.enabled) {
                    it.reports.xml.enabled = true
                }
                reports += it.reports.xml.destination
            }
        }
        reports
    }

    @Override
    List<Summary> build() {
        def summaryContent = []
        def jacocoCoverageParser = new JacocoCoverageParser()
        def coverageReportClassConverter = new CoverageReportClassConverter()
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('jacoco') }.each { Project p ->
            p.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }.each {
                File xml = it.reports.xml.destination
                def cov = jacocoCoverageParser.parse(xml)
                summaryContent += new JacocoCoverageSummary(
                    name: p.name,
                    title: 'Coverage[%]',
                    cssClasses: coverageReportClassConverter.convert(cov),
                    coverage: cov,
                    htmlReportFile: p.file("${it.reports.html.destination}/index.html"),
                )
            }
        }
        summaryContent
    }
}
