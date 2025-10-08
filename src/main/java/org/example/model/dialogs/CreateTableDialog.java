package org.example.model.dialogs;

import org.example.model.Field;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CreateTableDialog extends JDialog {
    private boolean ok = false;
    private final JTextField nameField = new JTextField();
    private final DefaultListModel<Field> schemaModel = new DefaultListModel<>();
    private final JList<Field> schemaList = new JList<>(schemaModel);

    public CreateTableDialog(Frame owner){
        super(owner, "Create table", true);
        setSize(500,400);
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Table name:"), BorderLayout.WEST);
        top.add(nameField, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(schemaList), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        btns.add(new JButton(new AbstractAction("Add field"){ public void actionPerformed(ActionEvent e){ onAddField(); }}));
        btns.add(new JButton(new AbstractAction("Remove field"){ public void actionPerformed(ActionEvent e){ int idx = schemaList.getSelectedIndex(); if(idx>=0) schemaModel.remove(idx); }}));
        btns.add(new JButton(new AbstractAction("OK"){ public void actionPerformed(ActionEvent e){ ok=true; setVisible(false); }}));
        btns.add(new JButton(new AbstractAction("Cancel"){ public void actionPerformed(ActionEvent e){ ok=false; setVisible(false); }}));
        add(btns, BorderLayout.SOUTH);
    }

    private void onAddField(){
        AddFieldDialog d = new AddFieldDialog(this);
        d.setVisible(true);
        if(d.isOk()) schemaModel.addElement(d.getField());
    }

    public boolean isOk(){ return ok; }
    public String getTableName(){ return nameField.getText().trim(); }
    public List<Field> getSchema(){ List<Field> out = new ArrayList<>(); for(int i=0;i<schemaModel.size();i++) out.add(schemaModel.get(i)); return out; }
}