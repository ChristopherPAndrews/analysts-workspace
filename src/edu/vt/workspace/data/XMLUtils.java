package edu.vt.workspace.data;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This class just contains a collection of utilities for working with XML streams.
 * All methods are static and meant to be used independently.
 *
 * @author cpa@cs.vt.edu
 */
public class XMLUtils {

    /**
     * This is a simple helper method to strip whitespace out of the middle of the XML document.
     * @param xmlEventReader an open XMLEventReader that is between tags
     * @throws XMLStreamException
     */
    public static void consumeWhitespace(XMLEventReader xmlEventReader) throws XMLStreamException {

        while (xmlEventReader.peek().isCharacters() && xmlEventReader.peek().asCharacters().isWhiteSpace()) {
            // consume any whitespace
            xmlEventReader.nextEvent();
        }
    }

    /**
     * This is a helper method to read out character data. Since the character
     * data can be broken up by the parser, this reads events until it runs out of character data.
     *
     * @param xmlEventReader an open reader with character data as the next event
     * @return a String containing all of the character data
     * @throws XMLStreamException
     */
    public static String readCharacterData(XMLEventReader xmlEventReader) throws XMLStreamException {
        StringBuilder data = new StringBuilder();
        XMLEvent event;

        while (xmlEventReader.peek().isCharacters()) {
            // it is a character event, so pop it off the event queue
            event = xmlEventReader.nextEvent();

            // add the character data to our buffer
            data.append(event.asCharacters().getData());
        }

        return data.toString().trim();
    }

    /**
     * This method reads a single field out of the XML stream. The field is expected to be a tag
     * and a textual value. The result is written into a SimpleElement object.
     *
     * @param xmlEventReader  an open reader with a start event as the next event
     * @param element a SimpleElement object to reuse
     * @return the SimpleElement object with new values set or null if this was not at a proper simple element in the XML stream
     * @throws XMLStreamException
     */
    public static SimpleElement readFieldValue(XMLEventReader xmlEventReader, SimpleElement element) throws XMLStreamException {
        XMLEvent event;

        if (element != null)
            element.clear();
        else
            element = new SimpleElement();

        event = xmlEventReader.nextEvent();

        if (event.isStartElement()) { // read a field from the document
            StartElement startElement = event.asStartElement();
            element.setName(startElement.getName().toString());


            // next element should be characters or the end of element
            element.setValue(readCharacterData(xmlEventReader));

            // all chracter data should be consumed, so the next event should be the end of the field
            event = xmlEventReader.nextEvent();

            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (element.getName().equals(endElement.getName().toString())) {
                    // we are good
                    return element;
                } else {
                    // unmatched end
                    throw new XMLStreamException("Encountered end of " + endElement.getName().toString()
                            + " while expecting end of " + element.getName());
                }
            } else { // wrong element
                throw new XMLStreamException("Unexpected XML event: " + event);
            }
            
        }
        // we aren't at a start element - return null
        return null;
    }


    public static Object readObject(XMLEventReader xmlEventReader) throws XMLStreamException{
        XMLEvent event;
        Object element = null;
        String className;
        String elementName;
        String data;
        Class<?> c;

        consumeWhitespace(xmlEventReader);
        event = xmlEventReader.nextEvent();
        
        if (event.isStartElement()) { // read a field from the document
            StartElement startElement = event.asStartElement();
            elementName = startElement.getName().toString();
            className = startElement.getAttributeByName(new QName("type")).getValue();
            try {
                c = Class.forName(className);
            
                consumeWhitespace(xmlEventReader);
                if (xmlEventReader.peek().isCharacters()) { // this is a single value element
                   
                    Constructor<?> con = c.getConstructor(new Class[]{String.class});
                    data = readCharacterData(xmlEventReader);
                    element = con.newInstance(new Object[]{data});
                    

                } else { // this is a compound type - initialize empty and add properties
           
                    Constructor<?> con = c.getConstructor(new Class[]{});

                    element = con.newInstance(new Object[]{});


                    event = xmlEventReader.peek();
                    while (event.isStartElement()) { // read a field from the document
                        StartElement startElement2 = event.asStartElement();
                        String propName = startElement2.getName().getLocalPart();
                        
                        String typeName = startElement2.getAttributeByName(new QName("type")).getValue();
                        Class<?> type = Class.forName(typeName);
                        Object value = readObject(xmlEventReader);
                        BeanInfo beanInfo = Introspector.getBeanInfo(c);
                        boolean set = false;
                        // first we try to set by finding an appropriate setter
                        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

                        for (PropertyDescriptor descriptor : propertyDescriptors) {
                            
                            if (descriptor.getName().equalsIgnoreCase(propName) ){
                                Method m = descriptor.getWriteMethod();
                                if (m != null){
                                    m.invoke(element, value);
                                    set = true;
                                    break;
                                }
                            }
                        }
                        if (! set){
                            try {
                                // if we couldn't find a property, we will try to set a field directly
                                Field field = c.getDeclaredField(propName);
                                field.set(element, value);
                            } catch (NoSuchFieldException ex) {
                                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }



                        consumeWhitespace(xmlEventReader);
                        event = xmlEventReader.peek();

                    } 
                }
            } catch (IntrospectionException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            //element.setName(startElement.getName().toString());
           

            // next element should be characters or the end of element
            data = readCharacterData(xmlEventReader);
            
          

            // all chracter data should be consumed, so the next event should be the end of the element
            event = xmlEventReader.nextEvent();

            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (elementName.equals(endElement.getName().toString())) {
                    // we are good
                    return element;
                } else {
                    // unmatched end
                    throw new XMLStreamException("Encountered end of " + endElement.getName().toString()
                            + " while expecting end of " + elementName);
                }
            } else { // wrong element
                throw new XMLStreamException("Unexpected XML event: " + event);
            }

        }



        return element;
    }




}
