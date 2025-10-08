package org.example.utils;

import org.example.model.ComplexInteger;
import org.example.model.ComplexReal;
import org.example.model.Field;

import java.util.ArrayList;
import java.util.List;

public class ParseUtil {

    public static List<Object> parseStringRowToObjects(List<String> strs, List<Field> schema) throws Exception {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < schema.size(); i++) {
            Field f = schema.get(i);
            String s = i < strs.size() ? strs.get(i) : "";
            s = s == null ? "" : s.trim();
            if (s.isEmpty()) {
                if (f.isNullable()) {
                    out.add(null);
                    continue;
                } else {
                    throw new Exception("Field '" + f.getName() + "' is required");
                }
            }
            switch (f.getType()) {
                case INTEGER: {
                    // поддержим запятую как разделитель десятичной части, но целое ожидается
                    String norm = s.replace(',', '.');
                    if (norm.contains(".")) {
                        // берем целую часть до точки
                        norm = norm.split("\\.")[0];
                    }
                    // пробуем Integer или Long
                    try {
                        long lv = Long.parseLong(norm);
                        // если помещается в Integer, можно вернуть Integer, но в модели мы допускаем Long
                        out.add(lv);
                    } catch (NumberFormatException ex) {
                        throw new Exception("Cannot parse integer for field '" + f.getName() + "': " + s);
                    }
                    break;
                }
                case REAL: {
                    try {
                        double dv = Double.parseDouble(s.replace(',', '.'));
                        out.add(dv);
                    } catch (NumberFormatException ex) {
                        throw new Exception("Cannot parse real for field '" + f.getName() + "': " + s);
                    }
                    break;
                }
                case CHAR: {
                    if (s.length() != 1) throw new Exception("Field '" + f.getName() + "' expects single char");
                    out.add(s.charAt(0));
                    break;
                }
                case STRING: {
                    out.add(s);
                    break;
                }
                case COMPLEX_INTEGER: {
                    double[] parts = parseComplexToDoubles(s);
                    long a = (long) parts[0];
                    long b = (long) parts[1];
                    out.add(new ComplexInteger(a, b));
                    break;
                }
                case COMPLEX_REAL: {
                    double[] parts = parseComplexToDoubles(s);
                    out.add(new ComplexReal(parts[0], parts[1]));
                    break;
                }
                default:
                    out.add(s);
            }
        }
        return out;
    }

    /**
     * Парсит комплексную строку, вроде:
     *  3+4i
     *  0+1i
     *  1,500000+-2,250000i
     *  1,000000+0,000000i
     *
     * Возвращает double[]{real, imag}
     */
    public static double[] parseComplexToDoubles(String raw) throws Exception {
        if (raw == null) throw new Exception("Empty complex");
        String s = raw.trim();
        if (s.endsWith("i") || s.endsWith("I")) {
            s = s.substring(0, s.length() - 1).trim();
        } else {
            throw new Exception("Complex must end with 'i': " + raw);
        }
        s = s.replace(',', '.');

        // Найти разделитель (первый + или - после позиции 0)
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
        String imagPart = s.substring(sep).trim();

        if (realPart.isEmpty() || imagPart.isEmpty()) throw new Exception("Bad complex format: " + raw);

        // Нормализация случаев: "+-2.25" -> "-2.25", "-+2.25" -> "+2.25", "++2.25" -> "+2.25", "--2.25" -> "-2.25"
        if (imagPart.length() >= 2) {
            char first = imagPart.charAt(0);
            char second = imagPart.charAt(1);
            if ((first == '+' || first == '-') && (second == '+' || second == '-')) {
                if (first == '+' && second == '-') {
                    imagPart = imagPart.substring(1); // "+-x" -> "-x"
                } else if (first == '+' && second == '+') {
                    imagPart = imagPart.substring(1); // "++x" -> "+x"
                } else if (first == '-' && second == '+') {
                    imagPart = imagPart.substring(1); // "-+x" -> "+x"
                } else if (first == '-' && second == '-') {
                    imagPart = "-" + imagPart.substring(2); // "--x" -> "-x"
                }
            }
        }

        double real = Double.parseDouble(realPart);
        double imag = Double.parseDouble(imagPart);
        return new double[]{real, imag};
    }
}
