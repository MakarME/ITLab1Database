package org.example.model;

import java.io.Serializable;
public class Field implements Serializable {
    private String name;
    private FieldType type;
    private boolean nullable;
    private Integer charLength;

    public Field() {}

    public Field(String name, FieldType type, boolean nullable, Integer charLength) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.charLength = charLength;
    }

    public Field(String name, FieldType type, boolean nullable) {
        this(name, type, nullable, null);
    }

    public String getName() { return name; }
    public FieldType getType() { return type; }
    public boolean isNullable() { return nullable; }
    public Integer getCharLength() { return charLength; }
}