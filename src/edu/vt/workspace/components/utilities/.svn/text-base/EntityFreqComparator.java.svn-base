package edu.vt.workspace.components.utilities;

import java.util.Comparator;

/**
 * This is a simple class that allows the {@code SortableListEntity} objects to be sorted by frequency.
 * 
 * @author cpa
 */
public class EntityFreqComparator implements Comparator<SortableListEntity> {

    public int compare(SortableListEntity o1, SortableListEntity o2) {
        int returnValue = o1.getEntity().numDocs() < o2.getEntity().numDocs() ? 1 : (o1.getEntity().numDocs() == o2.getEntity().numDocs() ? 0 : -1);
        if (returnValue == 0) {
            return o1.compareTo(o2);
        }
        return returnValue;
    }
}
