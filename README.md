# VTD-XML parser

## What is it?

This package provides a wrapper around [`VTD-XML`](https://github.com/dryade/vtd-xml) that simplifies the parsing of complex 
XML documents.

It leverages Kotlin to expose an API that resembles that of the `xml.etree` package in Python without building an XML 
tree in memory. 


## Example

Let's say you need to parse the following XML element:
```
<calculation>
    <notionalSchedule name="value" href= "PARTY1">
        <notionalStepSchedule>
            <initialValue>26492000</initialValue>
            <step>
                <stepDate>2003-04-22</stepDate>
                <stepValue>26492000</stepValue>
            </step>
            <step>
                <stepDate>2003-10-17</stepDate>
                <stepValue>26143265.416350599</stepValue>
            </step>
            <currency currencyScheme='http://www.fpml.org/coding-scheme/external/iso4217-2016-07-01'>DKK</currency>
        </notionalStepSchedule>
    </notionalSchedule>
</calculation>
```

into the following `data class`:
```
data class Notional(
    val initialValue: Double,
    val currency: String,
    val stepDates: List<LocalDate> = listOf(),
    val stepValues: List<Float> = listOf()
)
```

In order to achieve this with `VTD-XML` you need to:
 - move the `VTD-XML` cursor to the element: `calculation/notionalSchedule/notionalStepSchedule`.
 - read the `initialValue` element's text and convert it into a double
 - iterate over the `step` elements and add its (converted) content to `stepDates` and `stepValues` collections
 - read the `currency` element's text
 - reset the position of the `VTD-XML` cursor, so it can be used for further processing
 
This is achieved with a few lines of code:
```
parser.at("./notionalSchedule/notionalStepSchedule") {
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
```

We use the following methods of the `VTDXPathParser`:
 - `at` executes the code `block` provided at the first element matching the `xpath` expression supplied. The cursor is then 
   moved back to its original position.
 - `findText` returns the text value of the first element matching the `xpath` expression provided. There are a handful 
   of other methods that help you operate on an element if found (f.ex. `findAttribute`). Have a closer look at 
   the `XPathParser` API documentation for more information.
 - `iterateOn`is used to execute a code `block` on each element matching the `xpath` expression provided. 

`VTDXPathParser` always returns the cursor back to its initial position would the method called throw an exception. You 
remain in control of the position of your cursor.

This module simplifies a few other things (f.ex. managing namespaces). Have a look at its API or the unit tests for more 
information.