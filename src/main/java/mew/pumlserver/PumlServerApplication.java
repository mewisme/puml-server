package mew.pumlserver;

import com.openai.springboot.OpenAIClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { OpenAIClientAutoConfiguration.class })
@EnableScheduling
public class PumlServerApplication {

  public static void main(String[] args) {
    System.setProperty("PLANTUML_LIMIT_SIZE", "8192");
    System.setProperty("java.awt.headless", "true");

    SpringApplication.run(PumlServerApplication.class, args);
  }

}
