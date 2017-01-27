package com.github.ksoichiro.summary

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
            onlyIf {
                anyProjectHasJacocoPlugin()
            }
            extension = project.extensions."${SummaryExtension.NAME}"
            reportFile = project.file("${project.buildDir}/reports/summary/index.html")
            inputs.files jacocoReportFiles()
            outputs.file reportFile
        }
    }

    @TaskAction
    void exec() {
        def reportDir = reportFile.parentFile
        if (!reportDir.exists()) {
            project.mkdir(reportDir)
        }
        reportFile.text = """\
            |<html>
            |<head>
            |<title>Summary</title>
            |<style>
            |body {
            |  font-size: 14px;
            |  font-family: "Helvetica Neue"
            |}
            |h3 {
            |  font-size: 24px;
            |}
            |table {
            |  border-collapse: collapse;
            |}
            |td,th {
            |  border: 1px solid #ddd;
            |  padding: 4px;
            |}
            |th {
            |  font-weight: bold;
            |  border-bottom-width: 2px;
            |}
            |.right {
            |  text-align: right;
            |}
            |.bad {
            |  color: #d9534f;
            |}
            |.warn {
            |  color: #f0ad4e;
            |}
            |.fine {
            |  color: #5cb85c;
            |}
            |</style>
            |</head>
            |<body>
            |<h3>Summary</h3>
            |<table>
            |<tr>
            |  <th>Project</th>
            |  <th>Coverage[%]</th>
            |</tr>
            |""".stripMargin().stripIndent()
        project.rootProject.allprojects.each {
            it.findAll { it.plugins.hasPlugin('jacoco') }.each { Project p ->
                it.tasks.flatten().findAll { it.class.simpleName.startsWith 'JacocoReport' }.each {
                    File xml = it.reports.xml.destination
                    def cov = coverage(xml)
                    def classes = covClasses(cov)
                    def htmlReportFile = p.file("${it.reports.html.destination}/index.html")
                    reportFile.text += """\
                        |<tr>
                        |  <td>${p.name}</td>
                        |  <td class="right"><a href="file://${htmlReportFile.canonicalPath}" class="${classes}">${sprintf('%3.2f', cov.round(2))}</a></td>
                        |</tr>
                        |""".stripMargin().stripIndent()
                }
            }
        }
        reportFile.text += """\
            |</table>
            |</div>
            |</body>
            |</html>
            |""".stripMargin().stripIndent()
        println "Summary:"
        println "${reportFile.canonicalPath}"
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
