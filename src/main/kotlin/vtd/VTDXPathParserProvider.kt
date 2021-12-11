package com.pjcampi.xml.xpath.vtd

import com.pjcampi.xml.xpath.XPathParserProvider
import com.ximpleware.ParseException
import com.ximpleware.VTDGen
import com.ximpleware.VTDNav
import java.nio.charset.StandardCharsets

class VTDXPathParserProvider(private val namespaceAware: Boolean = false) : XPathParserProvider {

    private val vtdGenProvider: ThreadLocal<VTDGen> = ThreadLocal.withInitial { VTDGen() }
    private val vtdXPathParserProvider: ThreadLocal<VTDXPathParser> = ThreadLocal.withInitial { VTDXPathParser() }

    override fun provide(document: String): VTDXPathParser {
        val vg = vtdGenProvider.get()
        val vtdXPathParser = vtdXPathParserProvider.get()
        try {
            vg.setDoc(document.toByteArray(StandardCharsets.UTF_8))
            vg.parse(namespaceAware)
            vtdXPathParser.nav = vg.nav
            if (namespaceAware) {
                getRootNamespaces(vtdXPathParser.nav).forEach { vtdXPathParser.addNamespace(it.key, it.value) }
            }
            return vtdXPathParser
        } catch (e: ParseException) {
            throw IllegalArgumentException("Failed to parse xml document", e)
        }
    }

    private fun getRootNamespaces(nav: VTDNav): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var index = nav.rootIndex
        val nTokens = nav.tokenCount
        while (nav.getTokenDepth(index) == 0 && index < nTokens) {
            index++
            val maybeNamespacePrefix = nav.toNormalizedString(index)
            if (maybeNamespacePrefix.startsWith("xmlns:")) {
                index++
                val prefix = maybeNamespacePrefix.substring(6)
                val url = nav.toNormalizedString(index)
                result[prefix] = url
            }
        }
        return result
    }
}