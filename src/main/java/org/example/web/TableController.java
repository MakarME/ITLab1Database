package org.example.web;

import org.example.model.Field;
import org.example.model.Record;
import org.example.model.exceptions.ValidationException;
import org.example.model.requests.CreateTableRequest;
import org.example.model.requests.MergeRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class TableController {
    private final DatabaseService svc;

    public TableController(DatabaseService svc) {
        this.svc = svc;
    }

    @GetMapping("/tables")
    public Set<String> list() {
        return svc.listTables();
    }

    @PostMapping("/tables")
    public ResponseEntity<?> create(@RequestBody CreateTableRequest req) {
        try {
            svc.createTable(req.getName(), req.getSchema());
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/tables/{name}")
    public ResponseEntity<?> drop(@PathVariable String name) {
        svc.dropTable(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tables/{name}/rows")
    public List<org.example.model.Record> rows(@PathVariable("name") String name){
        return svc.getRows(name);
    }

    @PostMapping("/tables/{name}/rows")
    public ResponseEntity<?> insert(@PathVariable String name, @RequestBody List<Object> values) {
        try {
            svc.insertRow(name, new org.example.model.Record(new ArrayList<>(values)));
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/tables/{name}/rows/{index}")
    public ResponseEntity<?> update(
            @PathVariable String name,
            @PathVariable int index,
            @RequestBody List<Object> values) {
        try {
            svc.updateRow(name, index, new Record(new ArrayList<>(values)));
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/tables/{name}/rows/{index}")
    public ResponseEntity<?> deleteRow(
            @PathVariable("name") String name,
            @PathVariable("index") int index){
        svc.deleteRow(name, index);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tables/merge")
    public ResponseEntity<?> merge(@RequestBody MergeRequest req) {
        try {
            svc.mergeTables(req.getA(), req.getB(), req.getNewName());
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/tables/{name}/importCsv")
    public ResponseEntity<?> importCsv(
            @PathVariable("name") String name,
            @RequestParam("file") MultipartFile file) {
        try {
            svc.importCsv(name, file.getInputStream());
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/tables/{name}/exportCsv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable String name) {
        try {
            byte[] data = svc.exportCsv(name);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".csv\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tables/{name}/schema")
    public List<Field> schema(@PathVariable String name) {
        return svc.getTable(name).getSchema();
    }

    @GetMapping("/db/export")
    public ResponseEntity<Map<String, Object>> exportDatabase() {
        return ResponseEntity.ok(svc.exportFull());
    }

    @PostMapping("/db/load")
    public ResponseEntity<Void> loadDatabase(@RequestBody Map<String, Object> dump) throws ValidationException {
        svc.loadFull(dump);
        return ResponseEntity.ok().build();
    }
}