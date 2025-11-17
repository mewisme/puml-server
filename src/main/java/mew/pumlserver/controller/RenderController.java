package mew.pumlserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mew.pumlserver.dto.RenderRequest;
import mew.pumlserver.service.RenderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/render")
@Tag(name = "Render", description = "PlantUML rendering APIs")
public class RenderController {

  private final RenderService renderService;

  public RenderController(RenderService renderService) {
    this.renderService = renderService;
  }

  @PostMapping(value = "/svg", produces = "image/svg+xml")
  @Operation(summary = "Render PUML to SVG", description = "Converts PlantUML diagram to SVG format")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<byte[]> renderSvg(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    byte[] svg = renderService.renderSvg(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
    return ResponseEntity.ok()
        .headers(headers)
        .body(svg);
  }

  @PostMapping(value = "/png", produces = MediaType.IMAGE_PNG_VALUE)
  @Operation(summary = "Render PUML to PNG", description = "Converts PlantUML diagram to PNG format")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<byte[]> renderPng(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    byte[] png = renderService.renderPng(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    return ResponseEntity.ok()
        .headers(headers)
        .body(png);
  }

  @PostMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(summary = "Render PUML to Text", description = "Converts PlantUML diagram to plain text format")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PlantUML diagram source code", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenderRequest.class), examples = @ExampleObject(name = "Example PUML", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}")))
  public ResponseEntity<String> renderText(
      @Valid @org.springframework.web.bind.annotation.RequestBody RenderRequest request) throws IOException {
    String text = renderService.renderText(request.getPuml());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    return ResponseEntity.ok()
        .headers(headers)
        .body(text);
  }
}
