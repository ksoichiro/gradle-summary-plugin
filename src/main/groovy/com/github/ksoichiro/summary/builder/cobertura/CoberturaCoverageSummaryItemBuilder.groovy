package com.github.ksoichiro.summary.builder.cobertura

import com.github.ksoichiro.summary.CoverageReportClassConverter
import com.github.ksoichiro.summary.Summary
import com.github.ksoichiro.summary.builder.SummaryItemBuilder
import org.gradle.api.Project

class CoberturaCoverageSummaryItemBuilder extends SummaryItemBuilder {
    CoberturaCoverageSummaryItemBuilder(Project project) {
        super(project)
    }

    @Override
    Object[] dependsOn() {
        project.rootProject.allprojects.tasks.flatten().findAll { it.name.startsWith 'cobertura' }
    }

    @Override
    List<File> getInputFiles() {
        def reports = []
        //project.file("${project.buildDir}/reports/cobertura/coverage.xml")
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('net.saliman.cobertura') }.each {
            // Enable XML report carefully and silently
            def coberturaExtension = it.extensions.findByName('cobertura')
            if (coberturaExtension) {
                Set formats = coberturaExtension.coverageFormats
                if (formats && !formats.contains('xml')) {
                    coberturaExtension.coverageFormats += 'xml'
                }
                reports += it.file("${coberturaExtension.coverageReportDir}/coverage.xml")
            }
        }
        reports
    }

    @Override
    List<Summary> build() {
        def summaryContent = []
        def coberturaCoverageParser = new CoberturaCoverageParser()
        def coverageReportClassConverter = new CoverageReportClassConverter()
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('net.saliman.cobertura') }.each { Project p ->
            def coberturaExtension = p.extensions.findByName('cobertura')
            if (coberturaExtension) {
                File xml = p.file("${coberturaExtension.coverageReportDir}/coverage.xml")
                def cov = coberturaCoverageParser.parse(xml)
                summaryContent += new CoberturaCoverageSummary(
                    name: p.name,
                    title: 'Coverage[%]',
                    cssClasses: coverageReportClassConverter.convert(cov),
                    coverage: cov,
                    htmlReportFile: p.file("${coberturaExtension.coverageReportDir}/index.html"),
                )
            }
        }
        summaryContent
    }
}
