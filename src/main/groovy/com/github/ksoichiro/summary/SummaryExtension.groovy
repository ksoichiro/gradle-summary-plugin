package com.github.ksoichiro.summary

class SummaryExtension {
    static final NAME = 'summary'
    List<Class<? extends SummaryItemBuilder>> builders = [AllProjectsSummaryItemBuilder, JacocoCoverageSummaryItemBuilder]
    File destination
}
