package com.github.ksoichiro.summary

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.Unroll

class CoberturaSpec extends CoverageSpec {
    void setup() {
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
                dependencies {
                    classpath 'net.saliman:gradle-cobertura-plugin:2.4.0'
                }
            }
            plugins {
                id '${PLUGIN_ID}'
            }
            subprojects {
                apply plugin: 'java'
                apply plugin: 'net.saliman.cobertura'
            
                repositories {
                    mavenCentral()
                }
            
                dependencies {
                    testCompile 'junit:junit:4.11'
                    testRuntime 'org.slf4j:slf4j-log4j12:1.7.5'
                }

                check.dependsOn('cobertura')
            }
            """
    }

    @Unroll
    def "build with #gradleVersion"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(rootDir)
            .withArguments("check")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath(pluginClasspath)
            .build()
        println result.output

        then:
        new File("${rootDir}/project1/build/cobertura/cobertura.ser").canonicalFile.exists()
        new File("${rootDir}/project1/build/reports/cobertura/coverage.xml").canonicalFile.exists()
        new File("${rootDir}/project2/build/cobertura/cobertura.ser").canonicalFile.exists()
        new File("${rootDir}/project2/build/reports/cobertura/coverage.xml").canonicalFile.exists()
        TaskOutcome.SUCCESS == result.task(":check").getOutcome()
        new File("${rootDir}/build/reports/summary/index.html").text == """\
            <html>
                <head>
                    <title>Summary</title><style>body {
              font-size: 14px;
              font-family: "Helvetica Neue"
            }
            h3 {
              font-size: 24px;
            }
            table {
              border-collapse: collapse;
            }
            td,th {
              border: 1px solid #ddd;
              padding: 4px;
            }
            th {
              font-weight: bold;
              border-bottom-width: 2px;
            }
            .right {
              text-align: right;
            }
            .bad {
              color: #d9534f;
            }
            .warn {
              color: #f0ad4e;
            }
            .fine {
              color: #5cb85c;
            }
            </style>
                </head><body>
                    <h3>Summary</h3><table>
                        <tr>
                            <th>Project</th><th>Coverage[%]</th>
                        </tr><tr>
                            <td>
                                project1
                            </td><td class="right">
                                <a href="file://${rootDir.canonicalPath}/project1/build/reports/cobertura/index.html" class="bad">${coverage1}</a>
                            </td>
                        </tr><tr>
                            <td>
                                project2
                            </td><td class="right">
                                <a href="file://${rootDir.canonicalPath}/project2/build/reports/cobertura/index.html" class="bad">${coverage2}</a>
                            </td>
                        </tr>
                    </table>
                </body>
            </html>""".stripIndent()

        where:
        gradleVersion  | coverage1 | coverage2
        // In this test with Gradle 3.3, test task throws Exception,
        // which result in empty coverage.
        //   Exception: java.lang.IllegalStateException thrown from the UncaughtExceptionHandler in thread "Thread-4"
        "3.3"          | 0.00      | 0.00
        "2.14.1"       | 71.43     | 3.85
    }
}
