package mew.pumlserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mew.pumlserver.dto.RenderRequest;
import mew.pumlserver.dto.RenderResponse;
import mew.pumlserver.exception.NotFoundException;
import mew.pumlserver.model.CacheEntry;
import mew.pumlserver.service.RenderCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/render")
@Tag(name = "Render", description = "PlantUML rendering APIs")
public class RenderController {

  private final RenderCacheService cacheService;

  public RenderController(RenderCacheService cacheService) {
    this.cacheService = cacheService;
  }

  @PostMapping(value = "/svg", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Render PUML to SVG", description = "Converts PlantUML diagram to SVG format and returns cache ID. The same ID can be used to retrieve SVG, PNG, or Text formats.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<RenderResponse> renderSvg(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    String id = cacheService.cacheAllFormats(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity
        .ok()
        .headers(headers)
        .body(new RenderResponse(id));
  }

  @PostMapping(value = "/png", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Render PUML to PNG", description = "Converts PlantUML diagram to PNG format and returns cache ID. The same ID can be used to retrieve SVG, PNG, or Text formats.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<RenderResponse> renderPng(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    String id = cacheService.cacheAllFormats(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity
        .ok()
        .headers(headers)
        .body(new RenderResponse(id));
  }

  @PostMapping(value = "/text", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Render PUML to Text", description = "Converts PlantUML diagram to plain text format and returns cache ID. The same ID can be used to retrieve SVG, PNG, or Text formats.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<RenderResponse> renderText(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    String id = cacheService.cacheAllFormats(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity.ok()
        .headers(headers)
        .body(new RenderResponse(id));
  }

  @GetMapping(value = "/{type}/{id}/raw")
  @Operation(summary = "Get rendered content by ID", description = "Retrieves cached rendered content by ID and format type. The same ID can be used to retrieve SVG, PNG, or Text formats. Content expires after 30 minutes.")
  public ResponseEntity<?> getRawContent(
      @PathVariable String type,
      @PathVariable String id) {
    CacheEntry entry = cacheService.getCachedEntry(id);

    if (entry == null) {
      throw new NotFoundException("Rendered content not found or expired. ID: " + id);
    }

    HttpHeaders headers = new HttpHeaders();

    if ("text".equals(type)) {
      if (entry.getTextContent() == null) {
        throw new NotFoundException("Text content not available for ID: " + id);
      }
      headers.setContentType(MediaType.TEXT_PLAIN);
      return ResponseEntity.ok()
          .headers(headers)
          .body(entry.getTextContent());
    } else if ("svg".equals(type)) {
      if (entry.getSvgContent() == null) {
        throw new NotFoundException("SVG content not available for ID: " + id);
      }
      headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
      return ResponseEntity.ok()
          .headers(headers)
          .body(entry.getSvgContent());
    } else if ("png".equals(type)) {
      if (entry.getPngContent() == null) {
        throw new NotFoundException("PNG content not available for ID: " + id);
      }
      headers.setContentType(MediaType.IMAGE_PNG);
      return ResponseEntity.ok()
          .headers(headers)
          .body(entry.getPngContent());
    } else {
      throw new NotFoundException("Invalid content type: " + type + ". Supported types: svg, png, text");
    }
  }
}
