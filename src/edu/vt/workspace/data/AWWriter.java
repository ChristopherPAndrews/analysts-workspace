

package edu.vt.workspace.data;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class is the tool for writing out save data for the workspace. It handles the
 * basic marshalling into XML. The basic principle is to provide a number fo write()
 * methods that create XML entities for basic types. In each case, a tag can be added,
 * so the data is wrapped in a named entity. At the moment, the responsibility for
 * the names of the elements and the ordering is spread across the application. The only guarantee
 * made by this class is that the top level entity is called "workspace".
 *
 *
 * @author cpa@cs.vt.edu
 */
public final class AWWriter {
    private XMLStreamWriter writer;
    private boolean _dataOnly = false;

    public AWWriter(File outFile){
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = factory.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            startElement("workspace"); // open the top level element
        } catch (IOException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isDataOnly() {
        return _dataOnly;
    }

    public void setDataOnly(boolean _dataOnly) {
        this._dataOnly = _dataOnly;
    }

    


    public void close(){
        endElement();// close the workspace element
        try {
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void write(String name, String text){
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", text.getClass().getName());
            writer.writeCharacters(text);
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void write(String name, Integer value){
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", value.getClass().getName());
            writer.writeCharacters(String.valueOf(value));
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void write(String name, Double value) {
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", value.getClass().getName());
            writer.writeCharacters(String.valueOf(value));
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void write(String name, Boolean value) {
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", value.getClass().getName());
            writer.writeCharacters(String.valueOf(value));
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(String name, Rectangle rect){
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", rect.getClass().getName());
             writer.writeCharacters("\n");
            write("x", Double.valueOf(rect.getX()).intValue());
            write("y", Double.valueOf(rect.getY()).intValue());
            write("width", Double.valueOf(rect.getWidth()).intValue());
            write("height", Double.valueOf(rect.getHeight()).intValue());
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public void write(String name, AWSavable obj){
        try {
            writer.writeStartElement(name);
            writer.writeAttribute("type", obj.getClass().getName());
            writer.writeCharacters("\n");
            obj.writeData(this);
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method enters an XML tag of the appropriate type. In some respects, this is a hack
     * specific to XML, and thus unsatisfactory. I'll have to consider other approaches to this later.
     * The higher level functions shouldn't have to know about XML structure. Mostly, this is a
     * concession to my desire to be compatible with the jigsaw file format.
     * @param name
     */
    public void startElement(String name){
        try {
            writer.writeStartElement(name);
            writer.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void endElement(){
        try {
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(AWWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
    

}
