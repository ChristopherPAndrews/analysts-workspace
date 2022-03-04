package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.AWEntity;

/**
 * This is a simple event class to broadcast entity changes. At the moment,
 * this handles addition and removal of entities.
 *
 * @author cpa
 */
public class EntityChangeEvent {
    public static enum ChangeType {TYPE_CHANGE, ALIAS_CHANGE, ADD_ENTITY, REMOVE_ENTITY};
    private ChangeType _type = null;
    private AWEntity _entity;

    public EntityChangeEvent(AWEntity entity, ChangeType type){
        _entity = entity;
        _type = type;
    }

    
    public AWEntity getEntity() {
        return _entity;
    }

    public ChangeType getType(){
        return _type;
    }

}
