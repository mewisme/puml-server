package mew.pumlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PumlServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(PumlServerApplication.class, args);
  }

}
