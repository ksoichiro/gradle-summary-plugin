package com.github.ksoichiro.summary

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class GenerateSummaryTask extends DefaultTask {
    static final NAME = 'generateSummary'
    SummaryExtension extension
    File reportFile

    GenerateSummaryTask() {
        project.afterEvaluate {
            dependsOn allJacocoReportTasks()
            if (!rootProjectHasCheckTask()) {
                defineCheckTaskForRootProject()
            }
            if (!rootProjectHasCleanTask()) {
                defineCleanTaskForRootProject()
            }
            onlyIf {
                anyProjectHasJacocoPlugin()
            }
            extension = project.extensions."${SummaryExtension.NAME}"
            reportFile = extension.destination ?: project.file("${project.buildDir}/reports/summary/index.html")
            inputs.files jacocoReportFiles()
            outputs.file reportFile
        }
    }

    @TaskAction
    void exec() {
        createReportDir()

        def styleContent = getClass().getResourceAsStream("/templates/style.css").text
        def summaryContent = []
        project.rootProject.allprojects.findAll { it.plugins.hasPlugin('jacoco') }.each { Project p ->
            p.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }.each {
                File xml = it.reports.xml.destination
                def cov = coverage(xml)
                summaryContent += new Summary(
                    name: p.name,
                    cssClasses: covClasses(cov),
                    coverage: cov,
                    htmlReportFile: p.file("${it.reports.html.destination}/index.html"),
                )
            }
        }
        def templateMap = [
            styleContent: styleContent,
            summaryContent: summaryContent,
        ]

        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        Template template = engine.createTemplate(getClass().getResourceAsStream("/templates/index.tpl").text)

        Writer writer = new StringWriter()
        Writable output = template.make(templateMap)
        output.writeTo(writer)
        reportFile.text = writer.toString()

        println "Summary:"
        println "${reportFile.canonicalPath}"
    }

    def createReportDir() {
        def reportDir = reportFile.parentFile
        if (!reportDir.exists()) {
            project.mkdir(reportDir)
        }
    }

    def allJacocoReportTasks() {
        project.rootProject.allprojects.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }
    }

    def rootProjectHasCheckTask() {
        project.rootProject.tasks.flatten().any { it.name == 'check' }
    }

    def defineCheckTaskForRootProject() {
        project.tasks.create 'check'
        project.gradle.projectsEvaluated {
            project.rootProject.subprojects.tasks.flatten().findAll { it.name.contains('check') }.each {
                project.rootProject.tasks.check.mustRunAfter it
            }
        }
        project.rootProject.check.dependsOn NAME
    }

    def rootProjectHasCleanTask() {
        project.rootProject.tasks.flatten().any { it.name == 'clean' }
    }

    def defineCleanTaskForRootProject() {
        project.rootProject.tasks.create('clean') {
            doLast {
                if (reportFile?.exists()) {
                    project.rootProject.delete(reportFile)
                }
            }
        }
    }

    def anyProjectHasJacocoPlugin() {
        project.rootProject.allprojects.any { it.plugins.hasPlugin('jacoco') }
    }

    def jacocoReportFiles() {
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

    static covClasses(float cov) {
        if (80 <= cov) {
            return "fine"
        } else if (60 <= cov) {
            return "warn"
        } else {
            return "bad"
        }
    }

    static float coverage(File file) {
        def rootNode = new XmlParser(false, false).parseText(file.text.replaceAll("<!DOCTYPE[^>]*>", ""))
        def cov = 0.0f
        rootNode.counter.each { counter ->
            try {
                if ('INSTRUCTION' == counter.@type) {
                    def covered = Integer.valueOf(counter.@covered as String)
                    def missed = Integer.valueOf(counter.@missed as String)
                    cov = (100.0 * covered / (missed + covered)) as float
                }
            } catch (ignore) {
            }
        }
        cov
    }
}
