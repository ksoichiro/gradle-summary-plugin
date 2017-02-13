package com.github.ksoichiro.summary.builder.jacoco

class JacocoCoverageParser {
    static float parse(File file) {
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
