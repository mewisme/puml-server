package mew.pumlserver.service;

import mew.pumlserver.model.CacheEntry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RenderCacheService {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MINUTES = 30;
    private final RenderService renderService;

    public RenderCacheService(RenderService renderService) {
        this.renderService = renderService;
    }

    /**
     * Cache all rendered formats (SVG, PNG, Text) for a PUML diagram and return unique ID.
     * If an entry with the same PUML already exists:
     * - If it has rendered content, return its ID
     * - If it doesn't have rendered content, render and update the entry, then return its ID
     * Otherwise, create a new cache entry with all formats.
     */
    public String cacheAllFormats(String puml) throws IOException {
        // Check if this PUML is already cached
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            CacheEntry cachedEntry = entry.getValue();
            if (cachedEntry.getPuml().equals(puml) && !isExpired(cachedEntry)) {
                // If entry already has rendered content, return existing ID
                if (cachedEntry.getSvgContent() != null && cachedEntry.getPngContent() != null 
                    && cachedEntry.getTextContent() != null) {
                    return entry.getKey();
                }
                // If entry exists but doesn't have rendered content, render and update it
                byte[] svg = renderService.renderSvg(puml);
                byte[] png = renderService.renderPng(puml);
                String text = renderService.renderText(puml);
                cachedEntry.setSvgContent(svg);
                cachedEntry.setPngContent(png);
                cachedEntry.setTextContent(text);
                return entry.getKey();
            }
        }

        // Render all formats
        byte[] svg = renderService.renderSvg(puml);
        byte[] png = renderService.renderPng(puml);
        String text = renderService.renderText(puml);

        // Create new cache entry with all formats
        String id = UUID.randomUUID().toString();
        CacheEntry entry = new CacheEntry(id, puml, LocalDateTime.now(), svg, png, text);
        cache.put(id, entry);
        return id;
    }

    /**
     * Cache PUML code only (without rendering) and return unique ID.
     * If an entry with the same PUML already exists, return its ID instead of creating a new one.
     */
    public String cachePumlCode(String puml) {
        // Check if this PUML is already cached
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getPuml().equals(puml) && !isExpired(entry.getValue())) {
                return entry.getKey(); // Return existing ID
            }
        }

        // Create new cache entry with only PUML code (no rendering)
        String id = UUID.randomUUID().toString();
        CacheEntry entry = new CacheEntry(id, puml, LocalDateTime.now(), null, null, null);
        cache.put(id, entry);
        return id;
    }

    /**
     * Get cached entry by ID
     */
    public CacheEntry getCachedEntry(String id) {
        CacheEntry entry = cache.get(id);
        if (entry == null) {
            return null;
        }
        
        // Check if expired
        if (isExpired(entry)) {
            cache.remove(id);
            return null;
        }
        
        return entry;
    }

    /**
     * Ensure rendered content exists for a cache entry. If the entry doesn't have rendered content,
     * render all formats and update the entry.
     * 
     * @param entry The cache entry to ensure rendered content for
     * @throws IOException if rendering fails
     */
    public void ensureRenderedContent(CacheEntry entry) throws IOException {
        if (entry == null) {
            return;
        }
        
        // If entry already has all rendered content, no need to render
        if (entry.getSvgContent() != null && entry.getPngContent() != null 
            && entry.getTextContent() != null) {
            return;
        }
        
        // Render all formats and update entry
        byte[] svg = renderService.renderSvg(entry.getPuml());
        byte[] png = renderService.renderPng(entry.getPuml());
        String text = renderService.renderText(entry.getPuml());
        entry.setSvgContent(svg);
        entry.setPngContent(png);
        entry.setTextContent(text);
    }

    /**
     * Check if entry is expired (older than 30 minutes)
     */
    private boolean isExpired(CacheEntry entry) {
        return entry.getCreatedAt().plusMinutes(CACHE_TTL_MINUTES).isBefore(LocalDateTime.now());
    }

    /**
     * Clean up expired entries every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    /**
     * Get cache size (for monitoring)
     */
    public int getCacheSize() {
        return cache.size();
    }
}

