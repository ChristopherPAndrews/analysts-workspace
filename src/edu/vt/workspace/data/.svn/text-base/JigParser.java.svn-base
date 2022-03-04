package edu.vt.workspace.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This is a class that provides basic parsing facilities for .jig files
 * @todo there is a bug in here that creates multiple copies of the same entity
 *
 * @author cpa
 */
public class JigParser {




    /**
     * This reads document objects out of jig files. Documents consist of a collection of fields.
     * The typical fields are docID, docDate, docSource, and docText. Other fields are
     * interpreted as entities associated with this document.
     *
     * @param xmlEventReader an open XMLEventReader that has just read the start of a document object
     * @return
     * @throws XMLStreamException
     */
    public static AWDocument parseDocDescription(XMLEventReader xmlEventReader) throws XMLStreamException {
        AWDocument doc = new AWDocument();
        XMLEvent event;
        SimpleElement<String> element = new SimpleElement<String>();
        while (true) {
            // if this is a well formed XML doc, then we will exit cleanly when we see
            // END_ELEMENT. If this isn't, we will end up calling nextEvent() when there isn't one
            // which should throw an exception

            XMLUtils.consumeWhitespace(xmlEventReader);
            event = xmlEventReader.peek();
            if (event.isStartElement()) { // we have a field
                element = XMLUtils.readFieldValue(xmlEventReader, element);
                // set the field values into the document
                if ("docID".equalsIgnoreCase(element.getName())) {
                    doc.setName(element.getValue());
                } else if ("docDate".equalsIgnoreCase(element.getName())) {
                    doc.setDate(element.getValue());
                } else if ("docSource".equalsIgnoreCase(element.getName())) {
                    doc.setSource(element.getValue());
                } else if ("docText".equalsIgnoreCase(element.getName())) {
                    doc.setText(element.getValue());
                } else if ("docSeen".equalsIgnoreCase(element.getName())){
                    doc.setSeen(Boolean.parseBoolean(element.getValue()));
                }else{
                    doc.addEntity(element.getName(), element.getValue());
                }

            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if ("document".equalsIgnoreCase(endElement.getName().toString())) {
                    // end of the document, consume the event and return
                    xmlEventReader.nextEvent();
                    
                    return doc;
                }
            } else {
                throw new XMLStreamException("Unexpected XML event encountered " + event);
            }

        } // end of loop
    }

    /**
     * This method reads alias descriptions which consist of an alias_primary_id and an arbitrary
     * number of alias_other_id fields. These aliases are reported directly to the EntityManager to sort out
     *
     * @param xmlEventReader an open XMLEventReader that has just read the start of an alias
     * @throws XMLStreamException
     */
    public static void parseAliases(XMLEventReader xmlEventReader) throws XMLStreamException {
        XMLEvent event;
        AWEntity entity;
        AWEntity primary = null;
        ArrayList<AWEntity> aliases = new ArrayList<AWEntity>(5);
        SimpleElement<String> element = new SimpleElement<String>();

        while (true) {
            event = xmlEventReader.nextEvent();

            if (event.isStartElement()) { // read a field from the document
                StartElement startElement = event.asStartElement();
                String field = startElement.getName().toString();

                XMLUtils.consumeWhitespace(xmlEventReader);
                
                // the next element should be an actual entity

                element = XMLUtils.readFieldValue(xmlEventReader, element);

                entity = EntityManager.getInstance().getEntity(element.getName(), element.getValue());

                XMLUtils.consumeWhitespace(xmlEventReader);
                event = xmlEventReader.nextEvent();
                

                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (field.equals(endElement.getName().toString())) {

                        if ("alias_primary_id".equals(field)) {
                            primary = entity;
                        } else if ("alias_other_id".equals(field)) {
                            aliases.add(entity);
                        } else {
                            throw new XMLStreamException("Unexpected field encounted in alias: " + field);
                        }
                    } else {
                        // this end doesn't correspond to the start
                        throw new XMLStreamException("Encountered end of " + endElement.getName().toString()
                                + " while expecting end of " + field);
                    }
                } else {
                    throw new XMLStreamException("Unexpected XML event: " + event);
                }

            }

            // we have a spare end element, is this the end of the alias?
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if ("alias".equalsIgnoreCase(endElement.getName().toString())) {
                    // tell the EntityManager about the aliases
                    for (AWEntity alias : aliases) {
                        primary.addAlias(alias);
                        //EntityManager.getInstance().addAlias(primary, alias);
                    }
                    return;
                }
            }
        }
    }

    public static Vector<AWDocument> parseJigFile(File jigFile) {
        InputStream input = null;
        Vector<AWDocument> docs = new Vector<AWDocument>(50);
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            input = new FileInputStream(jigFile);
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(input);

            while (xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();


                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();

                    if ("document".equalsIgnoreCase(startElement.getName().toString())) {
                        AWDocument doc = parseDocDescription(xmlEventReader);
                        docs.add(doc);

                    }
                    if ("alias".equalsIgnoreCase(startElement.getName().toString())) {
                        parseAliases(xmlEventReader);

                    }
                }


            }

            xmlEventReader.close();
            System.out.println("Read " + docs.size() + " documents");

        } catch (XMLStreamException ex) {
            System.out.println("XML exception");
            Logger.getLogger(JigParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JigParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(JigParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return docs;

    }
}
