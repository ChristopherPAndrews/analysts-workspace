package edu.vt.workspace.data;

/**
 * This is a simple wrapper class to help save entity ranges. It wraps the
 * entity and range together.
 */
public class EntityRange implements AWSavable {

    Range _range;
    AWEntity _entity;

    public void EntityRange() {
    }

    public void setEntity(AWEntity entity) {
        _entity = entity;
    }

    public void setEntityID(String entityID){
        _entity = EntityManager.getInstance().getEntity(entityID);
    }

    public AWEntity getEntity(){
        return _entity;
    }

    public Range getRange(){
        return _range;
    }

    public void setRange(Range range) {
        _range = range;
    }

    public void writeData(AWWriter writer) {
        if (_entity != null && _range != null) {
            writer.write("range", _range);
            writer.write("entityID", _entity.getValue());
        }
    }
}
