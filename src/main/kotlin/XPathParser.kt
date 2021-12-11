package com.pjcampi.xml.xpath

interface XPathParser {

    /**
     * @return the name of the current node of the parser
     */
    val currentNodeName: String

    /**
     * Adds a namespace to the xpath parser.
     */
    fun addNamespace(prefix: String, url: String)

    /**
     * @return the result of the [block] of code executed from the xml element found for [xPath] and return to
     * the current xml element.
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    @Throws(XmlParsingException::class)
    fun <R : Any> at(xPath: String, block: XPathParser.() -> R): R

    @Throws(XmlParsingException::class)
    fun <R : Any> atOrNull(xPath: String, block: XPathParser.() -> R): R?

    /**
     * @return the string value of the first xml element found for [xPath] (tag name, attribute, text).
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    @Throws(XmlParsingException::class)
    fun find(xPath: String): String

    fun find(xPath: String, default: String): String

    /**
     * @return the string value of the first xml element found for [xPath] (tag name, attribute, text) or null if
     * the XPath did not return any element.
     */
    fun findOrNull(xPath: String): String?

    /**
     * @return the text of the first xml element found for [xPath]
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    @Throws(XmlParsingException::class)
    fun findText(xPath: String): String

    /**
     * @return the text of the first xml element found for [xPath] or default if the XPath did not return any element
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    fun findText(xPath: String, default: String): String

    /**
     * @return the text of the first xml element found for [xPath] or null if the XPath did not return any element.
     */
    fun findTextOrNull(xPath: String): String?

    /**
     * @return the value of [attributeName] for the first xml element found for [xPath]
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    @Throws(XmlParsingException::class)
    fun findAttribute(xPath: String, attributeName: String): String

    /**
     * @return the value of [attributeName] for the first xml element found for [xPath] or default if the XPath
     * did not return any element.
     * @throws [XmlParsingException] if the XPath did not return any element.
     */
    fun findAttribute(xPath: String, attributeName: String, default: String): String

    /**
     * @return the value of [attributeName] for the first xml element found for [xPath] or null if the XPath
     * did not return any element.
     */
    fun findAttributeOrNull(xPath: String, attributeName: String): String?

    /**
     * Iterate over all the elements found for [xPath]
     */
    fun iterateOn(xPath: String, block: XPathParser.() -> Unit)

    /**
     * moves the cursor to the [xPath] selected if it was found or throws a [XmlParsingException] otherwise.
     */
    @Throws(XmlParsingException::class)
    fun to(xPath: String)
}
