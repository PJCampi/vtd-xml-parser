package com.pjcampi.xml.xpath.vtd

import com.pjcampi.xml.xpath.XmlParsingException
import com.ximpleware.AutoPilot
import com.ximpleware.VTDNav

val VTDNav.currentName: String
    get() = toNormalizedString(currentIndex)

fun VTDNav.getAttributeValue(attributeName: String): String {
    return getAttributeValueOrNull(attributeName) ?: throw XmlParsingException(
        "I could not find the attribute: $attributeName did not return any result from: ($currentName, $currentIndex)."
    )
}

fun VTDNav.getAttributeValueOrNull(attributeName: String): String? {
    val attributeIndex = getAttrVal(attributeName)
    return if (attributeIndex > -1) toNormalizedString(attributeIndex) else null
}

/**
 * @return the name of the element that the global cursor is pointing to: it will be the tag name if the cursor points
 * to a tag, the element text if the cursor points to the text part of a tag...
 */
fun AutoPilot.toName(vn: VTDNav): String {
    return safeEvalXPath(vn) {
        vn.currentName
    }
}

fun AutoPilot.toNameOrNull(vn: VTDNav): String? {
    return safeEvalXPathOrNull(vn) { vn.currentName }
}

/**
 * @return the value of the Attribute selected
 */
fun AutoPilot.toAttributeValue(vn: VTDNav, attributeName: String? = null): String {
    return safeEvalXPath(vn) {
        vn.getAttributeValue(attributeName ?: vn.currentName)
    }
}

fun AutoPilot.toAttributeValueOrNull(vn: VTDNav, attributeName: String? = null): String? {
    var result: String? = null
    safeEvalXPathOrNull(vn) {
        result = vn.getAttributeValueOrNull(attributeName ?: vn.currentName)
    }
    return result
}

/**
 * This function adds a context around autopilot XPath iteration, ensuring that the AutoPilot is correctly reset and
 * the global cursor is correctly moved back after the XPath is executed and the code block is executed.
 */
inline fun AutoPilot.safeEvalXPathAndIterate(vn: VTDNav, block: VTDNav.() -> Unit) {
    bind(vn)
    vn.push()
    try {
        while (this.evalXPath() > -1) {
            vn.block()
        }
    } finally {
        this.resetXPath()
        vn.pop()
    }
}

/**
 * This function adds a context around autopilot XPath execution, ensuring that the AutoPilot is correctly reset and
 * the global cursor is correctly moved back after the XPath is executed and the code block is executed.
 */
inline fun <R : Any> AutoPilot.safeEvalXPath(vn: VTDNav, block: VTDNav.() -> R): R {
    return safeEvalXPathOrNull(vn, block)
        ?: throw XmlParsingException(
            "The XPath expression: $exprString did not return any result from: (${vn.currentName}, ${vn.currentIndex})."
        )
}

inline fun <R : Any> AutoPilot.safeEvalXPathOrNull(vn: VTDNav, block: VTDNav.() -> R): R? {
    bind(vn)
    vn.push()
    try {
        return if (this.evalXPath() > -1) vn.block() else null
    } finally {
        this.resetXPath()
        vn.pop()
    }
}
