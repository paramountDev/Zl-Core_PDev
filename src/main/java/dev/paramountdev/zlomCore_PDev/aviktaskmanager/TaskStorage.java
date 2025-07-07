package dev.paramountdev.zlomCore_PDev.aviktaskmanager;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TaskStorage {

    private final File file;
    private final YamlConfiguration config;

    public TaskStorage(File dataFolder) {
        this.file = new File(dataFolder, "tasks.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        if (config.contains("tasks")) {
            for (String key : config.getConfigurationSection("tasks").getKeys(false)) {
                Map<String, Object> data = config.getConfigurationSection("tasks." + key).getValues(false);
                tasks.add(Task.deserialize(data));
            }
        }
        return tasks;
    }

    public void saveTasks(List<Task> tasks) {
        config.set("tasks", null);
        int index = 0;
        for (Task task : tasks) {
            config.createSection("tasks." + index++, task.serialize());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
