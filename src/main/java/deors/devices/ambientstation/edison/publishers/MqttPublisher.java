package deors.devices.ambientstation.edison.publishers;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher implements Publisher {

    public String topic;

    private MqttClient mqttClient;

    private Logger logger = Logger.getLogger(MqttPublisher.class.getName());

    @Override
    public void connect(Properties properties) throws IOException {

        if (mqttClient != null) {
            return;
        }

        String deviceId = properties.getProperty("device.id");
        String brokerUrl = properties.getProperty("publisher.mqtt.broker.url");

        topic = properties.getProperty("publisher.mqtt.topic");

        logger.info("connecting with the MQTT broker at: " + brokerUrl);
        logger.info("messages will be published at topic: " + topic);

        try {
            mqttClient = new MqttClient(brokerUrl, deviceId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);

            logger.info("connection with the MQTT broker established");
        } catch (MqttException ex) {
            logger.log(Level.SEVERE, "unable to connect to the MQTT broker", ex);
            throw new IOException(ex);
        }
    }

    @Override
    public void publish(String message) throws IOException {

        if (mqttClient != null && message != null) {

            logger.info("publishing message to the MQTT broker: " + message);

            final MqttMessage data = new MqttMessage(message.getBytes());
            data.setQos(2);
            try {
                mqttClient.publish(topic, data);
            } catch (MqttException ex) {
                logger.log(Level.SEVERE, "unable to publish message to the MQTT broker", ex);
                throw new IOException(ex);
            }
        }
    }

    @Override
    public void close() throws IOException {

        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException ex) {
                logger.log(Level.SEVERE, "error closing the connection with the MQTT broker", ex);
                throw new IOException(ex);
            }
        }
    }
}
