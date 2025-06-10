package org.example.mobile_device.service;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface CommandService {
    void openDoor() throws MqttException;
}
