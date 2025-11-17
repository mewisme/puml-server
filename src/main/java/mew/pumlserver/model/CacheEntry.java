package mew.pumlserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntry {
    private String id;
    private String puml;
    private LocalDateTime createdAt;
    private byte[] svgContent; // SVG format
    private byte[] pngContent; // PNG format
    private String textContent; // Text format
}

