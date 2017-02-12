package com.github.ksoichiro.summary

class Utils {
    static createFile(String path) {
        def file = new File(path)
        file.parentFile.mkdirs()
        file
    }

    static createFile(dir, String path) {
        createFile("${dir}/${path}")
    }

    static createFile(String dir, Closure closure) {
        File file = createFile(dir)
        closure.call(file)
    }

    static createFile(dir, path, Closure closure) {
        createFile("${dir}/${path}", closure)
    }
}
