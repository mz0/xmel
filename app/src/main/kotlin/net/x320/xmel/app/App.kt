@file:JvmName("Main")
package net.x320.xmel.app

import net.x320.xmel.utilities.StringUtils

import org.apache.commons.text.WordUtils
import java.io.IOException
import javax.xml.stream.XMLStreamException

fun main() {
    val tokens = StringUtils.split(MessageUtils.getMessage())
    val result = WordUtils.capitalize(StringUtils.join(tokens))
    val fileName =  "/home/mz0/e/smeh/in/mitch_TP_m2.xml"
    val xmel = XmelReader()
    try {
        val results = xmel.read(fileName)
    } catch (e: IOException) {
        error(e.message.toString())
    } catch (e: XMLStreamException) {
        error(e.message.toString())
    }
    println("\nShared field definitions in Dictionary file $fileName:")
    xmel.sharedFields.forEach { f ->
        if (f.usage == 0) {
            println("${f.id} is never used")
        } else if (f.usage == 1) {
            println("${f.id} is used only once")
        } else {
            println("${f.id} is used ${f.usage} times")
        }
    }
}
