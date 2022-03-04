package edu.vt.workspace.components.utilities;

import java.util.EventListener;

/**
 * An interface to be implemented by classes interested in being informed of changes
 * in entities.
 *
 * @author cpa
 */
public interface EntityChangeListener extends EventListener{

    public void addEntity(EntityChangeEvent ece);
    public void entityChanged(EntityChangeEvent ece);
    public void removeEntity(EntityChangeEvent ece);

}
