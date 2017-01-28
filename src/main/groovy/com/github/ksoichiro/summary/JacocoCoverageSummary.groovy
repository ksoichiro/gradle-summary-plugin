package com.github.ksoichiro.summary

class JacocoCoverageSummary extends Summary {
    File htmlReportFile
    float coverage

    @Override
    boolean isRightAligned() {
        rightAligned = true
    }

    @Override
    boolean hasLink() {
        true
    }

    @Override
    String getLink() {
        "file://${htmlReportFile.canonicalPath}"
    }

    @Override
    Object getContent() {
        sprintf('%3.2f', coverage.round(2))
    }
}
