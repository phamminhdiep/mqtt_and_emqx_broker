package org.example.mobile_device.controller;

import lombok.AllArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.mobile_device.service.CommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commands")
@AllArgsConstructor
public class CommandController {
    private final CommandService commandService;

    @PostMapping("/open-door")
    public ResponseEntity<?> openDoor() {
        try {
            commandService.openDoor();
            return ResponseEntity.ok().build();
        } catch (MqttException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to open door: " + e.getMessage());
        }
    }
}
