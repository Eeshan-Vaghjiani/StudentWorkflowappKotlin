
package com.studentworkflow

import react.dom.render
import kotlinx.browser.document

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    render(container) {
        App {}
    }
}
