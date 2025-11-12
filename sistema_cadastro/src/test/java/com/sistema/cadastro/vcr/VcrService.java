package com.sistema.cadastro.vcr;

import com.easypost.easyvcr.Cassette;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VcrService {

    public static Path cassetteDir() {
        Path dir = Paths.get("src", "test", "cassettes");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {}
        }
        return dir;
    }

    public static Cassette newCassette(String name) {
        return new Cassette(cassetteDir().toString(), name);
    }

    public static boolean cassetteExists(String name) {
        Path json = cassetteDir().resolve(name + ".json");
        Path yaml = cassetteDir().resolve(name + ".yaml");
        return Files.exists(json) || Files.exists(yaml);
    }

    public static String readCassetteContent(String name) throws IOException {
        Path json = cassetteDir().resolve(name + ".json");
        Path yaml = cassetteDir().resolve(name + ".yaml");
        Path file = Files.exists(json) ? json : yaml;
        if (file == null || !Files.exists(file)) {
            return "";
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public static void deleteCassette(String name) {
        try {
            Path json = cassetteDir().resolve(name + ".json");
            Path yaml = cassetteDir().resolve(name + ".yaml");
            if (Files.exists(json)) Files.delete(json);
            if (Files.exists(yaml)) Files.delete(yaml);
        } catch (IOException ignored) {}
    }
}
