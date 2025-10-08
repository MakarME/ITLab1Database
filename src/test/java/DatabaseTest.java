import org.example.model.*;
import org.example.model.Record;
import org.example.model.exceptions.ValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DatabaseTest {

    @Test
    public void testCreateTableInsertAndValidate() throws Exception {
        Database db = new Database();
        db.createTable("persons", Arrays.asList(
                new Field("id", FieldType.INTEGER, false),
                new Field("name", FieldType.STRING, false),
                new Field("age", FieldType.INTEGER, true)
        ));
        Table t = db.getTable("persons");
        Record r = new Record(Arrays.asList(1, "Alice", 30));
        t.insert(r);
        Assert.assertEquals(1, t.getRows().size());
        Assert.assertEquals("Alice", t.getRows().get(0).get(1));
    }

    @Test(expected = ValidationException.class)
    public void testCharValidation() throws Exception {
        Database db = new Database();
        db.createTable("symbols", Arrays.asList(
                new Field("c", FieldType.CHAR, false, 1)
        ));
        Table t = db.getTable("symbols");
        Record r = new Record(Arrays.asList("ab"));
        t.insert(r);
    }

    @Test
    public void testMergeTables() throws Exception {
        Database db = new Database();
        db.createTable("a", Arrays.asList(
                new Field("id", FieldType.INTEGER, false),
                new Field("val", FieldType.STRING, true)
        ));
        db.createTable("b", Arrays.asList(
                new Field("id", FieldType.INTEGER, false),
                new Field("val", FieldType.STRING, true)
        ));
        Table ta = db.getTable("a");
        Table tb = db.getTable("b");
        ta.insert(new Record(Arrays.asList(1, "x")));
        tb.insert(new Record(Arrays.asList(2, "y")));

        db.mergeTables("a","b","ab");
        Table tab = db.getTable("ab");
        Assert.assertEquals(2, tab.getRows().size());
    }
}
