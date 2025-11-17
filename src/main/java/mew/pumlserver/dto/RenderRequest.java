package mew.pumlserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for rendering PlantUML diagrams")
public class RenderRequest {

    @NotBlank(message = "PUML content cannot be blank")
    @Pattern(regexp = ".*@startuml.*@enduml.*", 
             message = "PUML content must contain @startuml and @enduml tags",
             flags = Pattern.Flag.DOTALL)
    @Schema(description = "PlantUML diagram source code", 
            example = "@startuml\n\n!theme plain\n\nBob -> Alice : hello\n\n@enduml")
    private String puml;
}

