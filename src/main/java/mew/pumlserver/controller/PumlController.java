package mew.pumlserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mew.pumlserver.dto.PumlResponse;
import mew.pumlserver.exception.NotFoundException;
import mew.pumlserver.model.CacheEntry;
import mew.pumlserver.service.RenderCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/puml")
@Tag(name = "PUML", description = "PlantUML code retrieval APIs")
public class PumlController {

  private final RenderCacheService cacheService;

  public PumlController(RenderCacheService cacheService) {
    this.cacheService = cacheService;
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get PUML code by ID", description = "Retrieves the PlantUML source code by cache ID. The ID is returned when calling the /render API endpoints.")
  public ResponseEntity<PumlResponse> getPumlById(@PathVariable String id) {
    CacheEntry entry = cacheService.getCachedEntry(id);

    if (entry == null) {
      throw new NotFoundException("PUML code not found or expired. ID: " + id);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity
        .ok()
        .headers(headers)
        .body(new PumlResponse(entry.getPuml()));
  }
}

