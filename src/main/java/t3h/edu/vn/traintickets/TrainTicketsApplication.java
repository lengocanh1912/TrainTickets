package t3h.edu.vn.traintickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainTicketsApplication.class, args);
    }

}
