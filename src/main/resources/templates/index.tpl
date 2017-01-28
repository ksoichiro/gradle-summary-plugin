html {
    head {
        title 'Summary'
        style styleContent
    }
    body {
        h3 'Summary'
        table {
            tr {
                summaryContent[0].each {
                    th it.title
                }
            }
            summaryContent.each { summaries ->
                tr {
                    summaries.each { summary ->
                        if (summary.hasLink()) {
                            td(class: summary.rightAligned ? 'right' : '') {
                                a(href: summary.link, class: summary.cssClasses, summary.content)
                            }
                        } else {
                            td {
                                yield summary.content
                            }
                        }
                    }
                }
            }
        }
    }
}
