
package com.studentworkflow

import react.dom.render
import kotlinx.browser.document

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    render(container) {
        child("h1") {
            attrs.onClick = { println("Hello, World!") }
            +"Hello, World!"
        }
    }
}
