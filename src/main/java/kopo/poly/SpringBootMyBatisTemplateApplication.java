package kopo.poly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringBootMyBatisTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMyBatisTemplateApplication.class, args);
    }

}
