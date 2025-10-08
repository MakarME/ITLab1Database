package org.example.model.dialogs;

import org.example.model.Field;
import org.example.model.FieldType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class AddFieldDialog extends JDialog {
    private boolean ok=false;
    private final JTextField name = new JTextField();
    private final JComboBox<FieldType> type = new JComboBox<>(FieldType.values());
    private final JCheckBox nullable = new JCheckBox("nullable");
    private final JTextField charLen = new JTextField();

    public AddFieldDialog(Dialog owner){
        super(owner, "Add field", true);
        setSize(350,200);
        setLayout(new GridLayout(0,2));
        add(new JLabel("Name:")); add(name);
        add(new JLabel("Type:")); add(type);
        add(new JLabel("Nullable:")); add(nullable);
        add(new JLabel("Char length (for CHAR):")); add(charLen);
        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("OK"){ public void actionPerformed(ActionEvent e){ ok=true; setVisible(false); }}));
        p.add(new JButton(new AbstractAction("Cancel"){ public void actionPerformed(ActionEvent e){ ok=false; setVisible(false); }}));
        add(p);
    }

    public boolean isOk(){ return ok; }
    public Field getField(){
        String n = name.getText().trim();
        FieldType t = (FieldType) type.getSelectedItem();
        boolean nullb = nullable.isSelected();
        Integer clen = null;
        if(!charLen.getText().trim().isEmpty()) try{ clen = Integer.parseInt(charLen.getText().trim()); }catch(Exception ex){}
        return new Field(n, t, nullb, clen);
    }
}