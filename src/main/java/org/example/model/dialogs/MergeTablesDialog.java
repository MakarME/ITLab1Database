package org.example.model.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MergeTablesDialog extends JDialog {
    private boolean ok = false;
    private final JComboBox<String> boxA;
    private final JComboBox<String> boxB;
    private final JTextField newNameField = new JTextField();

    public MergeTablesDialog(Frame owner, List<String> tableNames) {
        super(owner, "Merge Tables", true);
        setSize(400,200);
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(3,2,5,5));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        p.add(new JLabel("Table A:"));
        boxA = new JComboBox<>(tableNames.toArray(new String[0]));
        p.add(boxA);

        p.add(new JLabel("Table B:"));
        boxB = new JComboBox<>(tableNames.toArray(new String[0]));
        p.add(boxB);

        p.add(new JLabel("New table name:"));
        p.add(newNameField);

        add(p, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        btns.add(new JButton(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                ok = true;
                setVisible(false);
            }
        }));
        btns.add(new JButton(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                ok = false; setVisible(false);
            }
        }));
        add(btns, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
    }

    public boolean isOk() { return ok; }
    public String getTableA() { return (String) boxA.getSelectedItem(); }
    public String getTableB() { return (String) boxB.getSelectedItem(); }
    public String getNewTableName() { return newNameField.getText().trim(); }
}
