package edu.vt.workspace.data;

import edu.vt.workspace.components.AWInternalFrame;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * This class is responsible for managing all of the files in the application.
 * It should have a list of all known files.
 *
 *
 * @author cpa
 */
public class FileManager implements AWSavable {

    private HashMap<String, AWDocument> _documentCache = new HashMap<String, AWDocument>(50);
    private LinkedList<AWInternalFrame> _openDocs = new LinkedList<AWInternalFrame>();
    private transient Helper _helper;
    private static FileManager _instance = new FileManager();

    private FileManager() {
        _helper = new Helper();
    }

    public static FileManager getInstance() {
        return _instance;
    }

    public static void reset() {
        _instance = new FileManager();
    }

    public void addDocuments(File file) {
        String[] suffixes = ImageIO.getReaderFormatNames();
        String extension;
        boolean isImage = false;
        if (file.canRead()&& !file.isHidden()) {
            if (file.isDirectory()) { // this is a directory, recurse
                for (String f : file.list()) {
                    addDocuments(new File(file, f));
                }

            } else { // this is an indidvidual file
                // jump over images
                extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                isImage = false;
                for (String suffix : suffixes) {
                    if (suffix.equalsIgnoreCase(extension)) {
                        isImage = true;
                        break;
                    }
                }
                if (!isImage) {
                    // add the document
                    addDocument(new AWDocument(file));
                }
            }
        }
    }

    /**
     * Add a list of documents into the collection.
     * 
     * @param newDocs a list of documents to add
     */
    public void addDocuments(List<AWDocument> newDocs) {
        for (AWDocument doc : newDocs) {
            addDocument(doc);
        }
    }

    /**
     * Add a single document into the collection.
     *
     * @param doc the document to add
     */
    public void addDocument(AWDocument doc) {
        if (doc.getId() <= 0){
            AWDataManager.getInstance().addDocument(doc);
        }
        _documentCache.put(doc.getName(), doc);
        
    }

    public void addFrame(AWInternalFrame frame) {
       // AWDataManager.getInstance().addFrame(frame);
        _openDocs.add(frame);
        frame.addInternalFrameListener(_helper);
    }

    /**
     * Get the AWDocument object associated with the given unique name. The names can
     * double as paths, so if the name cannot be resolved internally, this checks to see
     * if the file exists in the file system. If it does, this creates a new AWDocument
     * object containing the contents of the file.
     * 
     * @param name the unique name that identifies a particular document
     * @return the AWDocument object that contains the desired document
     */
    public AWDocument getDocument(String name) {
        System.out.println("looking for: " + name);
        AWDocument doc = _documentCache.get(name);
        if (doc == null) {
            // we don't have a record of this document - possibly this is a path
        System.out.println("couldn't find: " + name);
            File f = new File(name);
            if (f.exists()) {
                // this was a path - create a new document and add it to our collection
                doc = new AWDocument(f);
                _documentCache.put(name, doc);
            }
        }

        return doc;
    }
    
    public Vector<AWDocument> getDocuments() {
        Vector<AWDocument> docList = new Vector<AWDocument>(_documentCache.size());
        for (AWDocument doc : _documentCache.values()) {
            docList.add(doc);
        }
        Collections.sort((List) docList);
        return docList;
    }

    public void remove(AWInternalFrame frame) {

        _openDocs.remove(frame);
    }

    public List<AWInternalFrame> getOpenDocs(AWDocument doc) {
        List<AWInternalFrame> frames = new ArrayList<AWInternalFrame>(5);
        for (AWInternalFrame frame : _openDocs) {
            if (doc.equals(frame.getDocument())) {
                frames.add(frame);
            }
        }

        return frames;
    }
    
    public void writeData(AWWriter writer) {
        for (AWDocument doc : _documentCache.values()) {
            writer.write("document", doc);
        }
    }


    private class Helper extends InternalFrameAdapter {

        @Override
        public void internalFrameClosing(InternalFrameEvent ie) {
            remove((AWInternalFrame) ie.getInternalFrame());
        }
    }
}
