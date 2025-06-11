package org.example.car_sensor.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.example.car_sensor.utils.SSLSocketProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Component
@Slf4j
@RequiredArgsConstructor
@Data
public class CarSensor {

    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.brokerUrl}")
    private String brokerUrl;
    private MqttClient client;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    private MqttConnectOptions options;

    @PostConstruct
    public void startUp() throws NoSuchAlgorithmException, KeyManagementException {
        try{
            client = new MqttClient(brokerUrl, clientId);

            options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());


            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(sslContext.getSocketFactory());

            client.connect(options);
            log.info("Connected to broker");

            client.setCallback(new MqttCallback() {;
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("Connection lost: {}", cause.getMessage());
                    while (true) {
                        try {
                            Thread.sleep(3000);
                            client.connect(options);
                            log.info("Reconnected to broker");
                            break;
                        } catch (Exception e) {
                            log.error("Reconnection failed. Retrying...", e);
                        }
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    log.info("Message arrived on topic {}: {}", topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            publishOnlineNetworkStatus();

            client.subscribe("car/commands", 0);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }

    private void publishOnlineNetworkStatus() {
        try{
            String onlineMessage = "Car's sensor is online";
            MqttMessage message = new MqttMessage(onlineMessage.getBytes());
            message.setQos(2);
            message.setRetained(true);
            client.publish("mobile/car-network", message);
            log.info("Published retained online message");
        }catch (MqttException e){
            log.error("Failed to publish online network status:", e);
        }

    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                String offlineMessage = "Car's sensor is offline";
                MqttMessage message = new MqttMessage(offlineMessage.getBytes());
                message.setQos(2);
                message.setRetained(true);
                client.publish("mobile/car-network", message);
                log.info("Published retained offline message");

                client.disconnect(1000);
                client.close();
                log.info("Disconnected from broker");
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect from broker:", e);
        }
    }
}
