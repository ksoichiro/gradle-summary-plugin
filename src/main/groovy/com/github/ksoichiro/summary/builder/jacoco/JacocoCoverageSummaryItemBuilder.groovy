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
        jacocoReportTasks(project.rootProject.allprojects)
    }

    @Override
    List<File> getInputFiles() {
        def reports = []
        jacocoReportTasks(jacocoProjects()).each {
            if (!it.reports.xml.enabled) {
                it.reports.xml.enabled = true
            }
            reports += it.reports.xml.destination
        }
        reports
    }

    @Override
    List<Summary> build() {
        def summaryContent = []
        def coverageReportClassConverter = new CoverageReportClassConverter()
        jacocoReportTasks(jacocoProjects()).each {
            File xml = it.reports.xml.destination
            def cov = JacocoCoverageParser.parse(xml)
            summaryContent += new JacocoCoverageSummary(
                name: it.project.name,
                title: 'Coverage[%]',
                cssClasses: coverageReportClassConverter.convert(cov),
                coverage: cov,
                htmlReportFile: it.project.file("${it.reports.html.destination}/index.html"),
            )
        }
        summaryContent
    }

    def jacocoProjects() {
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('jacoco') }
    }

    static jacocoReportTasks(p) {
        p.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }
    }
}
