package org.example.mobile_device.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Component
@Slf4j
@RequiredArgsConstructor
@Data
public class MobileDevice {

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
            client.setCallback(new MqttCallback() {
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
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

            options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(false);
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

            client.subscribe("mobile/car-network", 2);
            client.subscribe("mobile/alerts", 1);
            client.subscribe("mobile/car-status", 2);


        } catch (MqttException e) {
            log.error("Failed to connect to broker:", e);
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                log.info("Disconnected from Mosquitto broker");
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect from broker:", e);
        }
    }
}
