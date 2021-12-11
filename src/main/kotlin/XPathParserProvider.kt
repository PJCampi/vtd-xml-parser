package com.pjcampi.xml.xpath

import java.lang.IllegalArgumentException

interface XPathParserProvider {
    /** Provides an XPathParser */
    @Throws(IllegalArgumentException::class)
    fun provide(document: String): XPathParser
}
