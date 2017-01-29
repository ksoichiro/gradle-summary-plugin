package com.github.ksoichiro.summary

import com.github.ksoichiro.summary.builder.AllProjectsSummaryItemBuilder
import com.github.ksoichiro.summary.builder.SummaryItemBuilder
import com.github.ksoichiro.summary.builder.cobertura.CoberturaCoverageSummaryItemBuilder
import com.github.ksoichiro.summary.builder.jacoco.JacocoCoverageSummaryItemBuilder

class SummaryExtension {
    static final NAME = 'summary'
    List<Class<? extends SummaryItemBuilder>> builders = [
        AllProjectsSummaryItemBuilder,
        JacocoCoverageSummaryItemBuilder,
        CoberturaCoverageSummaryItemBuilder,
    ]
    File destination
}
