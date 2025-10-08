package org.example.model;

import org.example.model.exceptions.ValidationException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database implements Serializable {
    private final Map<String, Table> tables = new HashMap<>();

    public void createTable(String name, List<Field> schema) {
        if(tables.containsKey(name)) throw new IllegalArgumentException("Table exists");
        tables.put(name, new Table(name, schema));
    }

    public void dropTable(String name) {
        tables.remove(name);
    }

    public Table getTable(String name){
        Table t = tables.get(name);
        if(t == null) throw new IllegalArgumentException("No table "+name);
        return t;
    }

    public java.util.Set<String> tableNames(){ return tables.keySet(); }

    public void saveToFile(File f) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        }
    }

    public static Database loadFromFile(File f) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Database) ois.readObject();
        }
    }

    public void mergeTables(String a, String b, String newName) {
        Table ta = getTable(a);
        Table tb = getTable(b);
        List<Field> sa = ta.getSchema();
        List<Field> sb = tb.getSchema();
        if(sa.size() != sb.size()) throw new IllegalArgumentException("Schemas size mismatch");
        for(int i=0;i<sa.size();i++){
            Field fa = sa.get(i);
            Field fb = sb.get(i);
            if(!fa.getName().equals(fb.getName()) || fa.getType() != fb.getType())
                throw new IllegalArgumentException("Schema mismatch at "+i);
        }
        Table tn = new Table(newName, sa);
        try {
            for(org.example.model.Record r : ta.getRows()) tn.insert(r);
            for(Record r : tb.getRows()) tn.insert(r);
        } catch (ValidationException e){
            throw new RuntimeException(e);
        }
        tables.put(newName, tn);
    }
}