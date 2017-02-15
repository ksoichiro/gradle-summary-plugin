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
        coberturaProjects().findAll { it.extensions.findByName('cobertura') != null }.collect {
            // Enable XML report carefully and silently
            def coberturaExtension = it.extensions.findByName('cobertura')
            Set formats = coberturaExtension.coverageFormats
            if (formats && !formats.contains('xml')) {
                coberturaExtension.coverageFormats += 'xml'
            }
            coverageReportFile(it, coberturaExtension)
        }
    }

    @Override
    List<Summary> build() {
        def coverageReportClassConverter = new CoverageReportClassConverter()
        coberturaProjects().findAll { it.extensions.findByName('cobertura') != null }.collect { Project p ->
            def coberturaExtension = p.extensions.findByName('cobertura')
            File xml = coverageReportFile(p, coberturaExtension)
            def cov = CoberturaCoverageParser.parse(xml)
            new CoberturaCoverageSummary(
                name: p.name,
                title: 'Coverage[%]',
                cssClasses: coverageReportClassConverter.convert(cov),
                coverage: cov,
                htmlReportFile: p.file("${coberturaExtension.coverageReportDir}/index.html"),
            )
        }
    }

    def coberturaProjects() {
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('net.saliman.cobertura') }
    }

    static File coverageReportFile(it, coberturaExtension) {
        it.file("${coberturaExtension.coverageReportDir}/coverage.xml")
    }
}
