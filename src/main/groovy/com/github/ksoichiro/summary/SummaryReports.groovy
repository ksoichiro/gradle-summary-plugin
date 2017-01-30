package com.github.ksoichiro.summary

import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport

interface SummaryReports extends ReportContainer<SingleFileReport> {
    SingleFileReport getHtml()
}
