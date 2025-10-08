package org.example.model;

import org.example.model.exceptions.ValidationException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable {
    private final String name;
    private final List<Field> schema;
    private final List<org.example.model.Record> rows = new ArrayList<>();

    public Table(String name, List<Field> schema) {
        this.name = name;
        this.schema = new ArrayList<>(schema);
    }

    public String getName(){ return name; }
    public List<Field> getSchema(){ return schema; }
    public List<org.example.model.Record> getRows(){ return rows; }

    public void insert(org.example.model.Record r) throws ValidationException {
        validate(r);
        Integer idIndex = findIdIndex();
        if (idIndex != null) {
            Object idVal = r.get(idIndex);
            if (idVal == null) throw new ValidationException("Field 'id' cannot be null");
            for (org.example.model.Record existing : rows) {
                Object exVal = existing.get(idIndex);
                if (idVal.equals(exVal))
                    throw new ValidationException("Duplicate id value: " + idVal);
            }
        }
        rows.add(r);
    }

    public void update(int index, org.example.model.Record r) throws ValidationException {
        if(index < 0 || index >= rows.size()) throw new IndexOutOfBoundsException("row");
        validate(r);
        Integer idIndex = findIdIndex();
        if (idIndex != null) {
            Object idVal = r.get(idIndex);
            if (idVal == null) throw new ValidationException("Field 'id' cannot be null");
            for (int i=0;i<rows.size();i++) {
                if (i == index) continue;
                Object exVal = rows.get(i).get(idIndex);
                if (idVal.equals(exVal))
                    throw new ValidationException("Duplicate id value: " + idVal);
            }
        }
        rows.set(index, r);
    }

    public void delete(int index) {
        if(index < 0 || index >= rows.size()) throw new IndexOutOfBoundsException("row");
        rows.remove(index);
    }

    public void validate(Record r) throws ValidationException {
        if(r.size() != schema.size()) throw new ValidationException("Record size != schema size");
        for(int i=0;i<schema.size();i++){
            Field f = schema.get(i);
            Object v = r.get(i);
            if(v == null){
                if(!f.isNullable()) throw new ValidationException("Field '"+f.getName()+"' is NOT NULL");
                else continue;
            }
            switch(f.getType()){
                case INTEGER:
                    if(!(v instanceof Integer) && !(v instanceof Long))
                        throw new ValidationException("Field "+f.getName()+" expects INTEGER");
                    break;
                case REAL:
                    if(!(v instanceof Float) && !(v instanceof Double))
                        throw new ValidationException("Field "+f.getName()+" expects REAL");
                    break;
                case CHAR:
                    if(!(v instanceof Character) && !(v instanceof String))
                        throw new ValidationException("Field "+f.getName()+" expects CHAR");
                    String s = v.toString();
                    Integer max = f.getCharLength();
                    if(max != null && s.length() > max)
                        throw new ValidationException("Field "+f.getName()+" CHAR length > "+max);
                    if(s.length() != 1) throw new ValidationException("Field "+f.getName()+" expects single char");
                    break;
                case STRING:
                    if(!(v instanceof String)) throw new ValidationException("Field "+f.getName()+" expects STRING");
                    break;
                case COMPLEX_INTEGER:
                    if(!(v instanceof ComplexInteger)) throw new ValidationException("Field "+f.getName()+" expects ComplexInteger");
                    break;
                case COMPLEX_REAL:
                    if(!(v instanceof ComplexReal)) throw new ValidationException("Field "+f.getName()+" expects ComplexReal");
                    break;
            }
        }
    }

    private Integer findIdIndex() {
        for (int i=0;i<schema.size();i++) {
            Field f = schema.get(i);
            if ("id".equalsIgnoreCase(f.getName())) return i;
        }
        return null;
    }

    @Override public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Table ").append(name).append(" ");
                sb.append("Schema: ");
        for(Field f: schema) sb.append(f.getName()).append(":").append(f.getType()).append(" ");
        sb.append(" Rows: ");
        for(int i=0;i<rows.size();i++){
            sb.append(i).append(": ").append(rows.get(i)).append(" ");
        }
        return sb.toString();
    }
}