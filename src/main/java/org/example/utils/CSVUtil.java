package org.example.utils;

import org.example.model.Field;
import org.example.model.Record;
import org.example.model.Table;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {

    public static void exportTableToCSVStream(Table table, OutputStream out) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // header
            boolean first = true;
            for (Field fld : table.getSchema()) {
                if (!first) w.write(',');
                first = false;
                w.write(escape(fld.getName()));
            }
            w.write('\n');
            // rows
            for (Record r : table.getRows()) {
                first = true;
                for (Object v : r.getValues()) {
                    if (!first) w.write(',');
                    first = false;
                    w.write(escape(v == null ? "" : v.toString()));
                }
                w.write('\n');
            }
            w.flush();
        }
    }


    public static void exportTableToCSV(Table table, File f) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))){
            // header
            boolean first=true;
            for(Field fld: table.getSchema()){
                if(!first) w.write(','); first=false;
                w.write(escape(fld.getName()));
            }
            w.write('\n');
            for(Record r: table.getRows()){
                first=true;
                for(Object v: r.getValues()){
                    if(!first) w.write(','); first=false;
                    w.write(escape(v==null?"":v.toString()));
                }
                w.write('\n');
            }
        }
    }

    public static List<List<String>> importCSV(File f) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(f))){
            String line;
            while((line = r.readLine()) != null){
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    public static List<List<String>> importCSV(InputStream in) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); // escaped quote
                        i++;
                    } else {
                        inQuotes = false; // end quote
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            String t = s.replace("\"", "\"\"");
            return "\"" + t + "\"";
        }
        return s;
    }
    private static String unescape(String s){
        s = s.trim();
        if(s.startsWith("\"") && s.endsWith("\"")){
            String core = s.substring(1, s.length()-1);
            return core.replace("\"\"","\"");
        }
        return s;
    }
}