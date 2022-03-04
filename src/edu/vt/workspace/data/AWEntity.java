

package edu.vt.workspace.data;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * This class holds basic entity information, which is basically type and value.
 * Entities are also designed to be unique - there should only be one active entity with
 * the same name at a time. As such, the entity maintains a list of the documents in
 * which it appears.
 *
 * @author cpa
 */
public class AWEntity implements Comparable, AWSavable, PropertyHolder{
    private int _id = -1;
    private String _type; // the type of entity (e.g., Location, Person, Organization)
    private String _value; // the value of the entity (e.g. New York, George Prado)
    private Set<AWDocument> _docs = new HashSet<AWDocument>(5);
    private Set<String> _aliases = new HashSet<String>(5);
    private Hashtable<String, String> _properties = new Hashtable<String, String>(); // this contains extra fields that might have been included in input files

    /**
     * Create a new AWEntity
     *
     * @param type the type of the entity (e.g., Location, Person, Organization)
     * @param value the value of the entity (e.g. New York, George Prado)
     */
    public AWEntity(String type, String value){
        _type = type;
        _value = value;
    }

    /**
     * Get the type of this entity
     * 
     * @return
     */
    public String getType() {
        return _type;
    }

    public void setType(String _type) {
        this._type = _type;
    }


    /**
     * Get the value of this entity
     *
     * @return
     */
    public String getValue() {
        return _value;
    }

    public void setValue(String _value) {
        this._value = _value;
    }

    


    /**
     * Get the id of this entity.
     * @return
     */
    public int getID(){
        return _id;
    }


    public void setID(int id){
        _id = id;
    }
    
    
    public void setProperty(String key, String value){
        _properties.put(key, value);
    }
    
    public String getProperty(String key){
        return _properties.get(key);
    }
    
    public boolean hasProperty(String key) {
        return _properties.containsKey(key);
    }

    
    
    




    /**
     * Add the contents of another entity as an alias. There is an assumption that
     * the types match...
     *
     * @param alias an entity that should be aliased to this one.
     */
    public void addAlias(AWEntity alias){
        // add the alias' primary name
        _aliases.add(alias.getValue());

        // add any aliases the alias has already accumulated
        for (String a: alias.getAliases()){
            _aliases.add(a);
        }

        // replace the alias with this one in all of the entities docs
        // and add docs to this entity's doc list
        for (AWDocument doc: alias.getDocs()){
            _docs.add(doc);
            doc.replaceEntity(alias, this);
        }

        // now register this alias with the Entitymanager
        EntityManager.getInstance().registerAlias(this, alias);
    }


    /**
     * Add this String as an alias. This basically just does a lookup with the
     * EntityManager to get an entity to alias to and then called the other version
     * of addAlias() with it.
     *
     * @param alias
     */
    public void addAlias(String alias){
        AWEntity entity = EntityManager.getInstance().getEntity(_type, alias);
        if (entity != this) // make sure we don't already know this alias
            addAlias(entity);
    }

    /**
     * Get a set of all of the aliases for this entity.
     *
     * @return
     */
    public Set<String> getAliases(){
        return _aliases;
    }

    /**
     * Add a document to the collection of places this entity appears
     * @param doc
     */
    public void addDoc(AWDocument doc) {
        _docs.add(doc);
    }

    /**
     * Return the number of documents that contain this entity. Note that this is a
     * loose interpretation of relevance - it is not the same as the number of times
     * the entity appears in a document.
     *
     * @return the document count
     */
    public int numDocs(){
        return _docs.size();
    }


    public Set<AWDocument> getDocs(){
        return _docs;
    }


    /**
     * Remove this document from the list of documents associated with this entity.
     * @param doc
     */
    void removeDocument(AWDocument doc) {
        _docs.remove(doc);
    }
    

    /**
     * This method reports if this entity matches a string value. This checks not only the
     * main value, but also all of the aliases.
     *
     * @param value the value to check against this entity
     * @return
     */
    public Boolean equals(String value){
        if (value.equalsIgnoreCase(_value))
            return true;
        // not the same, perhaps it is an alias
        return _aliases.contains(value);
    }

    /**
     * Build the String presentation of this entity
     * @return
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(_type);
        str.append("] ");
        str.append(_value);
        return str.toString();
    }

    /**
     * A simple comparison method that allows entities to be compared based on value.
     * @param o a second AWEntity
     * @return
     */
    public int compareTo(Object o) {
        return _value.compareToIgnoreCase(((AWEntity)o).getValue());
    }

    /**
     * Write out the data for this entity
     * 
     * @param writer
     */
    public void writeData(AWWriter writer) {
        writer.write("type", _type);
        writer.write("value", _value);
        writer.write("id", _id);
        for (String alias: _aliases){
            writer.write("alias", alias);
        }
    }

  
}
