package dev.paramountdev.zlomCore_PDev.avik;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Task {
    public String title;
    public String description;
    public double price;
    public UUID author;

    public Task(String title, String description, double price, UUID author) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.author = author;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("price", price);
        map.put("author", author.toString());
        return map;
    }

    public static Task deserialize(Map<String, Object> map) {
        return new Task(
                (String) map.get("title"),
                (String) map.get("description"),
                (double) map.get("price"),
                UUID.fromString((String) map.get("author"))
        );
    }
}

