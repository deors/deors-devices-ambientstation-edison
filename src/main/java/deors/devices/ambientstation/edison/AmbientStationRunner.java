package deors.devices.ambientstation.edison;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import deors.core.commons.StringToolkit;
import deors.devices.ambientstation.edison.publishers.Publisher;
import deors.devices.ambientstation.edison.publishers.PublisherFactory;

import mraa.mraa;
import mraa.Platform;

import upm_biss0001.BISS0001;
import upm_gas.TP401;
import upm_grove.GroveButton;
import upm_grove.GroveLed;
import upm_grove.GroveLight;
// import upm_grove.GroveTemp;
import upm_jhd1313m1.Jhd1313m1;
import upm_mic.Microphone;
import upm_th02.TH02;

public class AmbientStationRunner {

    // ambient data bean
    private AmbientData ambientData;

    // configuration properties
    private Properties properties;

    // button
    private GroveButton button;

    // led
    private GroveLed led;

    // LCD
    private Jhd1313m1 lcd;

    // temperature & humidity sensor
    // final prototype is built in with the TH02 temperature & humidity sensor
    private TH02 temperatureHumiditySensor;

    // temperature sensor
    // early prototype is built in with the simple temperature sensor (v1.2)
    // private GroveTemp temperatureSimpleSensor;

    // light sensor
    private GroveLight lightSensor;

    // sound sensor
    private Microphone soundSensor;

    // air quality sensor
    private TP401 airQualitySensor;

    // motion sensor
    private BISS0001 motionSensor;

    // external publisher
    private Publisher publisher;

    // thread keep forever flag
    private volatile boolean keepRunning = true;

    // the logger
    private static Logger logger = Logger.getLogger(AmbientStationRunner.class.getName());

    public static void main(String[] args) {

        // initialise the log system
        String logFile = System.getProperty("java.util.logging.config.file");
        try {
            InputStream logConfig;
            if (logFile == null || logFile.isEmpty()) {
                logConfig = AmbientStationRunner.class.getClassLoader().getResourceAsStream("logging.properties");
            } else {
                logConfig = new FileInputStream(logFile);
            }
            LogManager.getLogManager().readConfiguration(logConfig);
        } catch (IOException ioe) {
            System.err.println("ERROR: log system could not be initialised: " + ioe.getMessage());
            System.err.println("HALT!");
            return;
        }

        // check that we are running on Intel Edison
        Platform platform = mraa.getPlatformType();
        if (platform != Platform.INTEL_EDISON_FAB_C) {
            logger.severe(String.format("unsupported platform, exiting"));
            return;
        }

        // check that device id is received through command line or env var

        try {
            new AmbientStationRunner().launch();
        } catch (IOException ioe) {
            logger.severe(String.format("ambient station could not be started: %s", ioe.getMessage()));
            return;
        }
    }

    private static String getConfigurationProperty(String envKey, String sysKey, String defValue) {

        String retValue = defValue;
        String envValue = System.getenv(envKey);
        String sysValue = System.getProperty(sysKey);
        // system property prevails over environment variable
        if (sysValue != null) {
            retValue = sysValue;
        } else if (envValue != null) {
            retValue = envValue;
        }
        return retValue;
    }

    private void launch() throws IOException {

        // properties file name is provided via environment variable or system property
        // a sensible default is assumed
        // note: executable jar created by 'Intel System Studio for IoT' has properties files
        // in a resources folder instead of at the root of the classpath as usual
        String propertiesFileName = getConfigurationProperty("AMBIENT_PROP_FILE", "ambient.prop.file", "/application.properties");

        logger.info(String.format("loading properties from file: %s", propertiesFileName));

        try {
            properties = new Properties();
            properties.load(this.getClass().getResourceAsStream(propertiesFileName));
        } catch (NullPointerException npe) {
            // not very elegant, but is the exception raised by Properties class
            // when the properties file does not exist or could not be found
            logger.severe(String.format("the properties file was not found or could not be read"));
            return;
        }

        // read the station id
        String stationId = properties.getProperty("device.id");

        logger.info(String.format("ambient station id: %s", stationId));

        // ambient data bean initialised with the station id
        ambientData = new AmbientData(stationId);

        // button connected to D5 (digital in)
        button = new GroveButton(5);

        // led connected to D6 (digital out)
        led = new GroveLed(6);

        // LCD connected to I2C #1 bus
        lcd = new Jhd1313m1(0);

        // TH02 temperature and humidity sensor connected to I2C #2 bus
        temperatureHumiditySensor = new TH02(1);

        // temperature sensor connected to A0
        // temperatureSimpleSensor = new GroveTemp(0);

        // light sensor connected to A1
        lightSensor = new GroveLight(1);

        // sound sensor connected to A2
        soundSensor = new Microphone(2);

        // air quality sensor connected to A3
        airQualitySensor = new TP401(3);

        // motion sensor connected to D2
        motionSensor = new BISS0001(2);

        // loop forever reading information from sensors
        new Thread(() -> {

            while (keepRunning) {
                readAmbientData();
                logAmbientData();
                blinkLed();
                checkRanges();
                pause(1000);
            }
        }).start();

        // wait for some data to be collected
        pause(1000);

        // data is published to LCD on a separate thread
        new Thread(() -> {

            while (keepRunning) {
                publishLcd();
                pause(1000);
            }
        }).start();

        // data is published externally on a separate thread
        new Thread(() -> {

            while (keepRunning) {
                publishExternal();
                pause(5000);
            }
        }).start();
    }

    private void pause(long millisecs) {

        try {
            Thread.sleep(millisecs);
        } catch (InterruptedException e) {
        }
    }

    private void readAmbientData() {

        try {
            ambientData.setTemperatureValue(readTemperature());
            // ambientData.setTemperatureValue(readTemperatureSimple());
        } catch (RuntimeException re) {
            logger.severe(String.format("temperature could not be read: %s", re.getMessage()));
        }

        try {
            ambientData.setHumidityValue(readHumidity());
        } catch (RuntimeException re) {
            logger.severe(String.format("humidity could not be read: %s", re.getMessage()));
        }

        try {
            ambientData.setLightValue(readLight());
        } catch (RuntimeException re) {
            logger.severe(String.format("light could not be read: %s", re.getMessage()));
        }

        try {
            ambientData.setSoundValue(readSound());
        } catch (RuntimeException re) {
            logger.severe(String.format("sound could not be read: %s", re.getMessage()));
        }

        try {
            ambientData.setAirQualityValue(readAirQuality()); // also sets airQuality
        } catch (RuntimeException re) {
            logger.severe(String.format("air quality could not be read: %s", re.getMessage()));
        }

        try {
            ambientData.setMotionDetected(readMotionDetected());
        } catch (RuntimeException re) {
            logger.severe(String.format("motion detection could not be read: %s", re.getMessage()));
        }
    }

    // readTemperatureSimple() function is for the simple temperature sensor used in early prototype
    // final prototype is built in with the temperature & humidity TH02 sensor

    // private double readTemperatureSimple() {

    //     // read temperature from sensor (celsius)
    //     double rawValue = temperatureSimpleSensor.raw_value();

    //     final double r0 = 100000.0;
    //     final double b = 4275.0;
    //     final double max = 1023.0;
    //     final double corr = 298.15;
    //     final double zerok = 273.15;

    //     double stg1 = (max / rawValue) - 1.0;
    //     double stg2 = r0 * stg1;
    //     double stg3 = Math.log(stg2 / r0) / b;
    //     double stg4 = stg3 + (1.0 / corr);
    //     double stg5 = (1.0 / stg4) - zerok;

    //     return stg5;
    // }

    private double readTemperature() {

        // read temperature from sensor
        return temperatureHumiditySensor.getTemperature();
    }

    private double readHumidity() {

        // read humidity from sensor
        return temperatureHumiditySensor.getHumidity();
    }

    private double readLight() {

        // read ambient light from sensor
        return lightSensor.value();
    }

    private double readSound() {

        short buffer[] = new short[128];

        // read ambient sound from sensor
        int samples = soundSensor.getSampledWindow(2, buffer);
        double total = 0;
        for (int i : buffer){
            total += i;
        }

        return total / samples;
    }

    private double readAirQuality() {

        short buffer[] = new short[128];

        // read air quality from sensor
        int samples = airQualitySensor.getSampledWindow(2, buffer);
        double total = 0;
        for (int i : buffer){
            total += i;
        }

        return total / samples;
    }

    private boolean readMotionDetected() {

        return motionSensor.motionDetected();
    }

    private void logAmbientData() {

        StringBuffer message = new StringBuffer();
        message.append("station ambient data at: %s%n");
        message.append("- temperature read from sensor: %3.1f%n");
        message.append("- humidity read from sensor: %3.1f%n");
        message.append("- ambient light read from sensor: %.0f%n");
        message.append("- ambient sound read from sensor: %.0f%n");
        message.append("- air quality read from sensor: %.0f / %s%n");
        message.append("- motion detected: %b");

        logger.info(String.format(message.toString(),
            LocalDateTime.now().toString(),
            ambientData.getTemperatureValue(),
            ambientData.getHumidityValue(),
            ambientData.getLightValue(),
            ambientData.getSoundValue(),
            ambientData.getAirQualityValue(),
            ambientData.getAirQuality(),
            ambientData.isMotionDetected()));
    }

    private void checkRanges() {

        if (button.value() == 1) {
            ambientData.resetRanges();
        } else {
            ambientData.checkRanges();
        }
    }

    private void blinkLed() {

        new Thread(() -> {
            // blink the led for 200 ms to show that data was actually sampled
            led.on();
            pause(200);
            led.off();
        }).start();
    }

    private void blinkLedTwice() {

        new Thread(() -> {
            // blink the led twice for 50 ms to show that data was actually sampled
            led.on();
            pause(100);
            led.off();
            pause(100);
            led.on();
            pause(100);
            led.off();
        }).start();
    }

    private void publishLcd() {

        // the temperature range in degrees celsius
        // used for reference for background colour
        final double minTempRange = 0;
        final double maxTempRange = 50;

        // LCD colour control
        double fade;
        short r, g, b;

        // set the fade value depending on where we are in the temperature range
        if (ambientData.getTemperatureValue() <= minTempRange) {
            fade = 0.0f;
        } else if (ambientData.getTemperatureValue() >= maxTempRange) {
            fade = 1.0f;
        } else {
            fade = (ambientData.getTemperatureValue() - minTempRange) / (maxTempRange - minTempRange);
        }

        // fade the colour components separately
        r = (short)(255 * fade);
        g = (short)(64 * fade);
        b = (short)(255 * (1 - fade));

        // apply the calculated background colour
        lcd.setColor(r, g, b);

        // display the current date/time
        write16x2(
            "station data",
            LocalDateTime.now().toString());

        pause(1000);

        // display the temperature data on the LCD
        write16x2(
            String.format("temperature %.1f", ambientData.getTemperatureValue()),
            String.format("mn %.1f mx %.1f", ambientData.getMinTemperatureObserved(), ambientData.getMaxTemperatureObserved()));

        pause(1000);

        // display the humidity data on the LCD
        write16x2(
            String.format("humidity %.1f", ambientData.getHumidityValue()),
            String.format("mn %.1f mx %.1f", ambientData.getMinHumidityObserved(), ambientData.getMaxHumidityObserved()));

        pause(1000);

        // display the ambient light data on the LCD
        write16x2(
            String.format("light %.0f", ambientData.getLightValue()),
            String.format("mn %.0f mx %.0f", ambientData.getMinLightObserved(), ambientData.getMaxLightObserved()));

        pause(1000);

        // display the ambient sound data on the LCD
        write16x2(
            String.format("sound %.0f", ambientData.getSoundValue()),
            String.format("mn %.0f mx %.0f", ambientData.getMinSoundObserved(), ambientData.getMaxSoundObserved()));

        pause(1000);

        // display the air quality data on the LCD
        write16x2(
            String.format("air quality %.0f", ambientData.getAirQualityValue()),
            ambientData.getAirQuality().toString());

        pause(1000);

        // display the motion detection status
        write16x2(
            "motion detected",
            String.format("%b", ambientData.isMotionDetected()));
    }

    private void write16x2(String topLine, String bottomLine) {

        lcd.setCursor(0, 0);
        lcd.write(StringToolkit.padRight(topLine, 16));
        lcd.setCursor(1, 0);
        lcd.write(StringToolkit.padRight(bottomLine, 16));
    }

    private void publishExternal() {

        try {
            if (publisher == null) {
                openExternalPublisher();
            }
            if (publisher != null) {
                publisher.publish(ambientData.toJson());
                blinkLedTwice();
            }
        } catch (IOException ioe) {
            logger.severe(String.format("information could not be published externally: %s", ioe.getMessage()));
            closeExternalPublisher();
        }
    }

    private void openExternalPublisher() {

        try {
            if (publisher == null) {
                publisher = PublisherFactory.getInstance().getPublisher(properties.getProperty("publisher.impl"));

                // don't trust the publisher will not make
                // any changes in the properties
                // actually IBM Watson IoT client does
                Properties copy = new Properties();
                copy.putAll(properties);

                publisher.connect(copy);
            }
        } catch (IOException ioe) {
            logger.severe(String.format("connection with the external publisher could not be established: %s", ioe.getMessage()));
            publisher = null;
        }
    }

    private void closeExternalPublisher() {

        try {
            if (publisher != null) {
                publisher.close();
            }
        } catch (IOException ioe) {
            logger.severe(String.format("connection with the external publisher could not be closed: %s", ioe.getMessage()));
        } finally {
            publisher = null;
        }
    }
}
