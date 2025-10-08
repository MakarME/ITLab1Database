package org.example.gui;

import org.example.model.*;
import org.example.model.Record;
import org.example.model.dialogs.CreateTableDialog;
import org.example.model.dialogs.EditRowDialog;
import org.example.model.dialogs.MergeTablesDialog;
import org.example.model.exceptions.ValidationException;
import org.example.utils.CSVUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final Database db = new Database();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> tableList = new JList<>(listModel);
    private final TableModelAdapter tableModelAdapter = new TableModelAdapter(new Table("empty", new ArrayList<Field>()));
    private final JTable jTable = new JTable(tableModelAdapter);

    public MainFrame(){
        super("Simple DB Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLayout(new BorderLayout());

        // left - table list
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(200, getHeight()));
        left.add(new JScrollPane(tableList), BorderLayout.CENTER);

        JPanel leftButtons = new JPanel(new GridLayout(0,1));
        leftButtons.add(new JButton(new AbstractAction("Create table"){
            @Override public void actionPerformed(ActionEvent e){ onCreateTable(); }
        }));
        leftButtons.add(new JButton(new AbstractAction("Drop table"){ @Override public void actionPerformed(ActionEvent e){ onDropTable(); }}));
        leftButtons.add(new JButton(new AbstractAction("Save DB"){ @Override public void actionPerformed(ActionEvent e){ onSaveDB(); }}));
        leftButtons.add(new JButton(new AbstractAction("Load DB"){ @Override public void actionPerformed(ActionEvent e){ onLoadDB(); }}));
        leftButtons.add(new JButton(new AbstractAction("Merge tables") { @Override public void actionPerformed(ActionEvent e) { onMergeTables(); }}));
        left.add(leftButtons, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);

        // center - table view
        JPanel center = new JPanel(new BorderLayout());
        center.add(new JScrollPane(jTable), BorderLayout.CENTER);

        JPanel cButtons = new JPanel();
        cButtons.add(new JButton(new AbstractAction("Add row"){ @Override public void actionPerformed(ActionEvent e){ onAddRow(); }}));
        cButtons.add(new JButton(new AbstractAction("Edit row"){ @Override public void actionPerformed(ActionEvent e){ onEditRow(); }}));
        cButtons.add(new JButton(new AbstractAction("Delete row"){ @Override public void actionPerformed(ActionEvent e){ onDeleteRow(); }}));
        cButtons.add(new JButton(new AbstractAction("Import CSV"){ @Override public void actionPerformed(ActionEvent e){ onImportCSV(); }}));
        cButtons.add(new JButton(new AbstractAction("Export CSV"){ @Override public void actionPerformed(ActionEvent e){ onExportCSV(); }}));
        center.add(cButtons, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) onTableSelected();
        });

        // sample: create a demo table
        createDemo();
    }

    private void createDemo(){
        List<Field> schema = Arrays.asList(
                new Field("id", FieldType.INTEGER, false),
                new Field("nickname", FieldType.STRING, false),
                new Field("grade", FieldType.REAL, true),
                new Field("flag", FieldType.CHAR, true, 1),
                new Field("cint", FieldType.COMPLEX_INTEGER, true),
                new Field("creal", FieldType.COMPLEX_REAL, true)
        );

        db.createTable("demo_all_types", schema);
        listModel.addElement("demo_all_types");

        try {
            // пример 1
            org.example.model.Record r1 = new org.example.model.Record(Arrays.asList(
                    1L,                    // INTEGER (Long)
                    "Alice",               // STRING
                    12.5,                  // REAL (Double)
                    'A',                   // CHAR
                    new ComplexInteger(3, 4), // COMPLEX_INTEGER
                    new ComplexReal(1.5, -2.25) // COMPLEX_REAL
            ));
            db.getTable("demo_all_types").insert(r1);

            // пример 2 (с null-значениями там, где nullable = true)
            org.example.model.Record r2 = new org.example.model.Record(Arrays.asList(
                    2L,
                    "Bob",
                    null,                  // grade = null
                    null,                  // flag = null
                    new ComplexInteger(0, 1),
                    null                   // creal = null
            ));
            db.getTable("demo_all_types").insert(r2);

        } catch (ValidationException ex){
            ex.printStackTrace();
        }

        tableList.setSelectedValue("demo_all_types", true);
    }

    private void onCreateTable(){
        CreateTableDialog dlg = new CreateTableDialog(this);
        dlg.setVisible(true);
        if(!dlg.isOk()) return;
        String name = dlg.getTableName();
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Table name cannot be empty");
            return;
        }
        List<Field> schema = dlg.getSchema();
        if (schema == null || schema.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Table schema cannot be empty. Add at least one field.");
            return;
        }
        try {
            db.createTable(name, schema);
            listModel.addElement(name);
            tableList.setSelectedValue(name, true);
        } catch (Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }


    private void onDropTable(){
        String sel = tableList.getSelectedValue();
        if(sel == null) return;
        int res = JOptionPane.showConfirmDialog(this, "Drop table '"+sel+"'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(res == JOptionPane.YES_OPTION){
            db.dropTable(sel);
            listModel.removeElement(sel);
            if(!listModel.isEmpty()) tableList.setSelectedIndex(0);
            else tableModelAdapter.setTable(new Table("empty", new ArrayList<Field>()));
        }
    }

    private void onSaveDB() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("DB JSON file", "json"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                // Экспортируем в JSON
                Map<String, Object> dump = exportFull();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(f, dump);
                JOptionPane.showMessageDialog(this, "Saved as JSON");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void onLoadDB() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("DB JSON file", "json"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> dump = mapper.readValue(f, Map.class);
                loadFull(dump);

                listModel.clear();
                for (String name : db.tableNames()) listModel.addElement(name);
                if (!listModel.isEmpty()) tableList.setSelectedIndex(0);

                JOptionPane.showMessageDialog(this, "Loaded from JSON");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // copy loaded db's tables into this.db via reflection-free method (serialize->deserialize) not needed; simply replace content by writing to temp file and reading? Here we will reflectively set the private field for brevity.
    private void copyDatabase(Database loaded) throws Exception{
        java.lang.reflect.Field f = Database.class.getDeclaredField("tables");
        f.setAccessible(true);
        f.set(db, f.get(loaded));
        f.setAccessible(false);
    }

    private void onTableSelected(){
        String sel = tableList.getSelectedValue();
        if(sel == null) return;
        Table t = db.getTable(sel);
        tableModelAdapter.setTable(t);
        jTable.updateUI();
    }

    private void onAddRow(){
        String sel = tableList.getSelectedValue(); if(sel==null) return;
        Table t = db.getTable(sel);
        EditRowDialog dlg = new EditRowDialog(this, t.getSchema(), null);
        dlg.setVisible(true);
        if(!dlg.isOk()) return;
        try{
            org.example.model.Record r = new org.example.model.Record(dlg.getParsedValues());
            t.insert(r);
            tableModelAdapter.fireTableDataChanged();
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Insert failed: "+ex.getMessage()); }
    }

    private void onEditRow(){
        String sel = tableList.getSelectedValue(); if(sel==null) return;
        int row = jTable.getSelectedRow(); if(row<0) { JOptionPane.showMessageDialog(this, "Select row"); return; }
        Table t = db.getTable(sel);
        org.example.model.Record old = t.getRows().get(row);
        EditRowDialog dlg = new EditRowDialog(this, t.getSchema(), old);
        dlg.setVisible(true);
        if(!dlg.isOk()) return;
        try{
            org.example.model.Record r = new org.example.model.Record(dlg.getParsedValues());
            t.update(row, r);
            tableModelAdapter.fireTableDataChanged();
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Update failed: "+ex.getMessage()); }
    }

    private void onDeleteRow(){
        String sel = tableList.getSelectedValue(); if(sel==null) return;
        int row = jTable.getSelectedRow(); if(row<0) { JOptionPane.showMessageDialog(this, "Select row"); return; }
        Table t = db.getTable(sel);
        t.delete(row);
        tableModelAdapter.fireTableDataChanged();
    }

    private void onImportCSV(){
        String sel = tableList.getSelectedValue(); if(sel==null) return;
        JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            File f = fc.getSelectedFile();
            try{
                List<List<String>> csv = CSVUtil.importCSV(f);
                if(csv.isEmpty()) throw new Exception("Empty file");
                // first row maybe header - we will assume first row is header if it equals schema names
                Table t = db.getTable(sel);
                int headerMatches = 0;
                List<String> header = csv.get(0);
                for(int i=0;i<Math.min(header.size(), t.getSchema().size()); i++){
                    if(header.get(i).equals(t.getSchema().get(i).getName())) headerMatches++;
                }
                int start = (headerMatches == t.getSchema().size()) ? 1 : 0;
                for(int i=start;i<csv.size();i++){
                    List<String> row = csv.get(i);
                    List<Object> parsed = parseStringRow(row, t.getSchema());
                    t.insert(new Record(parsed));
                }
                tableModelAdapter.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Imported");
            }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Import failed: "+ex.getMessage()); }
        }
    }

    private void onExportCSV(){
        String sel = tableList.getSelectedValue(); if(sel==null) return;
        JFileChooser fc = new JFileChooser();
        if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File f = fc.getSelectedFile();
            try{ CSVUtil.exportTableToCSV(db.getTable(sel), f); JOptionPane.showMessageDialog(this, "Exported"); }
            catch(Exception ex){ JOptionPane.showMessageDialog(this, "Export failed: "+ex.getMessage()); }
        }
    }

    private void onMergeTables() {
        if (listModel.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least two tables to merge.");
            return;
        }
        MergeTablesDialog dlg = new MergeTablesDialog(this, new ArrayList<>(db.tableNames()));
        dlg.setVisible(true);
        if (!dlg.isOk()) return;

        String a = dlg.getTableA();
        String b = dlg.getTableB();
        String newName = dlg.getNewTableName();

        if (a.equals(b)) {
            JOptionPane.showMessageDialog(this, "Choose two different tables.");
            return;
        }
        if (newName == null || newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "New table name cannot be empty.");
            return;
        }
        if (db.tableNames().contains(newName)) {
            JOptionPane.showMessageDialog(this, "Table with that name already exists.");
            return;
        }

        try {
            db.mergeTables(a, b, newName);
            listModel.addElement(newName);
            tableList.setSelectedValue(newName, true);
            JOptionPane.showMessageDialog(this, "Merged into table '" + newName + "'");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Merge failed: " + ex.getMessage());
        }
    }

    // parse strings to typed objects according to schema
    private List<Object> parseStringRow(List<String> strs, List<Field> schema) throws Exception{
        List<Object> out = new ArrayList<>();
        for(int i=0;i<schema.size();i++){
            Field f = schema.get(i);
            String s = i < strs.size() ? strs.get(i) : "";
            if(s == null || s.isEmpty()){
                if(f.isNullable()) out.add(null);
                else throw new Exception("Field '"+f.getName()+"' is required");
                continue;
            }
            switch(f.getType()){
                case INTEGER:
                    // поддержим как целые с точкой/запятой и без
                    String intNormalized = s.replace(',', '.');
                    // Если в поле есть дробная часть — ошибка, но попробуем округлить/записать как long только целую часть
                    if (intNormalized.contains(".")) {
                        // поддерживаем целую запись: взять часть до точки
                        intNormalized = intNormalized.split("\\.")[0];
                    }
                    out.add(Long.parseLong(intNormalized));
                    break;
                case REAL:
                    // заменяем , -> .
                    out.add(Double.parseDouble(s.replace(',', '.')));
                    break;
                case CHAR:
                    if (s.length()!=1) throw new Exception("Field '"+f.getName()+"' expects single char");
                    out.add(s.charAt(0)); break;
                case STRING:
                    out.add(s); break;
                case COMPLEX_INTEGER: {
                    double[] parts = parseComplexToDoubles(s);
                    long a = (long) parts[0];
                    long b = (long) parts[1];
                    out.add(new ComplexInteger(a,b));
                    break;
                }
                case COMPLEX_REAL: {
                    double[] parts = parseComplexToDoubles(s);
                    out.add(new ComplexReal(parts[0], parts[1]));
                    break;
                }
                default: out.add(s);
            }
        }
        return out;
    }

    public static double[] parseComplexToDoubles(String raw) throws Exception {
        if (raw == null) throw new Exception("Empty complex");
        String s = raw.trim();
        if (s.endsWith("i") || s.endsWith("I")) {
            s = s.substring(0, s.length() - 1).trim();
        } else {
            throw new Exception("Complex must end with 'i'");
        }
        // Normalize decimal comma -> dot
        s = s.replace(',', '.');

        // find separator: first '+' or '-' after index 0 (skip leading sign of real)
        int sep = -1;
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '+' || c == '-') {
                sep = i;
                break;
            }
        }
        if (sep == -1) throw new Exception("Cannot find separator between real and imag in '" + raw + "'");

        String realPart = s.substring(0, sep).trim();
        String imagPart = s.substring(sep).trim(); // includes sign(s)

        if (realPart.isEmpty() || imagPart.isEmpty()) throw new Exception("Bad complex format: " + raw);

        // Normalize sequences like "+-2.25" -> "-2.25", "-+2.25" -> "-2.25", "++2.25" -> "+2.25", "--2.25" -> "2.25" (double sign handling)
        // Simplest: if imagPart starts with '+' followed by '-'/'+', drop leading '+'.
        if (imagPart.length() >= 2) {
            char first = imagPart.charAt(0);
            char second = imagPart.charAt(1);
            if ((first == '+' || first == '-') && (second == '+' || second == '-')) {
                // Examples:
                // "+-2.25" -> "-2.25"
                // "-+2.25" -> "+2.25"  (we want sign of second? keep second with its sign)
                // "++2.25" -> "+2.25"
                // "--2.25" -> "-2.25" (treat as negative)
                // We'll handle explicitly:
                if (first == '+' && second == '-') {
                    imagPart = imagPart.substring(1); // "+-x" -> "-x"
                } else if (first == '+' && second == '+') {
                    imagPart = imagPart.substring(1); // "++x" -> "+x"
                } else if (first == '-' && second == '+') {
                    // "-+x" -> "+x" (drop the first '-')
                    imagPart = imagPart.substring(1);
                } else if (first == '-' && second == '-') {
                    // "--x" -> "-x"  (keep one '-')
                    imagPart = "-" + imagPart.substring(2);
                }
            }
        }

        double real = Double.parseDouble(realPart);
        double imag = Double.parseDouble(imagPart);
        return new double[] { real, imag };
    }

    private Map<String, Object> exportFull() {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> tablesMap = new LinkedHashMap<>();

        for (String tableName : db.tableNames()) {
            Table t = db.getTable(tableName);
            Map<String, Object> tableDump = new LinkedHashMap<>();
            tableDump.put("schema", t.getSchema());

            List<List<Object>> rowsList = new ArrayList<>();
            for (Record r : t.getRows()) {
                rowsList.add(new ArrayList<>(r.getValues()));
            }

            tableDump.put("rows", rowsList);
            tablesMap.put(tableName, tableDump);
        }

        result.put("tables", tablesMap);
        return result;
    }

    // Импорт полной базы (аналогично web.loadFull)
    @SuppressWarnings("unchecked")
    private void loadFull(Map<String, Object> dump) throws Exception {
        // Очистка старых таблиц
        for (String name : Set.copyOf(db.tableNames())) {
            db.dropTable(name);
        }

        if (dump == null) return;
        Map<String, Object> tables = (Map<String, Object>) dump.get("tables");
        if (tables == null) return;

        for (Map.Entry<String, Object> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            Map<String, Object> tableDump = (Map<String, Object>) entry.getValue();

            // --- схема ---
            List<Field> schema = new ArrayList<>();
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

            // --- строки ---
            Table t = db.getTable(tableName);
            Object rowsObj = tableDump.get("rows");
            if (rowsObj instanceof List<?> rowsList) {
                for (Object rowObj : rowsList) {
                    List<Object> row;
                    if (rowObj instanceof List<?>) {
                        row = (List<Object>) rowObj;
                    } else if (rowObj instanceof Map<?, ?> mapRow) {
                        row = new ArrayList<>();
                        int i = 0;
                        while (mapRow.containsKey(String.valueOf(i))) {
                            row.add(mapRow.get(String.valueOf(i)));
                            i++;
                        }
                    } else {
                        throw new Exception("Unexpected row type: " + rowObj.getClass());
                    }
                    t.insert(new Record(row));
                }
            }
        }
    }
}