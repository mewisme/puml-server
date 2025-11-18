package mew.pumlserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for PUML code generation")
public class GenerateResponse {
    
    @Schema(description = "Generated PlantUML code", example = "@startuml\n\nBob -> Alice : hello\n\n@enduml")
    private String puml;
    
    @Schema(description = "Conversation ID for maintaining context in subsequent requests", example = "550e8400-e29b-41d4-a716-446655440000")
    private String conversationId;
}

