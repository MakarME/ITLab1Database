package org.example.model.dialogs;

import org.example.model.Field;
import org.example.model.ComplexInteger;
import org.example.model.ComplexReal;
import org.example.model.Record;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class EditRowDialog extends JDialog {
    private boolean ok=false;
    private final List<JTextField> editors = new ArrayList<>();
    private final List<Field> schema;

    public EditRowDialog(Frame owner, List<Field> schema, Record r){
        super(owner, "Edit row", true);
        this.schema = schema;
        setSize(600,300);
        setLayout(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(schema.size(),2));
        for(int i=0;i<schema.size();i++){
            Field f = schema.get(i);
            grid.add(new JLabel(f.getName()+" ("+f.getType()+")"));
            JTextField tf = new JTextField();
            if(r!=null){ Object v = r.get(i); if(v!=null) tf.setText(v.toString()); }
            editors.add(tf);
            grid.add(tf);
        }
        add(new JScrollPane(grid), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(new JButton(new AbstractAction("OK"){ public void actionPerformed(ActionEvent e){ ok=true; setVisible(false); }}));
        btns.add(new JButton(new AbstractAction("Cancel"){ public void actionPerformed(ActionEvent e){ ok=false; setVisible(false); }}));
        add(btns, BorderLayout.SOUTH);
    }

    public boolean isOk(){ return ok; }

    public List<Object> getParsedValues() throws Exception{
        List<Object> out = new ArrayList<>();
        for(int i=0;i<schema.size();i++){
            Field f = schema.get(i);
            String s = editors.get(i).getText().trim();
            if(s.isEmpty()){
                if(f.isNullable()) out.add(null);
                else throw new Exception("Field '"+f.getName()+"' is required");
                continue;
            }
            switch(f.getType()){
                case INTEGER: out.add(Long.parseLong(s)); break;
                case REAL: out.add(Double.parseDouble(s)); break;
                case CHAR: if(s.length()!=1) throw new Exception("Field '"+f.getName()+"' expects single char"); out.add(s.charAt(0)); break;
                case STRING: out.add(s); break;
                case COMPLEX_INTEGER: {
                    String[] parts = s.replace(" ","").split("[+,]", -1);
                    if(parts.length<2) throw new Exception("Bad complex integer format");
                    long a = Long.parseLong(parts[0]);
                    long b = Long.parseLong(parts[1].replace("i",""));
                    out.add(new ComplexInteger(a,b)); break;
                }
                case COMPLEX_REAL: {
                    String[] parts = s.replace(" ","").split("[+,]", -1);
                    if(parts.length<2) throw new Exception("Bad complex real format");
                    double a = Double.parseDouble(parts[0]);
                    double b = Double.parseDouble(parts[1].replace("i",""));
                    out.add(new ComplexReal(a,b)); break;
                }
                default: out.add(s);
            }
        }
        return out;
    }
}