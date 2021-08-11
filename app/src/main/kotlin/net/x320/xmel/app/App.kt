@file:JvmName("Main")
package net.x320.xmel.app

import net.x320.xmel.utilities.StringUtils

import org.apache.commons.text.WordUtils
import java.io.IOException
import javax.xml.stream.XMLStreamException

fun main() {
    val tokens = StringUtils.split(MessageUtils.getMessage())
    val result = StringUtils.join(tokens)
    println(WordUtils.capitalize(result))
}
