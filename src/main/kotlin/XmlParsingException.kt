package com.pjcampi.xml.xpath

class XmlParsingException(override val message: String = "", cause: Throwable? = null) : RuntimeException(message, cause)
