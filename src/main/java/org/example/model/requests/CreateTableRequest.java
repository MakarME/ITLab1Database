package org.example.model.requests;

import org.example.model.Field;

import java.util.List;

public class CreateTableRequest {
    private String name;
    private List<Field> schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getSchema() {
        return schema;
    }

    public void setSchema(List<Field> schema) {
        this.schema = schema;
    }
}
