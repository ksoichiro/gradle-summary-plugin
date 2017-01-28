html {
    head {
        title 'Summary'
        style styleContent
    }
    body {
        h3 'Summary'
        table {
            tr {
                th 'Project'
                th 'Coverage[%]'
            }
            summaryContent.each { summary ->
                tr {
                    td summary.name
                    td(class: 'right') {
                        a(href: "file://${summary.htmlReportFile.canonicalPath}", class: summary.cssClasses) {
                            yield sprintf('%3.2f', summary.coverage.round(2))
                        }
                    }
                }
            }
        }
    }
}
