package edu.vt.workspace.components;

import edu.vt.workspace.data.AWDocument;
import java.util.Vector;

/**
 * Describe interface Queriable here.
 *
 *
 * Created: Mon Mar  9 14:51:50 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public interface Queriable {


    public Vector<AWDocument> performSearch(String query);


    
}
