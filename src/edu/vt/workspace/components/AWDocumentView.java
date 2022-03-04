package edu.vt.workspace.components;

import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.LinkTarget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is an abstract stand-in for all document frames, allowing us
 * to refer to both text and image documents.
 * @author cpa
 */
public abstract class AWDocumentView extends AWInternalFrame implements LinkTarget{
    ArrayList<AWDocument> _documentList = new ArrayList<AWDocument>(1);
    
    public AWDocumentView(AWDocument doc, Queriable controller) {
        super(doc.getTitle(), controller);
        _documentList.add(doc);
    }
    
    public Collection<AWDocument> getTargetDocuments(){
        return _documentList;
    }
    
    public Collection<AWEntity> getTargetEntities(){
        return _doc.getEntities();
    }
    
    public LinkTarget getLinkSource(){
        return null;
    }
    
    public void addAdjustmentListener(AdjustmentListener listener){
        
    }
    
     
     public ArrayList<Rectangle> getEntityLocations(AWEntity entity){
        return null;
    }
    
    
     public ArrayList<Rectangle> getTermLocations(String term){
        return null;
    }
    

}
