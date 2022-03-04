package edu.vt.workspace.search;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * This is the main interface to the lucene search facilities.
 *
 *
 * Created: Sun Mar  1 15:31:17 2009
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class SearchEngine {
    private File _indexPath;
    private static final long serialVersionID = 2009200L;
    private transient IndexReader reader;
    private transient IndexSearcher searcher;
    private transient StandardAnalyzer analyzer;
    private transient QueryParser parser;


    /**
     * Creates a new <code>SearchEngine</code> instance using a default index location.
     * @throws FileNotFoundException  thrown when index can not be found
     */
   public SearchEngine() throws FileNotFoundException{
        this("index");
    }

    /**
     * Creates a new <code>SearchEngine</code> instance.
     *
     * @param indexPath a directory path to the Lucene index directory
     * @throws FileNotFoundException thrown when index can not be found
     */
    public SearchEngine(String indexPath) throws FileNotFoundException {
        _indexPath= new File(indexPath);
        if (_indexPath.exists()) {

            loadIndex();
        } else {
            throw new FileNotFoundException("Search index '" + _indexPath + "' - index can not be located.");
        }
    }

 
    
    
    
    /**
     *  This function initializes the search engine by loading the index and setting up the various search tools
     * and analyzers.
     */
    private void loadIndex() throws FileNotFoundException {
        try {
            reader = IndexReader.open(_indexPath);
            searcher = new IndexSearcher(reader);
        } catch (CorruptIndexException cie) {
            throw new FileNotFoundException("Search index '" + _indexPath + "' - index is corrupt or can not be located.");
        } catch (IOException ioe) {
            throw new FileNotFoundException("Search index '" + _indexPath + "' - index is corrupt or can not be located.");
        }
        analyzer = new StandardAnalyzer();
        parser = new QueryParser("contents", analyzer);
    }


    /**
     * This function should only ever be called by the object serializer - it handles reading the object back in
     * from the save file and reinitializing everything.
     * @param in an input stream attached to a save file
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    // don't need to save anything other than the index path, so this reattaches to the index
        in.defaultReadObject();
        loadIndex();
    }

    /**
     * This function is the main search function. It takes in a query and returns a list of files that match the results.
     * The query should match the Lucene search query syntax.
     *
     * @param query A search query
     * @return An ArrayList of Strings holding the ids of the found documents
     */
    public ArrayList<String> search(String query) {
        Query parsedQuery;
        Document doc;
        String id;
        TopDocCollector collector;
        ScoreDoc[] hits;
        int numTotalHits;
        int fetchCount = 50;
        ArrayList<String> results = new ArrayList<String>(fetchCount);

        try {
            parsedQuery = parser.parse(query);

        } catch (ParseException pe) {
            System.out.println("Unable to parse query - no results " + pe.getMessage());
            return results;
        }

        collector = new TopDocCollector(fetchCount); // start with fifty hits
        try {
            searcher.search(parsedQuery, collector);
            hits = collector.topDocs().scoreDocs;
            numTotalHits = collector.getTotalHits();
            if (fetchCount < numTotalHits) {
                collector = new TopDocCollector(numTotalHits);
                searcher.search(parsedQuery, collector);
                hits = collector.topDocs().scoreDocs;
            }

            for (ScoreDoc hit : hits) {
                doc = searcher.doc(hit.doc);
                id = doc.get("path");
                if (id != null) {
                    results.add(id);
                }
            }

        } catch (IOException ioe) {
            System.out.println("Error performing search - no results");
        }

        return results;
    }
}
