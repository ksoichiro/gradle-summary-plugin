package com.github.ksoichiro.summary

import com.github.ksoichiro.summary.builder.AllProjectsSummaryItemBuilder
import com.github.ksoichiro.summary.builder.jacoco.JacocoCoverageSummaryItemBuilder
import com.github.ksoichiro.summary.builder.SummaryItemBuilder

class SummaryExtension {
    static final NAME = 'summary'
    List<Class<? extends SummaryItemBuilder>> builders = [AllProjectsSummaryItemBuilder, JacocoCoverageSummaryItemBuilder]
    File destination
}
