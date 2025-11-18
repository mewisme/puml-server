package mew.pumlserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for generating PlantUML code using OpenAI")
public class GenerateRequest {

  @NotBlank(message = "Base URL cannot be blank")
  @Schema(description = "OpenAI API base URL", example = "https://api.openai.com/v1")
  private String baseUrl;

  @NotBlank(message = "API key cannot be blank")
  @Schema(description = "OpenAI API key", example = "sk-...")
  private String apiKey;

  @NotBlank(message = "Model cannot be blank")
  @Schema(description = "OpenAI model to use", example = "gpt-4")
  private String model;

  @NotBlank(message = "Prompt cannot be blank")
  @Schema(description = "User prompt describing the diagram to generate", example = "Create a sequence diagram showing user login flow")
  private String prompt;

  @Schema(description = "Conversation ID for maintaining context. If not provided, a new conversation will be created.", example = "550e8400-e29b-41d4-a716-446655440000")
  private String conversationId;

  @Schema(description = "Whether to stream the response. Default is false.", example = "false")
  private Boolean stream = false;
}
