package com.github.ksoichiro.summary

class Summary {
    String name
    String title
    String link
    String cssClasses
    boolean rightAligned
    def content

    boolean hasLink() {
        link != null
    }
}
