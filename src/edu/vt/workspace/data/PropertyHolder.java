package edu.vt.workspace.data;

/**
 * This is a generic interface for objects in AW that can support properties 
 * (i.e., arbitrary textual key, value pairs). The actual implementation is up to the specific class, 
 * but is probably a hash table of some kind.
 * @author cpa
 */
public interface PropertyHolder {
    public void setProperty(String key, String value);
    public String getProperty(String key);
    public boolean hasProperty(String key);
}
