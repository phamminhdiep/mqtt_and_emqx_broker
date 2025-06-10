package org.example.car_sensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarSensorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarSensorApplication.class, args);
    }

}
