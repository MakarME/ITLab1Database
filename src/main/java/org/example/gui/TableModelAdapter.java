package org.example.gui;

import org.example.model.Record;
import org.example.model.Table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TableModelAdapter extends AbstractTableModel {
    private Table table;

    public TableModelAdapter(Table table){ this.table = table; }

    public void setTable(Table t){ this.table = t; fireTableStructureChanged(); }

    @Override public int getRowCount() { return table == null ? 0 : table.getRows().size(); }
    @Override public int getColumnCount() { return table == null ? 0 : table.getSchema().size(); }

    @Override public String getColumnName(int column) { return table.getSchema().get(column).getName(); }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        return table.getRows().get(rowIndex).get(columnIndex);
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    // helper to return a copy of record values as strings
    public List<String> getRowAsStrings(int row){
        List<String> out = new ArrayList<>();
        Record r = table.getRows().get(row);
        for(Object v : r.getValues()) out.add(v == null ? "" : v.toString());
        return out;
    }
}