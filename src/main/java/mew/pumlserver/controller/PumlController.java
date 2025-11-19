package mew.pumlserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mew.pumlserver.dto.ExplainRequest;
import mew.pumlserver.dto.ExplainResponse;
import mew.pumlserver.dto.GenerateRequest;
import mew.pumlserver.dto.GenerateResponse;
import mew.pumlserver.dto.OptimizeRequest;
import mew.pumlserver.dto.PumlResponse;
import mew.pumlserver.dto.RenderRequest;
import mew.pumlserver.dto.RenderResponse;
import mew.pumlserver.exception.NotFoundException;
import mew.pumlserver.model.CacheEntry;
import mew.pumlserver.model.Conversation;
import mew.pumlserver.service.ConversationService;
import mew.pumlserver.service.PumlGenerationService;
import mew.pumlserver.service.RenderCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/puml")
@Tag(name = "PUML", description = "PlantUML code retrieval APIs")
public class PumlController {

  private final RenderCacheService cacheService;
  private final PumlGenerationService generationService;
  private final ConversationService conversationService;

  public PumlController(RenderCacheService cacheService, PumlGenerationService generationService,
      ConversationService conversationService) {
    this.cacheService = cacheService;
    this.generationService = generationService;
    this.conversationService = conversationService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Cache PUML code", description = "Caches PlantUML source code and returns cache ID. If the same PUML code already exists in cache, returns the existing ID. Otherwise, creates a new cache entry and returns a new ID. The returned ID can be used with any other endpoint (GET /api/v1/puml/{id}, GET /api/v1/render/{type}/{id}/raw). Cache IDs are shared across all endpoints.")
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

  @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Generate PUML code using OpenAI", description = "Generates PlantUML code using OpenAI API based on a user prompt. The system automatically acts as a PlantUML expert. "
      +
      "Supports conversation context and streaming. " +
      "- If conversationId is provided, maintains context from previous messages. " +
      "- If conversationId is not provided, a new conversation will be created and its ID will be returned. " +
      "- If stream is true, returns Server-Sent Events (SSE) stream with text/event-stream content type. " +
      "- If stream is false, returns JSON response with generated PUML code and conversationId. " +
      "Conversations automatically expire after 30 minutes of inactivity.", responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully generated PUML code", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateResponse.class), examples = @ExampleObject(name = "Non-streaming response", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\",\"conversationId\":\"550e8400-e29b-41d4-a716-446655440000\"}"))),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Streaming response (when stream=true)", content = @Content(mediaType = "text/event-stream")),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found or expired (when conversationId is provided)")
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OpenAI generation request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateRequest.class), examples = {
      @ExampleObject(name = "New conversation (non-streaming)", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"prompt\":\"Create a sequence diagram showing user login flow\",\"stream\":false}"),
      @ExampleObject(name = "Continue conversation (streaming)", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"prompt\":\"Add error handling to the previous diagram\",\"conversationId\":\"550e8400-e29b-41d4-a716-446655440000\",\"stream\":true}")
  }))
  public ResponseEntity<?> generatePuml(
      @Valid @org.springframework.web.bind.annotation.RequestBody GenerateRequest request) {

    String conversationId = request.getConversationId();
    Conversation conversation;

    if (conversationId != null && !conversationId.isBlank()) {
      conversation = conversationService.getConversation(conversationId);
      if (conversation == null) {
        throw new NotFoundException("Conversation not found or expired. ID: " + conversationId);
      }
    } else {
      conversationId = conversationService.createConversation();
      conversation = conversationService.getConversation(conversationId);
    }

    final Conversation finalConversation = conversation;
    conversation.addMessage("user", request.getPrompt());

    if (Boolean.TRUE.equals(request.getStream())) {
      SseEmitter emitter = new SseEmitter(60000L);

      new Thread(() -> {
        try {
          StringBuilder fullContent = new StringBuilder();

          String generatedPuml = generationService.generatePumlCode(
              request.getBaseUrl(),
              request.getApiKey(),
              request.getModel(),
              request.getPrompt(),
              finalConversation);

          for (char c : generatedPuml.toCharArray()) {
            emitter.send(SseEmitter.event().data(String.valueOf(c)));
            fullContent.append(c);
            Thread.sleep(10);
          }

          finalConversation.addMessage("assistant", fullContent.toString());
          cacheService.cachePumlCode(fullContent.toString());

          emitter.complete();
        } catch (Exception e) {
          emitter.completeWithError(e);
        }
      }).start();

      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_EVENT_STREAM)
          .body(emitter);
    } else {
      String generatedPuml = generationService.generatePumlCode(
          request.getBaseUrl(),
          request.getApiKey(),
          request.getModel(),
          request.getPrompt(),
          conversation);

      conversation.addMessage("assistant", generatedPuml);
      cacheService.cachePumlCode(generatedPuml);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      return ResponseEntity
          .ok()
          .headers(headers)
          .body(new GenerateResponse(generatedPuml, conversationId));
    }
  }

  @PostMapping(value = "/optimize", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Optimize PUML code using OpenAI", description = "Optimizes PlantUML code using OpenAI API. The system automatically acts as a PlantUML optimization expert. "
      +
      "Supports streaming. " +
      "- If stream is true, returns Server-Sent Events (SSE) stream with text/event-stream content type. " +
      "- If stream is false, returns JSON response with optimized PUML code. " +
      "This endpoint does not maintain conversation context.", responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully optimized PUML code", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PumlResponse.class), examples = @ExampleObject(name = "Non-streaming response", value = "{\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\"}"))),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Streaming response (when stream=true)", content = @Content(mediaType = "text/event-stream"))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OpenAI optimization request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = OptimizeRequest.class), examples = {
      @ExampleObject(name = "Non-streaming", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\",\"stream\":false}"),
      @ExampleObject(name = "Streaming", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\",\"stream\":true}")
  }))
  public ResponseEntity<?> optimizePuml(
      @Valid @org.springframework.web.bind.annotation.RequestBody OptimizeRequest request) {

    if (Boolean.TRUE.equals(request.getStream())) {
      SseEmitter emitter = new SseEmitter(60000L);

      new Thread(() -> {
        try {
          StringBuilder fullContent = new StringBuilder();

          String optimizedPuml = generationService.optimizePumlCode(
              request.getBaseUrl(),
              request.getApiKey(),
              request.getModel(),
              request.getPuml());

          for (char c : optimizedPuml.toCharArray()) {
            emitter.send(SseEmitter.event().data(String.valueOf(c)));
            fullContent.append(c);
            Thread.sleep(10);
          }

          cacheService.cachePumlCode(fullContent.toString());

          emitter.complete();
        } catch (Exception e) {
          emitter.completeWithError(e);
        }
      }).start();

      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_EVENT_STREAM)
          .body(emitter);
    } else {
      String optimizedPuml = generationService.optimizePumlCode(
          request.getBaseUrl(),
          request.getApiKey(),
          request.getModel(),
          request.getPuml());

      cacheService.cachePumlCode(optimizedPuml);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      return ResponseEntity
          .ok()
          .headers(headers)
          .body(new PumlResponse(optimizedPuml));
    }
  }

  @PostMapping(value = "/explain", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Explain PUML code using OpenAI", description = "Explains what a PlantUML diagram does using OpenAI API. The system automatically acts as a PlantUML explanation expert. "
      +
      "Supports streaming and multiple languages. " +
      "- If stream is true, returns Server-Sent Events (SSE) stream with text/event-stream content type. " +
      "- If stream is false, returns JSON response with explanation text. " +
      "- Language parameter controls the language of the explanation (e.g., 'en' for English, 'vi' for Vietnamese). " +
      "This endpoint does not maintain conversation context.", responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully explained PUML code", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExplainResponse.class), examples = @ExampleObject(name = "Non-streaming response", value = "{\"explanation\":\"This diagram shows a simple sequence diagram where Bob sends a 'hello' message to Alice.\"}"))),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Streaming response (when stream=true)", content = @Content(mediaType = "text/event-stream"))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OpenAI explanation request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExplainRequest.class), examples = {
      @ExampleObject(name = "Non-streaming (English)", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\",\"language\":\"en\",\"stream\":false}"),
      @ExampleObject(name = "Streaming (Vietnamese)", value = "{\"baseUrl\":\"https://api.openai.com/v1\",\"apiKey\":\"sk-...\",\"model\":\"gpt-4\",\"puml\":\"@startuml\\n\\nBob -> Alice : hello\\n\\n@enduml\",\"language\":\"vi\",\"stream\":true}")
  }))
  public ResponseEntity<?> explainPuml(
      @Valid @org.springframework.web.bind.annotation.RequestBody ExplainRequest request) {

    String language = request.getLanguage();
    if (language == null || language.isBlank()) {
      language = "en";
    }
    final String finalLanguage = language;

    if (Boolean.TRUE.equals(request.getStream())) {
      SseEmitter emitter = new SseEmitter(60000L);

      new Thread(() -> {
        try {
          StringBuilder fullContent = new StringBuilder();

          String explanation = generationService.explainPumlCode(
              request.getBaseUrl(),
              request.getApiKey(),
              request.getModel(),
              request.getPuml(),
              finalLanguage);

          for (char c : explanation.toCharArray()) {
            emitter.send(SseEmitter.event().data(String.valueOf(c)));
            fullContent.append(c);
            Thread.sleep(10);
          }

          emitter.complete();
        } catch (Exception e) {
          emitter.completeWithError(e);
        }
      }).start();

      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_EVENT_STREAM)
          .body(emitter);
    } else {
      String explanation = generationService.explainPumlCode(
          request.getBaseUrl(),
          request.getApiKey(),
          request.getModel(),
          request.getPuml(),
          finalLanguage);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      return ResponseEntity
          .ok()
          .headers(headers)
          .body(new ExplainResponse(explanation));
    }
  }

  @DeleteMapping(value = "/conversation/{conversationId}")
  @Operation(summary = "Delete conversation", description = "Deletes a conversation by ID. The conversation and all its context will be removed permanently.", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation deleted successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Success response", value = "{\"message\":\"Conversation deleted successfully\",\"conversationId\":\"550e8400-e29b-41d4-a716-446655440000\"}"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
  })
  @io.swagger.v3.oas.annotations.Parameter(name = "conversationId", description = "ID of the conversation to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
  public ResponseEntity<Map<String, String>> deleteConversation(@PathVariable String conversationId) {
    boolean deleted = conversationService.deleteConversation(conversationId);

    if (!deleted) {
      throw new NotFoundException("Conversation not found. ID: " + conversationId);
    }

    return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully", "conversationId", conversationId));
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get PUML code by ID", description = "Retrieves the PlantUML source code by cache ID. The ID can be obtained from any endpoint that returns an ID (POST /api/v1/puml, POST /api/v1/render/svg, POST /api/v1/render/png, POST /api/v1/render/text). Cache IDs are shared across all endpoints.")
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
