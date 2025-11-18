package mew.pumlserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mew.pumlserver.dto.PumlResponse;
import mew.pumlserver.dto.RenderRequest;
import mew.pumlserver.dto.RenderResponse;
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

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Cache PUML code", description = "Caches PlantUML source code and returns cache ID. If the same PUML code already exists in cache, returns the existing ID. Otherwise, creates a new cache entry and returns a new ID.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<RenderResponse> cachePuml(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) {
    String id = cacheService.cachePumlCode(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity
        .ok()
        .headers(headers)
        .body(new RenderResponse(id));
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get PUML code by ID", description = "Retrieves the PlantUML source code by cache ID. The ID is returned when calling the /render API endpoints or POST /api/v1/puml.")
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

