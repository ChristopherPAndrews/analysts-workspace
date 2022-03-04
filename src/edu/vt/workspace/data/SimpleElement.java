
package edu.vt.workspace.data;

/**
 * SimpleElements are basic XML elements with no hierarchical data. In other words, it consists
 * of a name (the tag), an optional value (the character data), and an optional list of
 * attributes.
 *
 * @param <T> The type of the data stored in this object
 * @author cpa@cs.vt.edu
 */
public class SimpleElement<T> {
    String _name;
    T _value;


    public SimpleElement(){

    }


    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public T getValue() {
        return _value;
    }

    public void setValue(T _value) {
        this._value = _value;
    }


    public void clear(){
        _name = "";
        _value = null;
    }


    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(_name);
        str.append(" : ");
        str.append(_value);
        return str.toString();
    }
}
