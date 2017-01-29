package com.github.ksoichiro.summary.builder.cobertura

class CoberturaCoverageParser {
    float parse(File file) {
        def rootNode = new XmlParser(false, false).parseText(file.text.replaceAll("<!DOCTYPE[^>]*>", ""))
        Float.valueOf(rootNode.@"line-rate" as String)
    }
}
