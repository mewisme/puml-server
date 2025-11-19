package mew.pumlserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for optimizing PlantUML code using OpenAI")
public class OptimizeRequest {

  @NotBlank(message = "Base URL cannot be blank")
  @Schema(description = "OpenAI API base URL", example = "https://api.openai.com/v1")
  private String baseUrl;

  @NotBlank(message = "API key cannot be blank")
  @Schema(description = "OpenAI API key", example = "sk-...")
  private String apiKey;

  @NotBlank(message = "Model cannot be blank")
  @Schema(description = "OpenAI model to use", example = "gpt-4")
  private String model;

  @NotBlank(message = "PUML code cannot be blank")
  @Schema(description = "PlantUML code to optimize", example = "@startuml\n\nBob -> Alice : hello\n\n@enduml")
  private String puml;

  @Schema(description = "Whether to stream the response. Default is false.", example = "false")
  private Boolean stream = false;
}

