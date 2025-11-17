package mew.pumlserver.service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service for rendering PlantUML diagrams.
 * 
 * This service renders diagrams using the default PlantUML theme,
 * which matches the style used on PlantUML.com.
 * 
 * To customize the style, users can add theme directives in their PUML code:
 * - !theme plain (default theme, same as PlantUML.com)
 * - !theme cerulean
 * - !theme reddress-darkred
 * - etc.
 * 
 * Or use skinparam directives for custom styling.
 */
@Service
public class RenderService {

  /**
   * Renders PlantUML diagram to SVG format.
   * Uses default theme matching PlantUML.com style.
   * 
   * @param puml PlantUML source code
   * @return SVG image as byte array
   * @throws IOException if rendering fails
   */
  public byte[] renderSvg(String puml) throws IOException {
    SourceStringReader reader = new SourceStringReader(puml);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    // FileFormatOption with default settings matches PlantUML.com rendering
    reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
    return os.toByteArray();
  }

  /**
   * Renders PlantUML diagram to PNG format.
   * Uses default theme matching PlantUML.com style.
   * 
   * @param puml PlantUML source code
   * @return PNG image as byte array
   * @throws IOException if rendering fails
   */
  public byte[] renderPng(String puml) throws IOException {
    SourceStringReader reader = new SourceStringReader(puml);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    reader.generateImage(os, new FileFormatOption(FileFormat.PNG));
    return os.toByteArray();
  }

  /**
   * Renders PlantUML diagram to plain text format.
   * 
   * @param puml PlantUML source code
   * @return Text representation of the diagram
   * @throws IOException if rendering fails
   */
  public String renderText(String puml) throws IOException {
    SourceStringReader reader = new SourceStringReader(puml);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    reader.generateImage(os, new FileFormatOption(FileFormat.UTXT));
    return os.toString();
  }
}
