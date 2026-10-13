package com.example.filesystem.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;

/** Decides which files can be previewed in the browser. */
public final class FileTypes {
    private static final Set<String> TEXT = Set.of("txt", "java", "json", "csv", "md", "xml", "log",
        "properties", "yml", "yaml", "js", "py", "html", "css", "sql");
    private static final Map<String, MediaType> IMAGE = Map.of(
        "png", MediaType.IMAGE_PNG, "jpg", MediaType.IMAGE_JPEG, "jpeg", MediaType.IMAGE_JPEG,
        "gif", MediaType.IMAGE_GIF);

    private FileTypes() { }

    public static String ext(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }
    /** "pdf", "image", "text" or null when not previewable. */
    public static String previewType(String name) {
        String e = ext(name);
        if (e.equals("pdf")) return "pdf";
        if (IMAGE.containsKey(e)) return "image";
        if (TEXT.contains(e)) return "text";
        return null;
    }
    public static MediaType previewMediaType(String name) {
        String t = previewType(name);
        if (t == null) return null;
        if (t.equals("pdf")) return MediaType.APPLICATION_PDF;
        if (t.equals("image")) return IMAGE.get(ext(name));
        return new MediaType("text", "plain", StandardCharsets.UTF_8); // never served as HTML
    }
}
