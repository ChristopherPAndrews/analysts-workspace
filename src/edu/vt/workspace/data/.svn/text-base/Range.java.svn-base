package edu.vt.workspace.data;

/**
 * This is a basic Range object. At the moment, its only reasponsibility is to store
 * a start and an end. 
 * 
 * @author cpa
 */
public class Range implements AWSavable{
    private int _start=0;
    private int _end=0;

    /**
     * Creates an empty Range object
     */
    public Range() {
    }

    /**
     * Creates a Range object with the designated start and end.
     *
     * @param start
     * @param end
     */
    public Range(int start, int end) {
        _start = start;
        _end = end;
    }

    public int getEnd() {
        return _end;
    }

    public void setEnd(int _end) {
        this._end = _end;
    }

    public int getStart() {
        return _start;
    }

    public void setStart(int _start) {
        this._start = _start;
    }

    public void setRange(int start, int end) {
        _start = start;
        _end = end;
    }

    public boolean isEmpty(){
        return _start==_end;
    }

    public void writeData(AWWriter writer) {
        writer.write("start", _start);
        writer.write("end", _end);
    }
    
    @Override
    public String toString() {
        return new String("Range: [" + _start + ", " + _end + "]");
    }

    @Override
    public boolean equals(Object obj) {
       if (obj instanceof Range){
           Range r2 = (Range)obj;
           return r2.getStart() == _start && r2.getEnd() == _end;
       }
       return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this._start;
        hash = 19 * hash + this._end;
        return hash;
    }


    
}
