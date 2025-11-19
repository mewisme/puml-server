package mew.pumlserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mew.pumlserver.model.Conversation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PumlGenerationService {

  private static final String SYSTEM_MESSAGE = "You are an expert in creating PlantUML diagrams. " +
      "Your task is to generate valid PlantUML code based on user requests. " +
      "Always return only the PlantUML code without any explanations, markdown formatting, or additional text. " +
      "The code must start with @startuml and end with @enduml. " +
      "Make sure the PlantUML code is complete, valid, and ready to use.";

  private static final String OPTIMIZE_SYSTEM_MESSAGE = "You are an expert in optimizing PlantUML diagrams. " +
      "Your task is to optimize and improve PlantUML code provided by users. " +
      "Optimize the code for better readability, maintainability, and best practices. " +
      "Always return only the optimized PlantUML code without any explanations, markdown formatting, or additional text. "
      +
      "The code must start with @startuml and end with @enduml. " +
      "Make sure the optimized PlantUML code is complete, valid, and ready to use.";

  private static String getExplainSystemMessage(String language) {
    String baseMessage = "You are an expert in PlantUML diagrams. " +
        "Your task is to explain what a PlantUML diagram does in a clear and concise manner. " +
        "Explain the purpose, flow, and key components of the diagram. " +
        "Return only the explanation text without any markdown formatting or additional text.";

    if ("vi".equalsIgnoreCase(language) || "vietnamese".equalsIgnoreCase(language)) {
      return "Bạn là chuyên gia về biểu đồ PlantUML. " +
          "Nhiệm vụ của bạn là giải thích biểu đồ PlantUML một cách rõ ràng và ngắn gọn bằng tiếng Việt. " +
          "Giải thích mục đích, luồng và các thành phần chính của biểu đồ. " +
          "Chỉ trả về văn bản giải thích, không có định dạng markdown hoặc văn bản bổ sung.";
    }

    return baseMessage;
  }

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public PumlGenerationService() {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  public String generatePumlCode(String baseUrl, String apiKey, String model, String prompt,
      Conversation conversation) {
    String url = baseUrl + "/chat/completions";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", model);
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", 2000);
    requestBody.put("stream", false);

    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", SYSTEM_MESSAGE));

    if (conversation != null && conversation.getMessages() != null) {
      for (Conversation.Message msg : conversation.getMessages()) {
        messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
      }
    }

    messages.add(Map.of("role", "user", "content", prompt));
    requestBody.put("messages", messages);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        request,
        String.class);

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      String content = jsonNode.get("choices")
          .get(0)
          .get("message")
          .get("content")
          .asText();

      return cleanPumlCode(content);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
    }
  }

  public void streamPumlCode(String baseUrl, String apiKey, String model, String prompt,
      Conversation conversation, SseEmitter emitter) {
    String url = baseUrl + "/chat/completions";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", model);
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", 2000);
    requestBody.put("stream", true);

    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", SYSTEM_MESSAGE));

    if (conversation != null && conversation.getMessages() != null) {
      for (Conversation.Message msg : conversation.getMessages()) {
        messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
      }
    }

    messages.add(Map.of("role", "user", "content", prompt));
    requestBody.put("messages", messages);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          url,
          HttpMethod.POST,
          request,
          String.class);

      String responseBody = response.getBody();
      if (responseBody != null) {
        String[] lines = responseBody.split("\n");
        StringBuilder fullContent = new StringBuilder();

        for (String line : lines) {
          if (line.startsWith("data: ")) {
            String data = line.substring(6).trim();
            if (data.equals("[DONE]")) {
              break;
            }

            try {
              JsonNode jsonNode = objectMapper.readTree(data);
              if (jsonNode.has("choices") && jsonNode.get("choices").size() > 0) {
                JsonNode delta = jsonNode.get("choices").get(0).get("delta");
                if (delta.has("content")) {
                  String content = delta.get("content").asText();
                  fullContent.append(content);
                  emitter.send(SseEmitter.event().data(content));
                }
              }
            } catch (Exception e) {
            }
          }
        }

        emitter.complete();
      }
    } catch (Exception e) {
      try {
        emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
      } catch (IOException ioException) {
      }
      emitter.completeWithError(e);
    }
  }

  public String optimizePumlCode(String baseUrl, String apiKey, String model, String puml) {
    String url = baseUrl + "/chat/completions";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", model);
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", 2000);
    requestBody.put("stream", false);

    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", OPTIMIZE_SYSTEM_MESSAGE));

    String userMessage = "Please optimize the following PlantUML code:\n\n" + puml;
    messages.add(Map.of("role", "user", "content", userMessage));
    requestBody.put("messages", messages);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        request,
        String.class);

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      String content = jsonNode.get("choices")
          .get(0)
          .get("message")
          .get("content")
          .asText();

      return cleanPumlCode(content);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
    }
  }

  public String explainPumlCode(String baseUrl, String apiKey, String model, String puml, String language) {
    String url = baseUrl + "/chat/completions";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", model);
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", 2000);
    requestBody.put("stream", false);

    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", getExplainSystemMessage(language)));

    String userMessage = "Please explain what the following PlantUML code does:\n\n" + puml;
    messages.add(Map.of("role", "user", "content", userMessage));
    requestBody.put("messages", messages);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        request,
        String.class);

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      String content = jsonNode.get("choices")
          .get(0)
          .get("message")
          .get("content")
          .asText();

      return content.trim();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
    }
  }

  private String cleanPumlCode(String content) {
    content = content.trim();
    if (content.startsWith("```")) {
      content = content.replaceFirst("^```(?:puml|plantuml)?\\s*", "");
      content = content.replaceFirst("```\\s*$", "");
      content = content.trim();
    }
    return content;
  }
}
