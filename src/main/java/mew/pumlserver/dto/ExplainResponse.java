package mew.pumlserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for PUML code explanation")
public class ExplainResponse {
    
    @Schema(description = "Explanation of what the PlantUML code does", example = "This diagram shows a simple sequence diagram where Bob sends a 'hello' message to Alice.")
    private String explanation;
}

