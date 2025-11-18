package mew.pumlserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Validation Failed");
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });
    body.put("message", errors);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequestException(
      BadRequestException ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFoundException(
      NotFoundException ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, Object>> handleIOException(
      IOException ex, WebRequest request) {

    String errorMessage = ex.getMessage();
    String error = "Rendering Error";

    // Check if error is related to Graphviz
    if (errorMessage != null && (errorMessage.contains("Graphviz") ||
        errorMessage.contains("dot") ||
        errorMessage.contains("Cannot find Graphviz"))) {
      error = "Graphviz Not Found";
      errorMessage = "Graphviz is required for some PlantUML diagram types (activity, component, etc.). " +
          "Please install Graphviz on your system:\n" +
          "- Linux: sudo apt-get install graphviz (Debian/Ubuntu) or sudo yum install graphviz (RHEL/CentOS)\n" +
          "- Windows: Download from https://graphviz.org/download/\n" +
          "- Mac: brew install graphviz\n\n" +
          "Original error: " + ex.getMessage();
    } else {
      errorMessage = "Failed to render PlantUML diagram: " + errorMessage;
    }

    Map<String, Object> body = new HashMap<>();
    body.put("error", error);
    body.put("message", errorMessage);
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(IllegalAccessError.class)
  public ResponseEntity<Map<String, Object>> handleIllegalAccessError(
      IllegalAccessError ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Module Access Error");
    body.put("message",
        "PlantUML requires Java module access. Please ensure JVM arguments are set: --add-opens java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED");
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(
      RuntimeException ex, WebRequest request) {

    String errorMessage = ex.getMessage();
    String error = "Internal Server Error";

    // Check if error is related to Graphviz
    if (errorMessage != null && (errorMessage.contains("Graphviz") ||
        errorMessage.contains("dot") ||
        errorMessage.contains("Cannot find Graphviz"))) {
      error = "Graphviz Not Found";
      errorMessage = "Graphviz is required for some PlantUML diagram types (activity, component, etc.). " +
          "Please install Graphviz on your system:\n" +
          "- Linux: sudo apt-get install graphviz (Debian/Ubuntu) or sudo yum install graphviz (RHEL/CentOS)\n" +
          "- Windows: Download from https://graphviz.org/download/\n" +
          "- Mac: brew install graphviz\n\n" +
          "Original error: " + ex.getMessage();
    }

    Map<String, Object> body = new HashMap<>();
    body.put("error", error);
    body.put("message", errorMessage);
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("path", request.getDescription(false).replace("uri=", ""));
    body.put("timestamp", LocalDateTime.now());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
