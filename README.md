# PUML Server

REST API server for rendering PlantUML diagrams to various formats (SVG, PNG, Text) using Spring Boot.

## Features

- Render PlantUML diagrams to SVG format
- Render PlantUML diagrams to PNG format
- Render PlantUML diagrams to plain text format
- Cache PUML code with unique IDs for later retrieval
- Retrieve cached PUML code by ID
- Generate PlantUML code using OpenAI API with conversation context
- Optimize PlantUML code using OpenAI API
- Explain PlantUML code using OpenAI API with multi-language support
- Streaming support for AI-generated PlantUML code
- Conversation management with automatic cleanup (30 minutes inactivity)
- Swagger/OpenAPI documentation with interactive UI
- Request validation with detailed error messages
- Global exception handling
- Default theme matching PlantUML.com style

## Requirements

- Java 17 or higher
- Maven Wrapper (included in project, no need to install Maven separately)
- Graphviz (optional but recommended for some diagram types)

### Installing Graphviz

Graphviz is required for certain PlantUML diagram types (activity diagrams, component diagrams, etc.). If you encounter a "Graphviz Not Found" error, install Graphviz:

**Linux (Debian/Ubuntu):**
```bash
sudo apt-get update
sudo apt-get install graphviz
```

**Linux (RHEL/CentOS):**
```bash
sudo yum install graphviz
```

**Windows:**
Download and install from [https://graphviz.org/download/](https://graphviz.org/download/)

**macOS:**
```bash
brew install graphviz
```

After installation, verify Graphviz is available:
```bash
dot -V
```

## Quick Start

### Using Maven Wrapper

```bash
# Build project
./mvnw clean package
# Windows: mvnw.cmd clean package

# Run application
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

### Run JAR directly

```bash
# Build first
./mvnw clean package
# Windows: mvnw.cmd clean package

# Run with JVM arguments
java --add-opens java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.gif=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.bmp=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.wbmp=ALL-UNNAMED \
     -jar target/puml-server-0.0.8-SNAPSHOT.jar
```

## API Endpoints

### POST /api/v1/render/svg
Renders PlantUML diagram to SVG format and returns cache ID. The same ID can be used to retrieve SVG, PNG, Text formats, or the original PUML code.

**Request:**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Note:** The returned ID is shared across all endpoints. You can use it with `GET /api/v1/puml/{id}` or `GET /api/v1/render/{type}/{id}/raw`.

### POST /api/v1/render/png
Renders PlantUML diagram to PNG format and returns cache ID. The same ID can be used to retrieve SVG, PNG, Text formats, or the original PUML code.

**Request:**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Note:** The returned ID is shared across all endpoints. You can use it with `GET /api/v1/puml/{id}` or `GET /api/v1/render/{type}/{id}/raw`.

### POST /api/v1/render/text
Renders PlantUML diagram to plain text format and returns cache ID. The same ID can be used to retrieve SVG, PNG, Text formats, or the original PUML code.

**Request:**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Note:** The returned ID is shared across all endpoints. You can use it with `GET /api/v1/puml/{id}` or `GET /api/v1/render/{type}/{id}/raw`.

### POST /api/v1/puml
Caches PlantUML source code and returns cache ID. If the same PUML code already exists in cache, returns the existing ID. Otherwise, creates a new cache entry and returns a new ID. The returned ID can be used with any other endpoint (`GET /api/v1/puml/{id}`, `GET /api/v1/render/{type}/{id}/raw`).

**Request:**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### GET /api/v1/puml/{id}
Retrieves the PlantUML source code by cache ID. The ID can be obtained from any endpoint that returns an ID (`POST /api/v1/puml`, `POST /api/v1/render/svg`, `POST /api/v1/render/png`, or `POST /api/v1/render/text`).

**Response:**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

### GET /api/v1/render/{type}/{id}/raw
Retrieves cached rendered content by ID and format type (svg, png, or text). The ID can be obtained from any endpoint that returns an ID. The same ID can be used to retrieve SVG, PNG, or Text formats. Content expires after 30 minutes.

**Note:** Cache IDs are shared across all endpoints. An ID returned from `POST /api/v1/puml` can be used here, and vice versa.

**Response:** Rendered content in the requested format (SVG, PNG, or plain text)

### POST /api/v1/puml/generate
Generates PlantUML code using OpenAI API based on a user prompt. The system automatically acts as a PlantUML expert. Supports conversation context and streaming.

**Request:**
```json
{
  "baseUrl": "https://api.openai.com/v1",
  "apiKey": "sk-...",
  "model": "gpt-4",
  "prompt": "Create a sequence diagram showing user login flow",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "stream": false
}
```

**Response (non-streaming):**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (streaming):** Server-Sent Events (SSE) stream with `text/event-stream` content type

**Features:**
- If `conversationId` is provided, maintains context from previous messages
- If `conversationId` is not provided, creates a new conversation and returns its ID
- If `stream` is `true`, returns SSE stream
- If `stream` is `false`, returns JSON response
- Conversations automatically expire after 30 minutes of inactivity
- Generated PUML code is automatically cached

### POST /api/v1/puml/optimize
Optimizes PlantUML code using OpenAI API. The system automatically acts as a PlantUML optimization expert. Supports streaming.

**Request:**
```json
{
  "baseUrl": "https://api.openai.com/v1",
  "apiKey": "sk-...",
  "model": "gpt-4",
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml",
  "stream": false
}
```

**Response (non-streaming):**
```json
{
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml"
}
```

**Response (streaming):** Server-Sent Events (SSE) stream with `text/event-stream` content type

**Features:**
- If `stream` is `true`, returns SSE stream
- If `stream` is `false`, returns JSON response
- Optimized PUML code is automatically cached
- This endpoint does not maintain conversation context

### POST /api/v1/puml/explain
Explains what a PlantUML diagram does using OpenAI API. The system automatically acts as a PlantUML explanation expert. Supports streaming and multiple languages.

**Request:**
```json
{
  "baseUrl": "https://api.openai.com/v1",
  "apiKey": "sk-...",
  "model": "gpt-4",
  "puml": "@startuml\n\nBob -> Alice : hello\n\n@enduml",
  "language": "en",
  "stream": false
}
```

**Response (non-streaming):**
```json
{
  "explanation": "This diagram shows a simple sequence diagram where Bob sends a 'hello' message to Alice."
}
```

**Response (streaming):** Server-Sent Events (SSE) stream with `text/event-stream` content type

**Features:**
- If `stream` is `true`, returns SSE stream
- If `stream` is `false`, returns JSON response
- `language` parameter controls the language of the explanation (e.g., 'en' for English, 'vi' for Vietnamese)
- Default language is English if not specified
- This endpoint does not maintain conversation context

### DELETE /api/v1/puml/conversation/{conversationId}
Deletes a conversation by ID. The conversation and all its context will be removed permanently.

**Response:**
```json
{
  "message": "Conversation deleted successfully",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## API Documentation

Once the server is running, access the Swagger UI at:

- Swagger UI: http://localhost:7235/swagger-ui.html
- API Docs (JSON): http://localhost:7235/v3/api-docs
- API Docs (YAML): http://localhost:7235/v3/api-docs.yaml

## Configuration

Server runs on port **7235** by default. You can change this in `src/main/resources/application.properties`:

```properties
server.port=7235
```

## Caching

The API uses a shared in-memory cache to store PUML code and rendered formats:

- **Cache Duration**: 30 minutes
- **Cache Behavior**: 
  - When you call `POST /api/v1/puml` or any `/render` endpoint, the system checks if the same PUML code already exists in cache
  - If found, it returns the existing cache ID
  - If not found, it creates a new cache entry and returns a new ID
- **Shared Cache ID**: Cache IDs are shared across all endpoints and controllers. The same ID returned from any endpoint can be used with any other endpoint:
  - Get the original PUML code via `GET /api/v1/puml/{id}`
  - Get rendered formats (SVG, PNG, Text) via `GET /api/v1/render/{type}/{id}/raw`
  - Example: An ID returned from `POST /api/v1/render/svg` can be used with `GET /api/v1/puml/{id}` or `GET /api/v1/render/png/{id}/raw`

## Request Validation

The API validates that:
- PUML content is not blank
- PUML content contains `@startuml` and `@enduml` tags

## Error Responses

All errors return JSON format:

```json
{
  "error": "Error Type",
  "message": "Error message or details",
  "status": 400,
  "path": "/api/v1/render/svg",
  "timestamp": "2025-11-17T23:00:00"
}
```

## PlantUML Themes

By default, diagrams are rendered with the same style as PlantUML.com. You can customize the theme by adding theme directives in your PUML code.

**Note:** Themes are downloaded from the internet when first used. Make sure your server has internet access to use themes.

### Using Themes

To use a theme, add the `!theme` directive at the beginning of your PUML code:

```puml
@startuml
!theme cerulean
Bob -> Alice : hello
@enduml
```

### Checking Available Themes

To see all available themes in your PlantUML version, you can use:

```puml
@startuml
help themes
@enduml
```

### Popular Themes

Some popular themes include:
- `cerulean` - Light blue theme
- `reddress-darkred` - Dark red theme  
- `sketchy-outline` - Sketchy style
- `spacelab` - Space lab theme
- `united` - United theme
- `dark` - Dark theme

**Note:** Theme names may vary by PlantUML version. Use `help themes` to see the complete list for your version.

### Custom Styling

Instead of themes, you can also use `skinparam` directives for custom styling:

```puml
@startuml
skinparam backgroundColor #FFFFFF
skinparam defaultFontName Arial
Bob -> Alice : hello
@enduml
```

## Project Structure

```
puml-server/
├── src/main/java/mew/pumlserver/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data Transfer Objects
│   ├── exception/       # Exception handlers
│   ├── service/         # Business logic
│   └── PumlServerApplication.java
├── src/main/resources/
│   └── application.properties
├── scripts/             # Build and run scripts
└── pom.xml
```

## Dependencies

- Spring Boot 3.5.7
- PlantUML 8059
- SpringDoc OpenAPI 2.8.14
- Lombok
- Spring Boot Starter Web
- Spring Boot Starter validation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Copyright (c) 2025 Mew

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

