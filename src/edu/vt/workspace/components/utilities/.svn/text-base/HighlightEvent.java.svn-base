package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.Range;

/**
 * This is a simple event class to broadcast changes in document highlighting.
 *
 * @author cpa
 */
public class HighlightEvent {
    private AWDocument _doc;
    private AWEntity _entity;
    private int _start;
    private int _end;

    /**
     * Create a new highlight event.
     *
     * @param doc the document that is firing the event
     * @param range the Range of the highlight
     */
    public HighlightEvent(AWDocument doc, Range range) {
        _doc = doc;
        _entity = null;
        _start = range.getStart();
        _end = range.getEnd();
    }

    /**
     * Create a new highlight event with associated entity
     * @param doc
     * @param range
     * @param entity
     */
    public HighlightEvent(AWDocument doc, Range range, AWEntity entity) {
        _doc = doc;
        _entity = entity;
        _start = range.getStart();
        _end = range.getEnd();
    }



    public int getStart() {
        return _start;
    }

    public int getEnd() {
        return _end;
    }

    public AWDocument getDocument() {
        return _doc;
    }
    
    public AWEntity getEntity(){
        return _entity;
    }
}
