package com.github.ksoichiro.summary

import org.gradle.api.Task
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer

class SummaryReportsImpl extends TaskReportContainer<SingleFileReport> implements SummaryReports {
    SummaryReportsImpl(Task task) {
        super(SingleFileReport, task)
        add(TaskGeneratedSingleFileReport, "html", task)
    }

    @Override
    SingleFileReport getHtml() {
        getByName("html")
    }
}
