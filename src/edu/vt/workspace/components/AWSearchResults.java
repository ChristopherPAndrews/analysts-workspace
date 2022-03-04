package edu.vt.workspace.components;

import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import java.util.Vector;

/**
 *
 * @author cpa
 */
public class AWSearchResults extends AWFileList implements AWSavable{
    private String _query = null;
    private AWInternalFrame _source;
    
    /**
     * This is an empty constructor to be used by the save mechanism.
     */
    public AWSearchResults() {
        super("",null, null);
    }
        
    
    /**
     * Creates a new <code>AWSearchResults</code> instance based on the results of a
     * textual query.
     *
     * @param query the search query that created this result collection
     * @param source the object that triggered the search
     * @param controller  the master class that this can make requests to
     */
    public AWSearchResults(String query, AWInternalFrame source, Queriable controller) {
        super("Search Results", source, controller);
        _query = query;
        _source = source;
        setLabel(_query, null);
        setHighlightTerm(_query);
    }

    
    
    /**
     * Set the query
     *
     * This is designed to be used from the save mechanism - it is possible that
     * it could be used for editing the query as well.
     * @param query the String to use as a query
     */
    public void setQuery(String query){
        _query = query;
        setLabel(_query, null);
        setHighlightTerm(_query);

    }
    
    
    /**
     * Get the query that was used to generate this result set
     * @return the query string
     */
    public String getQuery(){
        return _query;
    }

    public AWInternalFrame getSource() {
        return _source;
    }

    public void setSource(AWInternalFrame source) {
        source = _source;
    }

    
    @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);
        writer.write("query", _query);
    }

    @Override
    protected boolean loadList() {
        if (_query != null){
            _documents = _controller.performSearch(_query);
            _list.setListData((Vector<AWDocument>)_documents);
            return true;
        }else
            return false;
    }
}
