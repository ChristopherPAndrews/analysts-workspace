package edu.vt.workspace.data;

import edu.vt.workspace.components.utilities.EntityChangeEvent;
import edu.vt.workspace.components.utilities.EntityChangeListener;
import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * This class manages all of the entities in the system. There are a couple of main goals
 * for this class:
 * - it should provide a quick way to list all entities and their counts, discriminated based on type
 * - it should act as an "Entity Factory", building entities and distributed them to make sure that
 * - it should map entity types to colors
 * only one entity of a given type exists at a time.
 *
 * A current limitation of this is the hard limit of 8 entity types at the moment (due to the colors)
 *
 * @author cpa
 */
public class EntityManager implements AWSavable {

    private static final Map<String, URL> ENTITY_ICONS;

    static {
        Map<String, URL> tmpMap = new HashMap<String, URL>(5);
        tmpMap.put("person", EntityManager.class.getResource("/resources/images/person_icon.png"));
        tmpMap.put("money", EntityManager.class.getResource("/resources/images/money_icon.png"));
        tmpMap.put("place", EntityManager.class.getResource("/resources/images/place_icon.png"));
        tmpMap.put("location", EntityManager.class.getResource("/resources/images/place_icon.png"));
        tmpMap.put("date", EntityManager.class.getResource("/resources/images/date_icon.png"));
        tmpMap.put("organization", EntityManager.class.getResource("/resources/images/organization_icon.png"));
        tmpMap.put("email", EntityManager.class.getResource("/resources/images/email_icon.png"));
        ENTITY_ICONS = Collections.unmodifiableMap(tmpMap);
    }
    private static final Color[] ENTITY_COLORS = {
        new Color(228, 26, 28),
        new Color(55, 126, 184),
        new Color(77, 175, 74),
        new Color(152, 78, 163),
        new Color(255, 127, 0),
        new Color(255, 255, 51),
        new Color(166, 86, 40),
        new Color(247, 129, 191)
    };
    private static EntityManager _instance = new EntityManager();
    private HashMap<Integer, AWEntity> _entityCache = new HashMap<Integer, AWEntity>(1000);
    private int _currentColor = 0; // used to select entity color as new types are added
    private HashMap<String, Color> _entityColorMap = new HashMap<String, Color>(5);
    private ArrayList<EntityChangeListener> _entityChangeListeners = new ArrayList<EntityChangeListener>(10);

    /**
     * This is a singleton class - use getInstance to get an instance of this
     */
    private EntityManager() {
        
    }

    /**
     * Get the current instance of the EntityManager
     *
     * @return the EntityManager
     */
    public static EntityManager getInstance() {
        return _instance;
    }

    /**
     * Clear out the Entity collection and start fresh.
     */
    public static void reset() {
        _instance.clear();


        _instance = new EntityManager();
    }

    /**
     * This method clears out all entities in the system. The only thing this really does is tell
     * all other objects that have entities to release them. The actual clearing will be handled
     * by the reset method that throws out the whole instance.
     */
    private void clear() {

        for (AWEntity entity : _entityCache.values()) {
            sendEntityRemoveEvent(entity);
        }
        _entityCache.clear();

    }
    
    
    private Color pickColor(){
        Color color = null;
         // we haven't seen this type before, so we need to pick a new color
        if (_currentColor < ENTITY_COLORS.length) {
            color = ENTITY_COLORS[_currentColor++];
        } else if (_entityColorMap.size() < ENTITY_COLORS.length) {
            // we have run through all colors, but there is an unused one
            for (int i = 0; i < ENTITY_COLORS.length; i++) {
                color = ENTITY_COLORS[i];
                if (!_entityColorMap.containsValue(color)) {
                    // i is unused, so we can use that
                    break;
                }
            }
        } else {
            // all of the colors are used up, so we need to recycle
            color = ENTITY_COLORS[_currentColor % ENTITY_COLORS.length];
        }
        return color;
    }

    /**
     * This is a helper method that adds a new type to the system. it creates the new entry
     * in the _entityTypes list where all of the entities are stored. It also registers
     * the new type with a color. Currently, there are a limited number of colors, so this cycles through
     * them in order. If the end of the list is hit, this will look for colors that are no longer in use.
     * If there aren't any, this cycles back through the list again, reusing old colors.
     *
     * @param type the new entity type being added to the system
     */
    private void addType(String type) {

        if (_entityColorMap.containsKey(type)) {
            return; // we already know about this type
        }

        Color color = AWDataManager.getInstance().getEntityTypeColor(type);
        if (color != null) { // we know about the type, but need to load it locally
            if (color.equals(Color.black)){
                color = pickColor();
            }
            _entityColorMap.put(type, color);
            return;
        }
        color = pickColor();
       
        _entityColorMap.put(type, color);
        AWDataManager.getInstance().addEntityType(type, color);
    }

    /**
     * This method is used to add a newly made entity to the system. This is one that
     * has been created dynamically by the user, so it is already marshaled into an
     * entity data structure.
     * @param entity
     */
    public void addEntity(AWEntity entity) {
        int id;
        if (!_entityColorMap.containsKey(entity.getType())) {
            // new type, add it
            addType(entity.getType());
        }

        if (entity.getID() <= 0) { // entity is not in the data store
            id = AWDataManager.getInstance().addEntity(entity.getValue(), entity.getType());
            entity.setID(id);
        }

          
        _entityCache.put(entity.getID(), entity);

        // add the entity to any entity listeners
        EntityChangeEvent ece = new EntityChangeEvent(entity, EntityChangeEvent.ChangeType.ADD_ENTITY);
        for (EntityChangeListener listener : _entityChangeListeners) {
            listener.addEntity(ece);
        }
    }
    
    /**
     * This method returns the AWEntity of the appropriate type and value. This attempts to find
     * an existing entity, but if there isn't one, it creates it and files it.
     *
     * 
     * @param type the category of the entity (e.g., Location, Person, ...)
     * @param value the value of the entity (e.g., London, George Prado, ...)
     * @return an AWEntity matching the parameters
     */
    public AWEntity getEntity(String type, String value) {
        AWEntity entity;
        int id;
        if (!_entityColorMap.containsKey(type)) {
            // new type, add it
            addType(type);
        } else {
            // look to see if we know the entity
            id = AWDataManager.getInstance().getEntity(value);
            if (id > 0 && _entityCache.containsKey(id)) {
                return _entityCache.get(id);
            }
        }


        // this entity isn't in the main list, do we have an alias for it?

        if ((id = AWDataManager.getInstance().getEntityForAlias(value)) > 0) {
            return _entityCache.get(id);
        }


        // this is a new entity
        entity = new AWEntity(type, value);
        //entities.add(entity);
        id = AWDataManager.getInstance().addEntity(value, type);
        entity.setID(id);
        _entityCache.put(id, entity);
        return entity;
    }

    /**
     * This method tries to find a matching entity to the given value. In this instance, we do not
     * want to make one if we can't find it. There is also no guarantee that we will find the
     * right entity if there are entities with different types and the same value.
     *
     * @param value
     * @return
     */
    public AWEntity getEntity(String value) {
        int id;
        id = AWDataManager.getInstance().getEntity(value);
        if (id > 0 && _entityCache.containsKey(id)) {
            return _entityCache.get(id);
        }

        // this entity isn't in the main list, do we have an alias for it?

        if ((id = AWDataManager.getInstance().getEntityForAlias(value)) > 0 && _entityCache.containsKey(id)) {
            return _entityCache.get(id);
        }
        return null;
    }
    
    /**
     * This method fetches an entity based on the entity id.
     * 
     * @param id the numerical id of the alias
     * @return 
     */
    public AWEntity getEntity(int id){
        return _entityCache.get(id);
    }
    

    /**
     * Return a list of all entity categories we know about.
     *
     * @return the list of categories
     */
    public Set<String> getEntityTypes() {
        return _entityColorMap.keySet();
    }

    /**
     * Returns a list of all entities of a given type. If there are no entities
     * of that type, an empty list is returned.
     *
     * @param type the type of entities that are desired
     * @return the list of entities
     */
    public Vector<AWEntity> getEntities(String type) {
        if (!_entityColorMap.containsKey(type)) {
            // this is not a type we know - return an empty list
            return new Vector<AWEntity>(0);
        }

        int[] entityIds = AWDataManager.getInstance().getEntitiesForType(type);
        Vector<AWEntity> entities = new Vector<AWEntity>(entityIds.length);
        for (int id : entityIds) {
            entities.add(_entityCache.get(id));
        }
        return entities;
    }

    /**
     * This method is used to register an alias for an entity. This should add the
     * alias to the primary entities alias list and then remove all instances
     * of the alias, replacing them with the primary entity where necessary. We then load the
     * alias into a special list so we can fetch the master if necessary.
     *
     * @param entity
     * @param alias
     */
    public void registerAlias(AWEntity entity, AWEntity alias) {

        AWDataManager.getInstance().removeEntity(alias);
        AWDataManager.getInstance().registerAlias(entity, alias.getValue());
        _entityCache.remove(alias.getID());

        sendEntityChangeEvent(entity, EntityChangeEvent.ChangeType.ALIAS_CHANGE);
        // sendEntityRemoveEvent(alias);
    }

    /**
     * Given an entity, this returns an appropriate color for the entity
     *
     * @param entity the entity we want to color
     * @return a preselected Color associated with the entity
     */
    public Color getColor(AWEntity entity) {
        return _entityColorMap.get(entity.getType());
    }

    /**
     * Given an entity, this returns an appropriate color for the entity
     *
     * @param string an entity type
     * @return a preselected Color associated with the entity
     */
    public Color getColor(String string) {
        try {
            return _entityColorMap.get(string);
        } catch (java.lang.NullPointerException npe) {
            return null;
        }
    }

    /**
     * Given an entity, this returns an appropriate icon file (if one exists).
     * 
     * @param entity
     * @return url of an icon resource to use or null if one doesn't exist
     */
    public URL getIconURL(AWEntity entity) {
        URL url = null;
        String type = entity.getType();
        url = ENTITY_ICONS.get(type.toLowerCase());

        return url;
    }

    /**
     * This method is used to completely wipe an entity from the system. If it is the
     * last entity of its kind, the type is removed as well.
     *
     * @param entity the {@code AWEntity} to be removed
     */
    public void removeEntity(AWEntity entity) {
        AWDataManager.getInstance().removeEntity(entity);
        _entityCache.remove(entity.getID());

        if (AWDataManager.getInstance().getEntityTypeCount(entity.getType()) == 0) {
            // the type is no longer in use, remove it
            AWDataManager.getInstance().removeEntityType(entity.getType());
            _entityColorMap.remove(entity.getType());
        }


        // notify listeners and documents of the change
        sendEntityRemoveEvent(entity);


    }

    /**
     * This method changes the type of an entity. It removes it from one type list
     * and adds it to the new one.
     *
     * @param entity
     * @param type
     */
    public void changeEntityType(AWEntity entity, String type) {
        String oldType = entity.getType();
        entity.setType(type);

        if (!_entityColorMap.containsKey(type)) { // this is a new entity category
            addType(type);
        }

        AWDataManager.getInstance().changeEntityType(entity, type);

        // remove dead type if applicable
        if (AWDataManager.getInstance().getEntityTypeCount(oldType) == 0) {
            // the type is no longer in use, remove it
            AWDataManager.getInstance().removeEntityType(oldType);
            _entityColorMap.remove(oldType);
        }


        // notify listeners and documents of the change
        sendEntityChangeEvent(entity, EntityChangeEvent.ChangeType.TYPE_CHANGE);
    }

    public void addEntityChangeListener(EntityChangeListener listener) {
        _entityChangeListeners.add(listener);
    }

    public void removeEntityChangeListener(EntityChangeListener listener) {
        _entityChangeListeners.remove(listener);
    }

    private void sendEntityChangeEvent(AWEntity entity, EntityChangeEvent.ChangeType type) {
        EntityChangeEvent ece = new EntityChangeEvent(entity, type);
        for (EntityChangeListener listener : _entityChangeListeners) {
            listener.entityChanged(ece);
        }

        for (AWDocument doc : entity.getDocs()) {
            doc.entityChanged(entity);
        }
    }

    private void sendEntityRemoveEvent(AWEntity entity) {
        // remove the entity from all documents
        for (AWDocument doc : entity.getDocs()) {
            doc.removeEntity(entity);
        }

        // remove the entity from any entity listeners
        EntityChangeEvent ece = new EntityChangeEvent(entity, EntityChangeEvent.ChangeType.REMOVE_ENTITY);
        for (EntityChangeListener listener : _entityChangeListeners) {
            listener.removeEntity(ece);
        }
    }

    /**
     * Write out the save data for each entity.
     *
     * @param writer
     */
    public void writeData(AWWriter writer) {
        for (AWEntity entity : _entityCache.values()) {
            writer.write("entity", entity);
        }
    }
}
