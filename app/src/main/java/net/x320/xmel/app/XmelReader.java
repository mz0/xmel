package net.x320.xmel.app;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XmelReader {
    static final String Fields = "fields";
    static final String FIELD = "field";
    static final String Messages = "messages";
    static final String MESSAGE = "message";
    static final String elValue = "value";
    static final String elAttribute = "attribute";
    static final String idAttr = "id";
    static final String nameAttr = "name";
    static final String refAttr = "reference";
    private Map<String, RecordField> sharedFields;
    private Map<String, Record> recordTypes;
    private boolean sharedFieldsDone;
    private boolean messageStarted;

    public Collection<RecordField> getSharedFields() {
        return sharedFields != null ? sharedFields.values() : null;
    }

    public List<Record> read(String fileName) throws IOException, XMLStreamException {
        List<Record> messages = new ArrayList<>();
        try (InputStream in = new FileInputStream(fileName)) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    String elementName = startElement.getName().getLocalPart();
                    switch (elementName) {
                        case Fields:
                            if (sharedFields != null) throw new XMLStreamException("More than one <fields> element?");
                            sharedFields = new HashMap<>();
                            break;
                        case FIELD:
                            boolean fieldsSection = !sharedFieldsDone; // this is a <fields> member, not a <message> member
                            Iterator<Attribute> attributes = startElement.getAttributes();
                            String id = null, name = null, ref = null;
                            while (attributes.hasNext()) {
                                Attribute attribute = attributes.next();
                                String attrName = attribute.getName().toString();
                                if (attrName.equals(idAttr)) {
                                    id = attribute.getValue();
                                } else if (attrName.equals(nameAttr)) {
                                    name = attribute.getValue();
                                } else if (attrName.equals(refAttr)) {
                                    ref = attribute.getValue();
                                }
                            }
                            if (fieldsSection) {
                                if (id == null) {
                                    throw new XMLStreamException("shared <field> has no 'id' attribute");
                                }
                                if (sharedFields.containsKey(id)) {
                                    throw new XMLStreamException("Non-unique <field> id=" + id);
                                }
                                RecordField rf = new RecordField(id, name, true);
                                sharedFields.put(id, rf);
                            } else { // we must be inside <message> element
                                if (ref != null) { // it's a shared field definition
                                    if (sharedFields.containsKey(ref)) {
                                        sharedFields.get(ref).addUsage();
                                    } else {
                                        throw new XMLStreamException("Undefined field reference '" + ref + "'");
                                    }
                                    if (id != null) {
                                        System.out.println("odd field reference '" + ref + "' has id=" + id);
                                    }
                                }
                            }
                            break;
                        case Messages:
                            if (recordTypes != null) {
                                throw new XMLStreamException("More than one <messages> element?");
                            }
                            recordTypes = new HashMap<>();
                            break;
                        case MESSAGE:
                            attributes = startElement.getAttributes();
                            id = null; name = null;
                            while (attributes.hasNext()) {
                                Attribute attribute = attributes.next();
                                String attrName = attribute.getName().toString();
                                if (attrName.equals(idAttr)) {
                                    id = attribute.getValue();
                                } else if (attrName.equals(nameAttr)) {
                                    name = attribute.getValue();
                                }
                            }
                            if (id == null) {
                                throw new XMLStreamException("<message> has no 'id' attribute");
                            }
                            if (recordTypes.containsKey(id)) {
                                throw new XMLStreamException("Non-unique <message> id=" + id);
                            }
                            recordTypes.put(id, new Record(id, name, new ArrayList<>()));
                            messageStarted = true;
                            break;
                        case elValue:
                        case elAttribute:
                            break;
                        default:
                            System.out.println("Element <" + elementName + ">");
                    }
                } else if (event.isEndElement()) {
                    String elementName = event.asEndElement().getName().getLocalPart();
                    if (elementName.equals(Fields)) {
                        if (sharedFieldsDone) {
                            System.err.println("<messages> seems already done");
                        }
                        sharedFieldsDone = true;
                    } else if (elementName.equals(MESSAGE)) {
                        if (!messageStarted) {
                            System.err.println("<message> had not started, yet EndElement event occurred");
                        }
                        messageStarted = false;
                    }
                }
            }
        }
        return messages;
    }
}
