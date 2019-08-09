package deors.devices.ambientstation.edison.publishers;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.iotf.client.device.DeviceClient;

public class IBMWatsonIoTPublisher implements Publisher {

    public String topic;

    private DeviceClient watsonIoTClient;

    private Logger logger = Logger.getLogger(IBMWatsonIoTPublisher.class.getName());

    @Override
    public void connect(Properties properties) throws IOException {

        if (watsonIoTClient != null) {
            return;
        }

        topic = properties.getProperty("publisher.mqtt.topic");

        logger.info("connecting with the IBM Watson IoT broker");
        logger.info("messages will be published at topic: " + topic);

        try {
            watsonIoTClient = new DeviceClient(properties);
            watsonIoTClient.connect();

            logger.info("connection with the IBM Watson IoT broker established");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "unable to connect to the IBM Watson IoT broker", ex);
            throw new IOException(ex);
        }
    }

    @Override
    public void publish(String message) throws IOException {

        if (watsonIoTClient != null && message != null) {

            logger.info("publishing message to the IBM Watson IoT broker: " + message);

            try {
                boolean status = watsonIoTClient.publishEvent(topic, message);
                if (!status) {
                    logger.log(Level.SEVERE, "unable to publish message to the IBM Watson IoT broker - reason unknown");
                    throw new IOException("unable to publish message to the IBM Watson IoT broker - reason unknown");
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "unable to publish message to the IBM Watson IoT broker", ex);
                throw new IOException(ex);
            }
        }
    }

    @Override
    public void close() throws IOException {

        if (watsonIoTClient != null) {
            try {
                watsonIoTClient.disconnect();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "error closing the connection with the IBM Watson IoT broker", ex);
                throw new IOException(ex);
            }
        }
    }
}
