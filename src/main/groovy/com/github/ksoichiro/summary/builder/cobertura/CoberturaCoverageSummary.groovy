package com.github.ksoichiro.summary.builder.cobertura

import com.github.ksoichiro.summary.Summary

class CoberturaCoverageSummary extends Summary {
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
        sprintf('%3.2f', (coverage * 100).round(2))
    }
}
