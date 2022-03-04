package edu.vt.workspace.components;

import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.FileManager;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author Christopher Andrews
 * 
 */
public class AWArbFileList extends AWFileList implements AWSavable{
    private Vector<String> _docRefs;
    private String _descr;
    private int _listID;



    public AWArbFileList(){
        super("", null, null);
        _docRefs = new Vector<String>(15);
        _documents = new Vector<AWDocument>(15);
    }


    public AWArbFileList(Vector<AWDocument> docs, String title, String description, AWInternalFrame source, Queriable controller){
        super(title, source, controller);
        setLabel(description, null);
        _documents = docs;
        _descr = description;  
    }
    
    public void addDocument(AWDocument document){
        _documents.add(document);
    }

    public void setDocref(String docID) {
        _docRefs.add(docID);
    }
    
    public Collection<AWDocument> getDocuments(){
        return _documents;
    }

    public void setDescription(String descr){
        _descr = descr;
        setLabel(_descr, null);
    }

    public String getDescription(){
        return _descr;
    }

    public int getListID() {
        return _listID;
    }

    public void setListID(int listID) {
        _listID = listID;
    }
    
    
    
    
    
    @Override
    protected boolean loadList() {
        if (_docRefs != null && ! _docRefs.isEmpty()){ // restoring from save, need to look up the docs first
            _documents = new Vector<AWDocument>(_docRefs.size());
            for(String docName: _docRefs){
                AWDocument doc = FileManager.getInstance().getDocument(docName);
                if (doc != null)
                    _documents.add(doc);
            }
            _docRefs = null;
        }
        
        _list.setListData((Vector<AWDocument>)_documents);
        return true;
    }

    @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);
        writer.write("description", _descr);
        for (AWDocument doc : _documents) {
            writer.write("docref", doc.getId());
        }
    }




}
