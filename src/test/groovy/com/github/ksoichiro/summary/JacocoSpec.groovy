package com.github.ksoichiro.summary

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class JacocoSpec extends CoverageSpec {
    void setup() {
        buildFile << """
            plugins {
                id '${PLUGIN_ID}'
            }
            subprojects {
                apply plugin: 'java'
                apply plugin: 'jacoco'
            
                repositories {
                    mavenCentral()
                }
            
                dependencies {
                    testCompile 'junit:junit:4.11'
                }
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
                                <a href="file://${rootDir.canonicalPath}/project1/build/reports/jacoco/test/html/index.html" class="warn">72.22</a>
                            </td>
                        </tr><tr>
                            <td>
                                project2
                            </td><td class="right">
                                <a href="file://${rootDir.canonicalPath}/project2/build/reports/jacoco/test/html/index.html" class="bad">4.00</a>
                            </td>
                        </tr>
                    </table>
                </body>
            </html>""".stripIndent()

        where:
        gradleVersion  | _
        "3.3"          | _
        "2.14.1"       | _
    }
}
