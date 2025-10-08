package org.example.web;

import org.example.model.Field;
import org.example.model.FieldType;
import org.example.model.Database;
import org.example.model.Record;
import org.example.model.Table;
import org.example.model.exceptions.ValidationException;
import org.example.utils.CSVUtil;
import org.example.utils.ParseUtil;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class DatabaseService {
    private final Database db = new Database(); // ваша модель

    public Set<String> listTables() { return db.tableNames(); }

    public void createTable(String name, List<Field> schema) {
        db.createTable(name, schema);
    }

    public void dropTable(String name) { db.dropTable(name); }

    public Table getTable(String name) { return db.getTable(name); }

    public List<org.example.model.Record> getRows(String name) { return db.getTable(name).getRows(); }

    public void insertRow(String name, org.example.model.Record r) throws Exception {
        db.getTable(name).insert(r);
    }

    public void updateRow(String name, int index, org.example.model.Record r) throws Exception {
        db.getTable(name).update(index, r);
    }

    public void deleteRow(String name, int index) { db.getTable(name).delete(index); }

    public void mergeTables(String a, String b, String newName) { db.mergeTables(a,b,newName); }

    public void saveToFile(File f) throws IOException { db.saveToFile(f); }

    public void loadFromFile(File f) throws IOException, ClassNotFoundException {
        Database loaded = Database.loadFromFile(f);
        // replace internal map via reflection or by copying; easiest — replace by serialization assign:
        // For brevity we will set contents by serializing db and deserializing to copy - here we just set reference (dangerous but acceptable for demo)
        java.lang.reflect.Field fld;
        try {
            fld = Database.class.getDeclaredField("tables");
            fld.setAccessible(true);
            fld.set(db, fld.get(loaded));
            fld.setAccessible(false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void importCsv(String tableName, InputStream csvStream) throws Exception {
        Table t = db.getTable(tableName);
        List<List<String>> rows = CSVUtil.importCSV(csvStream);
        // detect header as before etc; reuse parse util similar to MainFrame.parseStringRow
        boolean firstIsHeader = false;
        if(!rows.isEmpty()){
            List<String> header = rows.get(0);
            int matches = 0;
            for(int i=0;i<Math.min(header.size(), t.getSchema().size()); i++){
                if(header.get(i).equals(t.getSchema().get(i).getName())) matches++;
            }
            firstIsHeader = (matches == t.getSchema().size());
        }
        int start = firstIsHeader ? 1 : 0;
        for(int i=start;i<rows.size();i++){
            List<String> rr = rows.get(i);
            List<Object> parsed = ParseUtil.parseStringRowToObjects(rr, t.getSchema());
            t.insert(new org.example.model.Record(parsed));
        }
    }

    public byte[] exportCsv(String tableName) throws IOException {
        Table t = db.getTable(tableName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CSVUtil.exportTableToCSVStream(t, baos);
        return baos.toByteArray();
    }

    public Map<String, Object> exportFull() {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> tablesMap = new LinkedHashMap<>();

        for (String tableName : db.tableNames()) {
            Table t = db.getTable(tableName);
            Map<String, Object> tableDump = new LinkedHashMap<>();
            tableDump.put("schema", t.getSchema());

            // Конвертируем каждую запись в List<Object>
            List<List<Object>> rowsList = new ArrayList<>();
            for (org.example.model.Record r : t.getRows()) {
                rowsList.add(new ArrayList<>(r.getValues())); // r.getValues() — список значений в Record
            }

            tableDump.put("rows", rowsList);
            tablesMap.put(tableName, tableDump);
        }

        result.put("tables", tablesMap);
        return result;
    }

    public void loadFull(Map<String, Object> dump) throws ValidationException {
        // Очистка текущих таблиц
        for (String name : Set.copyOf(db.tableNames())) {
            db.dropTable(name);
        }

        if (dump == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> tables = (Map<String, Object>) dump.get("tables");
        if (tables == null) return;

        for (Map.Entry<String, Object> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> tableDump = (Map<String, Object>) entry.getValue();

            // Схема
            List<Field> schema = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> schemaList = (List<Map<String, Object>>) tableDump.get("schema");
            if (schemaList != null) {
                for (Map<String, Object> fieldMap : schemaList) {
                    String name = (String) fieldMap.get("name");
                    FieldType type = FieldType.valueOf((String) fieldMap.get("type"));
                    Boolean nullable = fieldMap.containsKey("nullable") ? (Boolean) fieldMap.get("nullable") : Boolean.FALSE;

                    Integer charLength = null;
                    if (fieldMap.containsKey("charLength") && fieldMap.get("charLength") != null) {
                        charLength = ((Number) fieldMap.get("charLength")).intValue();
                    }

                    schema.add(new Field(name, type, nullable, charLength));
                }
            }

            db.createTable(tableName, schema);

            // Ряды
            Table t = db.getTable(tableName);
            Object rowsObj = tableDump.get("rows");
            if (rowsObj instanceof List<?> rowsList) {
                for (Object rowObj : rowsList) {
                    List<Object> row;
                    if (rowObj instanceof List<?>) {
                        row = (List<Object>) rowObj;
                    } else if (rowObj instanceof Map<?, ?> mapRow) {
                        // Если JSON десериализовался в Map, взять значения по ключам "0", "1"...
                        row = new ArrayList<>();
                        int i = 0;
                        while (mapRow.containsKey(String.valueOf(i))) {
                            row.add(mapRow.get(String.valueOf(i)));
                            i++;
                        }
                    } else {
                        throw new ValidationException("Unexpected row type: " + rowObj.getClass());
                    }
                    t.insert(new Record(row));
                }
            }
        }
    }
}
