package com.github.ksoichiro.summary

class CoverageReportClassConverter {
    static final CLASS_FINE = "fine"
    static final CLASS_WARN = "warn"
    static final CLASS_BAD = "bad"

    def thresholdFine = 80
    def thresholdWarn = 60

    String convert(float cov) {
        if (thresholdFine <= cov) {
            return CLASS_FINE
        } else if (thresholdWarn <= cov) {
            return CLASS_WARN
        } else {
            return CLASS_BAD
        }
    }
}
