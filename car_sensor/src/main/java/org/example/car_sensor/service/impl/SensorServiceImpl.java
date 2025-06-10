package org.example.car_sensor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.example.car_sensor.client.CarSensor;
import org.example.car_sensor.service.SensorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    private final CarSensor device;
    @Override
    @Scheduled(fixedRate = 5000)
    public void publishAlert() {
        try{
            String topic = "mobile/alerts";
            String onlineMessage = "Alert: Engine Overheating";
            MqttMessage message = new MqttMessage(onlineMessage.getBytes());
            message.setQos(1);

            device.getClient().publish(topic, message);
            log.info("Published alert message");
        }catch (MqttException e){
            log.error("Failed to publish alert message:", e);
        }
    }

    @Override
    @Scheduled(fixedRate = 7000)
    public void publishCarStatus() {
        try {
            String topic = "mobile/car-status";

            String[] statuses = {
                    "Status: Stable",
                    "Status: Moving",
                    "Status: Idle",
                    "Status: Charging",
                    "Status: Low Battery",
                    "Status: Error"
            };

            String randomStatus = statuses[new Random().nextInt(statuses.length)];

            MqttMessage message = new MqttMessage(randomStatus.getBytes());
            message.setQos(2);

            device.getClient().publish(topic, message);
            log.info("Published car status message: {}", randomStatus);
        } catch (MqttException e) {
            log.error("Failed to publish car status message:", e);
        }
    }
}
