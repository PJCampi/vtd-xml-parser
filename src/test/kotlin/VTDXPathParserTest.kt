package com.nordea.solid.core.internal.io.xml.xpath

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.pjcampi.xml.xpath.XmlParsingException
import com.pjcampi.xml.xpath.vtd.VTDXPathParser
import com.pjcampi.xml.xpath.vtd.VTDXPathParserProvider
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class VTDXPathParserTest {

    data class Notional(
        val initialValue: Double,
        val currency: String,
        val stepDates: List<LocalDate> = listOf(),
        val stepValues: List<Float> = listOf()
    )

    companion object {

        private lateinit var parser: VTDXPathParser

        @BeforeAll
        @JvmStatic
        internal fun setupParser() {
            val xml = VTDXPathParserTest::class.java.getResource("/notional.xml")!!.readText()
            parser = VTDXPathParserProvider().provide(xml)
        }
    }

    @Test
    fun `test parsing of notional`() {
        val notional = parseNotional(parser)

        // assert that we parsed correctly
        assertEquals(26492000.0, notional.initialValue)
        assertEquals("DKK", notional.currency)
        assertEquals(45, notional.stepDates.size)
        assertEquals(45, notional.stepValues.size)

        // assert that the cursor has returned to it's initial position
        assertEquals("calculation", parser.currentNodeName)
    }

    @Test
    fun `test error in xpath`() {
        assertFailsWith(XmlParsingException::class) {
            parser.at("./notionalSchedule/notionalStepSchedule") {
                val initialValue = findTextOrNull("./initilValue")
                assertNull(initialValue)

                val default = findText("./initilValue", "default")
                assertEquals("default", default)

                findText("./initilValue")
            }
        }
        assertEquals("calculation", parser.currentNodeName)
    }

    @Test
    fun `test getting attribute value`() {
        assertEquals("value", parser.findAttribute("./notionalSchedule", "name"))
    }

    @Test
    fun `test complex query with attribute as filter`() {
        assertEquals("G16538", parser.findText("//party[@id='PARTY1']/person/personId"))
        assertEquals("G16538", parser.findText("//party[@id=//notionalSchedule/@href]/person/personId"))
    }

    @Test
    fun `test getting wrong attribute fails gracefully`() {
        assertFailsWith(XmlParsingException::class) {
            val nullDefault = parser.findAttributeOrNull("./notionalSchedule", "anotherName")
            assertNull(nullDefault)

            val default = parser.findAttribute("./notionalSchedule", "anotherName", "default")
            assertEquals("default", default)

            parser.findAttribute("./notionalSchedule", "anotherName")
        }
        assertEquals("calculation", parser.currentNodeName)
        assertNull(parser.findAttributeOrNull("./notionalSchedule", "anotherName"))
    }

    @Test
    fun `test namespace processing no namespace`() {
        val parser = VTDXPathParserProvider(true).provide("<root></root>")
        assertEquals("root", parser.currentNodeName)
    }

    @Test
    fun `test multiple namespaces`() {
        val parser = VTDXPathParserProvider(true).provide(
            """<root xmlns:myns = \http://www.myns.com/trade/1/canonical-message\ xmlns:xsi = \http://www.w3.org/2001/XMLSchema-instance\><myns:a><xsi:b><c>success</c></xsi:b></myns:a></root>"""
        )
        assertEquals("success", parser.findText("//myns:a/xsi:b/c"))
    }

    @Test
    fun `test to`() {
        val result = parser.at("./notionalSchedule/notionalStepSchedule") {
            to("initialValue")
            val initialValue = findText(".").toDouble()
            to("../currency")
            val currency = findText(".")
            Notional(initialValue, currency)
        }
        // we are already too low in the xml parser
        assertFailsWith(XmlParsingException::class) {
            parser.to("initialValue")
        }
        assertThat(result).isDataClassEqualTo(Notional(26492000.0, "DKK"))
    }

    @Test
    fun `run tests from multiple threads`() {
        val provider = VTDXPathParserProvider()
        var errorInThread = false
        fun toExecute(xml: String) {
            val parser = provider.provide(xml)
            try {
                repeat(1000) {
                    val notional = parseNotional(parser)
                    assertEquals(45, notional.stepDates.size)
                }
            } catch (t: Throwable) {
                errorInThread = true
            }
        }
        val xml = VTDXPathParserTest::class.java.getResource("/notional.xml")!!.readText()
        val thread1 = Thread { toExecute(xml) }.apply { start() }
        val thread2 = Thread { toExecute(xml) }.apply { start() }
        thread1.join()
        thread2.join()
        assertFalse(errorInThread, "There has been an error in a thread")
    }

    private fun parseNotional(parser: VTDXPathParser): Notional {
        return parser.at("./notionalSchedule/notionalStepSchedule") {
            val initialValue = findText("./initialValue").toDouble()
            val currency = findText("./currency")

            val stepDates = mutableListOf<LocalDate>()
            val stepValues = mutableListOf<Float>()
            iterateOn("./step") {
                stepDates.add(LocalDate.parse(findText("./stepDate"), DateTimeFormatter.BASIC_ISO_DATE))
                stepValues.add(findText("./stepValue").toFloat())
            }

            Notional(initialValue, currency, stepDates, stepValues)
        }
    }
}