package com.github.ksoichiro.summary

import com.github.ksoichiro.summary.builder.SummaryItemBuilder
import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateSummaryTask extends DefaultTask {
    static final NAME = 'generateSummary'
    SummaryExtension extension
    List<SummaryItemBuilder> summaryItemBuilders
    File reportFile

    GenerateSummaryTask() {
        project.afterEvaluate {
            if (!rootProjectHasCheckTask()) {
                defineCheckTaskForRootProject()
            }
            if (!rootProjectHasCleanTask()) {
                defineCleanTaskForRootProject()
            }
            extension = project.extensions."${SummaryExtension.NAME}"
            summaryItemBuilders = extension.builders.collect { it.newInstance(project) }
            dependsOn summaryItemBuilders.collect { it.dependsOn() }?.findAll { it != null }?.flatten()
            reportFile = extension.destination ?: project.file("${project.buildDir}/reports/summary/index.html")
            inputs.files summaryItemBuilders.collect { it.getInputFiles() }?.findAll { it != null }?.flatten()
            outputs.file reportFile
        }
    }

    @TaskAction
    void exec() {
        createReportDir()

        def styleContent = getClass().getResourceAsStream("/templates/style.css").text

        def templateMap = [
            styleContent: styleContent,
            summaryContent: new SummaryItemMerger(project).merge(summaryItemBuilders.collect { it.build() }),
        ]

        TemplateConfiguration config = new TemplateConfiguration(
            useDoubleQuotes: true,
            expandEmptyElements: true,
            autoNewLine: true,
            autoIndent: true)
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
}
