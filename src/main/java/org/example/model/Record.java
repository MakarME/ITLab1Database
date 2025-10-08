package org.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Record implements Serializable {
    private final List<Object> values;

    public Record(List<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public Object get(int idx){ return values.get(idx); }
    public List<Object> getValues(){ return Collections.unmodifiableList(values); }
    public void set(int idx, Object value){ values.set(idx, value); }
    public int size(){ return values.size(); }
    @Override public String toString(){ return values.toString(); }
}