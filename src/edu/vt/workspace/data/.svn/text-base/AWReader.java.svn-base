package edu.vt.workspace.data;

import edu.vt.workspace.components.AWInternalFrame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This class parses AWX save files. Unlike AWSWriter, which is more of a collection of utilities,
 * this class has to be more aware of the contents and handle them appropriately.
 *
 * @todo I should probably rewrite this to be class oriented since this is really a single atomic operation...
 * 
 * @author cpa@cs.vt.edu
 */
public final class AWReader {
    private XMLEventReader xmlEventReader;
    private InputStream input = null;
    private ArrayList<AWInternalFrame> frames = new ArrayList<AWInternalFrame>(100);
    
    public AWReader(File file) throws FileNotFoundException{
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            input = new FileInputStream(file);
            xmlEventReader = inputFactory.createXMLEventReader(input);
            parseFile();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWReader.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(AWReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }



    private void parseFile() throws XMLStreamException{


        while (xmlEventReader.hasNext()){
            XMLEvent event = xmlEventReader.peek();
            
            if (event.isStartElement()){
                StartElement startElement = event.asStartElement();
                if ("entity".equalsIgnoreCase(startElement.getName().toString())){
                    // handle entity element
                    xmlEventReader.nextEvent();
                    parseEntity();
                } else if ("document".equalsIgnoreCase(startElement.getName().toString())){
                    // handle document element
                    // documents are setup to have a compatible format to jig files, so we just reuse the method
                    xmlEventReader.nextEvent();
                    AWDocument doc = parseDocument();
                    FileManager.getInstance().addDocument(doc);
                } else if ("frame".equalsIgnoreCase(startElement.getName().toString())){
                    // handle frame element
                    Object o;
                    o = XMLUtils.readObject(xmlEventReader);
                    if (o instanceof AWInternalFrame){
                        frames.add((AWInternalFrame)o);
                    }else{
                        Logger.getLogger(AWReader.class.getName()).log(Level.SEVERE, null, "Object returned while reading frames was not a frame");
                    }

                }else{
                    event = xmlEventReader.nextEvent();
                }

            }else{
                event = xmlEventReader.nextEvent();

            }
        }
    }




    private void parseEntity() throws XMLStreamException {
        XMLEvent event;
        SimpleElement<String> element = new SimpleElement<String>();
        Boolean complete = false;
        String type = "";
        String value = "";
        int id = -1;
        List<String> aliases = new ArrayList<String>(5);
        Hashtable<String, String> metadata = new Hashtable<String, String>();

        while (! complete) {
            XMLUtils.consumeWhitespace(xmlEventReader);
            event = xmlEventReader.peek();
            if (event.isStartElement()) { // we have a field
                element = XMLUtils.readFieldValue(xmlEventReader, element);
                if ("type".equalsIgnoreCase(element.getName())){
                    type = element.getValue();
                } else if ("value".equalsIgnoreCase(element.getName())){
                    value = element.getValue();
                } else if ("id".equalsIgnoreCase(element.getName())){
                    id = Integer.parseInt(element.getValue());
                }else if ("alias".equalsIgnoreCase(element.getName())){
                    aliases.add(element.getValue());
                } else{
                    metadata.put(element.getName(), element.getValue());
                }


            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if ("entity".equalsIgnoreCase(endElement.getName().toString())) {
                    // end of the document, process the entity, consume the event and return
                    AWEntity entity = EntityManager.getInstance().getEntity(type, value);
                   // entity.setID(id);
                    for (String alias: aliases){
                        entity.addAlias(alias);
                    }
                    for (String key: metadata.keySet()){
                        entity.setProperty(key, metadata.get(key));
                    }
                    metadata.clear();
                    
                    xmlEventReader.nextEvent();
                    complete = true;
                }
            } else {
                throw new XMLStreamException("Unexpected XML event encountered " + event);
            }


        }
    }

    /**
     * This is specialized parsing code that knows the structure of documents. Like the
     * {@code parseEntity()} method, this is a break from the general conventions of the save mechanism.
     * This is due, in large part, to the fact that this evolved out of code to read JigSaw files, and
     * for a while I wanted to keep the data portion compatible. However, that is no longer the case,
     * and this could conceptually be fixed to be more in keeping with the more generic approaches used
     * elsewhere.
     * @return
     * @throws XMLStreamException
     */
    private AWDocument parseDocument() throws XMLStreamException{
        AWDocument doc = new AWDocument();
        XMLEvent event;
        SimpleElement<String> element = new SimpleElement<String>();
        Hashtable<String, String> metadata = new Hashtable<String, String>();
        while (true) {
            // if this is a well formed XML doc, then we will exit cleanly when we see
            // END_ELEMENT. If this isn't, we will end up calling nextEvent() when there isn't one
            // which should throw an exception

            XMLUtils.consumeWhitespace(xmlEventReader);
            event = xmlEventReader.peek();
            if (event.isStartElement()) { // we have a field
                
                // first, make sure that it is not a compound type
                if ("entityRange".equalsIgnoreCase(((StartElement)event).getName().toString())){
                    Object o = XMLUtils.readObject(xmlEventReader);
                    if (o instanceof EntityRange) {
                        doc.addEntity(((EntityRange) o).getEntity(), ((EntityRange) o).getRange());
                    } else {
                        throw new XMLStreamException("Incompatible object encountered for entityRange: " + o.getClass().getCanonicalName());
                    }
                    continue;
                }else if ("highlight".equalsIgnoreCase(((StartElement)event).getName().toString())){
                    Object o = XMLUtils.readObject(xmlEventReader);
                    if (o instanceof Range) {
                        doc.setHighlight((Range)o);
                    } else {
                        throw new XMLStreamException("Incompatible type encountered for highlight"+ o.getClass().getCanonicalName());
                    }
                    continue;
                }

                element = XMLUtils.readFieldValue(xmlEventReader, element);
                // set the field values into the document
                if ("docID".equalsIgnoreCase(element.getName())) {
                    doc.setId(Integer.parseInt(element.getValue()));
                } else if ("docDate".equalsIgnoreCase(element.getName())) {
                    doc.setDate(element.getValue());
                } else if ("docSource".equalsIgnoreCase(element.getName())) {
                    doc.setSource(element.getValue());
                } else if ("docText".equalsIgnoreCase(element.getName())) {
                    doc.setText(element.getValue());
                }else if ("docName".equalsIgnoreCase(element.getName())) {
                    doc.setName(element.getValue());
                } else if ("docSeen".equalsIgnoreCase(element.getName())){
                    doc.setSeen(Boolean.parseBoolean(element.getValue()));
                }else{
                    doc.setProperty(element.getName(), element.getValue());
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




    public List<AWInternalFrame> getFrames(){
        return frames;
    }
}
