package edu.vt.workspace.data;

import com.joestelmach.natty.ParseResult;
import com.joestelmach.natty.Parser;
import edu.vt.workspace.components.utilities.HighlightEvent;
import edu.vt.workspace.components.utilities.HighlightListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class encapsulates a text document with entities.
 *
 * Note: while documents are concerned with the comings and goings of entities,
 * it is not an EntityChangeListener. This is primarily because most entity changes
 * are not relevant to most documents, and there is enough information to notify
 * specific documents of changes that apply to them.
 * 
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
public class AWDocument implements Comparable, AWSavable, PropertyHolder {

    private int _id;
    private String _name = "";
    private Date _date;
    private String _source = "";
    private String _title = "";
    private String _text;
    private String _type = "text";
    private Set<AWEntity> _entities = new HashSet<AWEntity>(15);
    private File _file;
    private Boolean _seen = false;// has this document been looked at
    private ArrayList<Range> _highlights = new ArrayList<Range>(10);
    private ArrayList<HighlightListener> _highlightListeners = new ArrayList<HighlightListener>(5);
    private HashMap<Range, AWEntity> _entityRanges = new HashMap<Range, AWEntity>(15);
    private Hashtable<String, String> _properties = new Hashtable<String, String>(); // this contains extra fields that might have been included in input files

    /**
     * Create an empty AWDocument object.
     *
     * The only real reason to do this is to fill this in later during a read operation.
     */
    public AWDocument() {
    }

    public AWDocument(File f) {
        BufferedReader fileBuffer = null;
        String line = null;
        StringBuilder contents = new StringBuilder();
        try {
            _file = f;

            //setId(f.getCanonicalPath());
            setName(f.getCanonicalPath());
            fileBuffer = new BufferedReader(new FileReader(f));

            fileBuffer.mark(1000);

            do {
                _title = fileBuffer.readLine();
            } while (_title.length() == 0);


            if (_title.startsWith("IMAGEREF:")) {
                _type = "image";
                // set file to the actual image file
                _file = new File(f.getParent(), _title.substring(10));
                // now find the real title
                do {
                    _title = fileBuffer.readLine();
                } while (_title.length() == 0);
            }

            if (_title.length() > 50) {
                _title = _title.substring(0, 50);
            }


            try {
                fileBuffer.reset();
            } catch (IOException ioe) {
                fileBuffer.close();
                fileBuffer = new BufferedReader(new FileReader(f));
            }



            // read the file into the contents field
            while ((line = fileBuffer.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }

            _text = contents.toString();



        } catch (IOException ex) {
            Logger.getLogger(AWDocument.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                fileBuffer.close();
            } catch (IOException ex) {
                Logger.getLogger(AWDocument.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public Date getDate() {
        return _date;
    }

    public void setDate(Date _date) {
        this._date = _date;
    }

    public void setDate(String dateString) {
        if (!dateString.isEmpty()){
            Parser dateParser = new Parser();
            ParseResult result = dateParser.parse(dateString);
            List<Date> dates = result.getDates();
            _date = dates.get(0);
        }
    }

    public void setType(String type) {
        _type = type;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getSource() {
        return _source;
    }

    public void setSource(String _source) {
        this._source = _source;
    }

    public String getText() {
        return _text;
    }

    public void setText(String _text) {
        this._text = _text;
        if (_title.equals("")) {
            if (_text.indexOf("\n") >= 0 && _text.indexOf("\n") < 55) {
                _title = _text.substring(0, _text.indexOf("\n"));
            } else {
                _title = _text.substring(0, 55) + "...";
            }
        }
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String _title) {
        this._title = _title;
    }
    
     public void setProperty(String key, String value){
        _properties.put(key, value);
    }
    
    public String getProperty(String key){
        return _properties.get(key);
    }
    
    public boolean hasProperty(String key){
        return _properties.containsKey(key);
    }
    

    public Boolean getSeen() {
        return _seen;
    }

    public Set<AWEntity> getEntities() {
        return _entities;
    }

    public Map<Range, AWEntity> getEntityRanges() {
        return _entityRanges;
    }

    public void setSeen(Boolean _seen) {
        this._seen = _seen;
    }

    public String getType() {
        return _type;
    }

    private void findEntityOccurance(String value, AWEntity entity) {
        int index = _text.indexOf(value);
        while (index != -1) {
            Range newRange = new Range(index, index + value.length());
            _entityRanges.put(newRange, entity);
            for (HighlightListener listener : this.getHighlightListeners()) {
                listener.addHighlight(new HighlightEvent(this, newRange, entity));
            }
            index = _text.indexOf(value, index + 1);
        }
    }

    public void addEntity(String type, String value) {
        AWEntity entity = EntityManager.getInstance().getEntity(type, value);
        addEntity(entity);
    }

    public void addEntity(AWEntity entity) {
        entity.addDoc(this);
        _entities.add(entity);
        findEntityOccurance(entity.getValue(), entity);
        for (String alias : entity.getAliases()) {
            findEntityOccurance(alias, entity);
        }
    }

    /**
     * This is a specialized form for adding an entity that is not based on 
     * textual analysis.
     * @param entity the {@code AWEntity} to be added
     * @param range the {@code range} over which the entity appears
     */
    public void addEntity(AWEntity entity, Range range) {
        entity.addDoc(this);
        
        _entities.add(entity);
        _entityRanges.put(range, entity);
        for (HighlightListener listener : this.getHighlightListeners()) {
            listener.addHighlight(new HighlightEvent(this, range, entity));
        }
    }

    public void removeEntity(AWEntity entity) {
        _entities.remove(entity);
        for (Iterator<Range> i = _entityRanges.keySet().iterator(); i.hasNext();) {
            Range r = i.next();
            if (_entityRanges.get(r).equals(entity)) {
                i.remove();
                for (HighlightListener listener : this.getHighlightListeners()) {
                    listener.removeHighlight(new HighlightEvent(this, r, entity));
                }
            }
        }
    }

    public void removeEntityFromRange(Range range) {
        AWEntity entity = _entityRanges.remove(range);

        for (HighlightListener listener : this.getHighlightListeners()) {
            listener.removeHighlight(new HighlightEvent(this, range, entity));
        }
        if (entity != null && !_entityRanges.containsValue(entity)) {
            _entities.remove(entity);
            entity.removeDocument(this);
        }
    }

    public void replaceEntity(AWEntity oldEntity, AWEntity newEntity) {
        _entities.remove(oldEntity);
        _entities.add(newEntity);
        for (Range r : _entityRanges.keySet()) {
            if (_entityRanges.get(r).equals(oldEntity)) {
                _entityRanges.put(r, newEntity);
            }
        }

    }

    public void entityChanged(AWEntity entity) {
        for (Range r : _entityRanges.keySet()) {
            if (_entityRanges.get(r).equals(entity)) {
                for (HighlightListener listener : this.getHighlightListeners()) {
                    listener.updateHighlight(new HighlightEvent(this, r, entity));
                }
            }
        }
    }

    public AWEntity getEntity(Range range) {
        return _entityRanges.get(range);
    }

    public File getFile() {
        return _file;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("AWSDocument- ");
        str.append("title : ").append(_title);
        str.append("id : ").append(_id);
        return str.toString();
    }

    public int compareTo(Object o) {
        if (o instanceof AWDocument) {
            return _title.compareToIgnoreCase(((AWDocument) o).getTitle());
        } else {
            return 0;
        }
    }

    /**
     * Add a h for the specified Range.
     * @param h a Range object defining the h
     */
    public void setHighlight(Range h) {
        _highlights.add(h);
        for (HighlightListener listener : this.getHighlightListeners()) {
            listener.addHighlight(new HighlightEvent(this, h));
        }
    }

    /**
     * Add a h based on a start and end position. In essence, this creates a Range and passes it
     * to the other version of this method.
     * @param start position marking the start of the h
     * @param end position marking the end of the h
     */
    public void setHighlight(int start, int end) {
        setHighlight(new Range(start, end));
    }

    /**
     * Remove the h described by the start and end positions. This requires an exact match.
     * The expectation is that this should be used in conjunction with something that already knows
     * appropriate ranges.
     * @param highlight
     */
    public void removeHighlight(Range highlight) {
        for (Range h : _highlights) {
            if (h.equals(highlight)) {
                _highlights.remove(h);
                break;
            }
        }
        for (HighlightListener listener : this.getHighlightListeners()) {
            listener.removeHighlight(new HighlightEvent(this, highlight));
        }
    }

    /**
     * Fetch a list of the highlights contained in this document. This currently
     * returns the internal list, so it shouldn't be modified by the caller.
     *
     * @return an ArrayList<Range> of h Ranges.
     */
    public ArrayList<Range> getHighlights() {
        return _highlights;
    }

    /**
     * Get the list of HighlightListeners listening to this document.
     *
     * @return the list of listeners
     */
    public Iterable<HighlightListener> getHighlightListeners() {
        return _highlightListeners;
    }

    public void addHighlightListener(HighlightListener hl) {
        _highlightListeners.add(hl);
    }

    public void removeHighlightListener(HighlightListener hl) {
        _highlightListeners.remove(hl);
    }

    /**
     * Write out the important components of this document to the writer.
     *
     * @param writer
     */
    public void writeData(AWWriter writer) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        writer.write("docID", _id);
        if (_date != null) {
            writer.write("docDate", df.format(_date));
        }
        writer.write("docSource", _source);
        writer.write("docText", _text);
        writer.write("docName", _name);
        if (!writer.isDataOnly()) {
            writer.write("docSeen", _seen);
            for (Range highlight : _highlights) {
                writer.write("highlight", highlight);
            }
        }
//        for (AWEntity entity : _entities){
//            writer.write(entity.getType(), entity.getValue());
//        }

        EntityRange entityRange = new EntityRange();
        for (Range range : _entityRanges.keySet()) {
            entityRange.setRange(range);
            entityRange.setEntity(_entityRanges.get(range));
            writer.write("entityRange", entityRange);
        }

    }
}
