

package edu.vt.workspace.components.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * This class provides a mechanism for sorting the contents of a list without having to touch the
 * list data used to build it.
 *
 * Much of this class is derived from the SortedListModel found here:
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/sorted_jlist/
 *
 * @author cpa
 */
public class SortedListModel extends AbstractListModel {
    private ListModel _unsortedModel;
    private ArrayList<SortedEntity> _sortedIndices;
    private Comparator _comparator;
    private SortOrder _sortOrder;

    public SortedListModel(ListModel model, Comparator comparator, SortOrder sortOrder) {
        _unsortedModel = model;
        _comparator = comparator;
        _sortOrder =sortOrder;
        _sortedIndices = new ArrayList<SortedEntity>(model.getSize());

        for (int i = 0; i < _unsortedModel.getSize(); i++){
            SortedEntity entity = new SortedEntity(i);
            _sortedIndices.add(entity);
        }

        sort();
    }

    public SortedListModel(ListModel model, Comparator comparator) {
        this(model, comparator, SortOrder.ASCENDING);
    }

    public SortedListModel(ListModel model) {
        this(model, null, SortOrder.UNORDERED);
    }


    public int getSize() {
        return _unsortedModel.getSize();
    }

    public Object getElementAt(int i) {
        return _unsortedModel.getElementAt(_sortedIndices.get(i).getIndex());
    }



    public void sort(){
        if (_sortOrder == SortOrder.UNORDERED || _comparator == null)
            return;
        Collections.sort(_sortedIndices);

    }



    public enum SortOrder{
        UNORDERED,
        ASCENDING,
        DESCENDING;
    }

    private class SortedEntity implements Comparable{
        private int _index;

        public SortedEntity(int index){
            _index = index;
        }

        public int getIndex() {
            return _index;
        }

        public void setIndex(int index) {
            _index = index;
        }


        public int compareTo(Object t) {
            Object thisObj = _unsortedModel.getElementAt(_index);
            Object thatObj = _unsortedModel.getElementAt(((SortedEntity)t).getIndex());

            int comparison = _comparator.compare(thisObj, thatObj);
            if (_sortOrder == SortOrder.DESCENDING){
                comparison = -comparison;
            }
            return comparison;

        }

    }

}
