package com.github.ksoichiro.summary

import com.github.ksoichiro.summary.builder.SummaryItemBuilder
import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

class GenerateSummaryTask extends DefaultTask implements Reporting<SummaryReports> {
    static final NAME = 'generateSummary'
    SummaryExtension extension
    List<SummaryItemBuilder> summaryItemBuilders

    @Nested
    final SummaryReportsImpl reports = (SummaryReportsImpl) getInstantiator().newInstance(SummaryReportsImpl, this)

    GenerateSummaryTask() {
        group = 'reporting'
        reports.html.enabled = true
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
            (new DslObject(reports.html)).conventionMapping.with {
                enabled = true
                destination = {
                    extension.destination ?: project.file("${project.buildDir}/reports/summary/index.html")
                }
            }
            inputs.files summaryItemBuilders.collect { it.getInputFiles() }?.findAll { it != null }?.flatten()
        }
    }

    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException()
    }

    @Override
    SummaryReports getReports() {
        reports
    }

    @Override
    SummaryReports reports(Closure closure) {
        reports.configure closure
    }

    SummaryReports reports(Action<? super SummaryReports> action) {
        action.execute reports
        reports
    }

    @TaskAction
    void exec() {
        if (!reports.html.isEnabled()) {
            didWork = false
            return
        }

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
        reports.html.destination.text = writer.toString()

        println "Summary:"
        println "${reports.html.destination.canonicalPath}"
    }

    def createReportDir() {
        def reportDir = reports.html.destination.parentFile
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
                if (reports.html.destination?.exists()) {
                    project.rootProject.delete(reports.html.destination)
                }
            }
        }
    }
}
