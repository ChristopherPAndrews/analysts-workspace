package edu.vt.workspace.data;

import java.awt.event.AdjustmentListener;
import java.util.Collection;

/**
 * This interface is implemented by all valid link targets. It provides a consistent
 * interface for querying if the target responds to a particular target.
 * @todo get individual selections working again
 * @todo get entity link selection working properly
 * 
 *@author Christopher Andrews [cpa@cs.vt.edu]
 */
public interface LinkTarget {
    public Collection<AWDocument> getTargetDocuments();
    
    public Collection<AWEntity> getTargetEntities();
    
    public String getTitle();
    
    public LinkTarget getLinkSource();
    
    public void addAdjustmentListener(AdjustmentListener listener);
}
