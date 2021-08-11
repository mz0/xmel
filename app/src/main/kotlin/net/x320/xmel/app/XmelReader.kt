package net.x320.xmel.app

import java.io.IOException
import java.io.FileInputStream
import java.util.ArrayList
import java.util.HashMap
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.events.Attribute

class XmelReader {
    private var sharedFields: MutableMap<String, RecordField>? = null
    private var recordTypes: MutableMap<String, Record>? = null
    private var sharedFieldsDone = false
    private var messageStarted = false

    fun getSharedFields(): Collection<RecordField>? {
        return if (sharedFields != null) sharedFields!!.values else null
    }

    @Throws(IOException::class, XMLStreamException::class)
    fun read(fileName: String): List<Record> {
        val messages: List<Record> = ArrayList()
        FileInputStream(fileName).use { stream ->
            val inputFactory = XMLInputFactory.newInstance()
            val eventReader = inputFactory.createXMLEventReader(stream)
            while (eventReader.hasNext()) {
                val event = eventReader.nextEvent()
                if (event.isStartElement) {
                    val startElement = event.asStartElement()
                    when (val elementName = startElement.name.localPart) {
                        Fields -> {
                            if (sharedFields != null) throw XMLStreamException("More than one <fields> element?")
                            sharedFields = HashMap()
                        }
                        FIELD -> {
                            val fieldsSection = !sharedFieldsDone // this is a <fields> member, not a <message> member
                            val attributes = startElement.attributes
                            var id: String? = null
                            var name: String? = null
                            var ref: String? = null
                            while (attributes.hasNext()) {
                                val attribute = attributes.next()
                                val attrName = attribute.name.toString()
                                if (attrName == idAttr) {
                                    id = attribute.value
                                } else if (attrName == nameAttr) {
                                    name = attribute.value
                                } else if (attrName == refAttr) {
                                    ref = attribute.value
                                }
                            }
                            if (fieldsSection) {
                                if (id == null) {
                                    throw XMLStreamException("shared <field> has no 'id' attribute")
                                }
                                if (sharedFields!!.containsKey(id)) {
                                    throw XMLStreamException("Non-unique <field> id=$id")
                                }
                                val rf = RecordField(id, name, true)
                                sharedFields!![id] = rf
                            } else { // we must be inside <message> element
                                if (ref != null) { // it's a shared field definition
                                    if (sharedFields!!.containsKey(ref)) {
                                        sharedFields!![ref]!!.addUsage()
                                    } else {
                                        throw XMLStreamException("Undefined field reference '$ref'")
                                    }
                                    if (id != null) {
                                        println("odd field reference '$ref' has id=$id")
                                    }
                                }
                            }
                        }
                        Messages -> {
                            if (recordTypes != null) {
                                throw XMLStreamException("More than one <messages> element?")
                            }
                            recordTypes = HashMap()
                        }
                        MESSAGE -> {
                            val attributes = startElement.attributes
                            var id: String? = null
                            var name: String? = null
                            while (attributes.hasNext()) {
                                val attribute: Attribute = attributes.next()
                                val attrName = attribute.name.toString()
                                if (attrName == idAttr) {
                                    id = attribute.value
                                } else if (attrName == nameAttr) {
                                    name = attribute.value
                                }
                            }
                            if (id == null) {
                                throw XMLStreamException("<message> has no 'id' attribute")
                            }
                            if (recordTypes!!.containsKey(id)) {
                                throw XMLStreamException("Non-unique <message> id=$id")
                            }
                            recordTypes!![id] = Record(id, name, ArrayList())
                            messageStarted = true
                        }
                        elValue, elAttribute -> {
                        }
                        else -> println("Element <$elementName>")
                    }
                } else if (event.isEndElement) {
                    val elementName = event.asEndElement().name.localPart
                    if (elementName == Fields) {
                        if (sharedFieldsDone) {
                            System.err.println("<messages> seems already done")
                        }
                        sharedFieldsDone = true
                    } else if (elementName == MESSAGE) {
                        if (!messageStarted) {
                            System.err.println("<message> had not started, yet EndElement event occurred")
                        }
                        messageStarted = false
                    }
                }
            }
        }
        return messages
    }

    companion object {
        const val Fields = "fields"
        const val FIELD = "field"
        const val Messages = "messages"
        const val MESSAGE = "message"
        const val elValue = "value"
        const val elAttribute = "attribute"
        const val idAttr = "id"
        const val nameAttr = "name"
        const val refAttr = "reference"
    }
}