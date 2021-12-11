package com.pjcampi.xml.xpath.vtd

import com.pjcampi.xml.xpath.XPathParser
import com.pjcampi.xml.xpath.XmlParsingException
import com.ximpleware.AutoPilot
import com.ximpleware.VTDNav
import org.apache.logging.log4j.kotlin.Logging

open class VTDXPathParser() : XPathParser {

    companion object : Logging

    constructor(vn: VTDNav) : this() {
        this.nav = vn
    }

    internal var nav: VTDNav
        get() = _nav
        set(value) {
            autoPilots.values.forEach { it.bind(value) }
            _nav = value
        }

    private lateinit var _nav: VTDNav

    private val autoPilots = mutableMapOf<String, AutoPilot>()
    private val namespaces = mutableMapOf<String, String>()

    override val currentNodeName: String
        get() = nav.currentName

    override fun addNamespace(prefix: String, url: String) {
        if (prefix in namespaces && namespaces[url] != url) {
            logger.warn {
                "Namespace with name: $prefix already exists with URI: ${namespaces[prefix]}. Overriding to $url."
            }
        }
        namespaces[prefix] = url
        autoPilots.values.forEach { it.declareXPathNameSpace(prefix, url) }
    }

    override fun <R : Any> at(xPath: String, block: XPathParser.() -> R): R {
        return getAutoPilot(xPath).safeEvalXPath(nav) { this@VTDXPathParser.block() }
    }

    override fun <R : Any> atOrNull(xPath: String, block: XPathParser.() -> R): R? {
        return getAutoPilot(xPath).safeEvalXPathOrNull(nav) { this@VTDXPathParser.block() }
    }

    override fun find(xPath: String): String {
        val ap = getAutoPilot(xPath)
        return ap.toName(nav)
    }

    override fun find(xPath: String, default: String): String {
        return findOrNull(xPath) ?: default
    }

    override fun findOrNull(xPath: String): String? {
        val ap = getAutoPilot(xPath)
        return ap.toNameOrNull(nav)
    }

    override fun findAttribute(xPath: String, attributeName: String): String {
        return getAutoPilot(xPath).toAttributeValue(nav, attributeName)
    }

    override fun findAttribute(xPath: String, attributeName: String, default: String): String {
        return findAttributeOrNull(xPath, attributeName) ?: default
    }

    override fun findAttributeOrNull(xPath: String, attributeName: String): String? {
        return getAutoPilot(xPath).toAttributeValueOrNull(nav, attributeName)
    }

    override fun findText(xPath: String): String {
        try {
            return at(xPath) { nav.toNormalizedString(nav.text) }
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw XmlParsingException("No text found for $xPath from: (${nav.currentName}, ${nav.currentIndex})")
        }
    }

    override fun findText(xPath: String, default: String): String {
        return findTextOrNull(xPath) ?: default
    }

    override fun findTextOrNull(xPath: String): String? {
        var result: String? = null
        atOrNull(xPath) {
            if (nav.text != -1) result = nav.toNormalizedString(nav.text)
        }
        return result
    }

    override fun iterateOn(xPath: String, block: XPathParser.() -> Unit) {
        return getAutoPilot(xPath).safeEvalXPathAndIterate(nav) { this@VTDXPathParser.block() }
    }

    override fun to(xPath: String) {
        val ap = getAutoPilot(xPath)
        ap.resetXPath()
        val currentIndex = nav.currentIndex
        if (ap.evalXPath() == -1) {
            nav.recoverNode(currentIndex)
            throw XmlParsingException("I could not move to $xPath from: (${nav.currentName}, ${nav.currentIndex})")
        }
    }

    override fun toString(): String {
        return "${this::class.simpleName}[${this.hashCode()}]@(${this.nav.currentName}, ${this.nav.currentIndex})"
    }

    private fun getAutoPilot(xPath: String): AutoPilot {
        return autoPilots.getOrPut(xPath) {
            AutoPilot().apply {
                namespaces.forEach { declareXPathNameSpace(it.key, it.value) }
                bind(this@VTDXPathParser.nav)
                selectXPath(xPath)
            }
        }
    }
}
