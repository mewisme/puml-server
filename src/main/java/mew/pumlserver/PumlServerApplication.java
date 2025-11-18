package mew.pumlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PumlServerApplication {

  public static void main(String[] args) {
    // Enable PlantUML to download themes from internet
    System.setProperty("PLANTUML_LIMIT_SIZE", "8192");
    // Allow PlantUML to access internet for themes
    System.setProperty("java.awt.headless", "true");
    
    SpringApplication.run(PumlServerApplication.class, args);
  }

}
