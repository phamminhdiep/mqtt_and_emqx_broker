package org.example.mobile_device.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.example.mobile_device.client.MobileDevice;
import org.example.mobile_device.service.CommandService;
import org.springframework.stereotype.Service;
import org.eclipse.paho.client.mqttv3.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {
    private final MobileDevice device;

    @Override
    public void openDoor() throws MqttException {
        try{
            String topic = "car/commands";
            String onlineMessage = "Command: Open Door";
            MqttMessage message = new MqttMessage(onlineMessage.getBytes());
            message.setQos(0);
            message.setRetained(false);

            device.getClient().publish(topic, message);
            log.info("Published open door command message");
        }catch (MqttException e){
            log.error("Failed to publish open door command message:", e);
            throw new MqttException(e);
        }
    }


}
