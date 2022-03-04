package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.AWEntity;

/**
 * This is a simple helper class that wraps {@code AWEntity} objects for sorting.
 * 
 * @author cpa
 */
public class SortableListEntity implements Comparable {
    private AWEntity _entity;

    public SortableListEntity(AWEntity entity) {
        _entity = entity;
    }

    public int compareTo(Object o) {
        if (o instanceof SortableListEntity) {
            return _entity.compareTo(((SortableListEntity) o).getEntity());
        }
        return 0;
    }

    public AWEntity getEntity() {
        return _entity;
    }
}
