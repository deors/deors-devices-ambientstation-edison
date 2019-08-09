package deors.devices.ambientstation.edison;

public class AmbientData {

    // station id
    private String id = "unknown";

    // current temperature
    private double temperatureValue = 0.0;

    // minimum and maximum temperature observed
    private double minTemperatureObserved = Integer.MAX_VALUE;
    private double maxTemperatureObserved = Integer.MIN_VALUE;

    // current humidity
    private double humidityValue = 0.0;

    // minimum and maximum humidity observed
    private double minHumidityObserved = Integer.MAX_VALUE;
    private double maxHumidityObserved = Integer.MIN_VALUE;

    // current ambient light
    private double lightValue = 0.0;

    // minimum and maximum ambient light observed
    private double minLightObserved = Integer.MAX_VALUE;
    private double maxLightObserved = Integer.MIN_VALUE;

    // current ambient sound
    private double soundValue = 0.0;

    // minimum and maximum ambient sound observed
    private double minSoundObserved = Integer.MAX_VALUE;
    private double maxSoundObserved = Integer.MIN_VALUE;

    // current air quality
    private double airQualityValue = 0.0;
    private AirQuality airQuality = AirQuality.FRESH;

    // minimum and maximum air quality observed
    private double minAirQualityObserved = Integer.MAX_VALUE;
    private double maxAirQualityObserved = Integer.MIN_VALUE;

    // current motion detection status
    private boolean motionDetected = false;

    public AmbientData(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(double temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public double getMinTemperatureObserved() {
        return minTemperatureObserved;
    }

    public void setMinTemperatureObserved(double minTemperatureObserved) {
        this.minTemperatureObserved = minTemperatureObserved;
    }

    public double getMaxTemperatureObserved() {
        return maxTemperatureObserved;
    }

    public void setMaxTemperatureObserved(double maxTemperatureObserved) {
        this.maxTemperatureObserved = maxTemperatureObserved;
    }

    public double getHumidityValue() {
        return humidityValue;
    }

    public void setHumidityValue(double humidityValue) {
        this.humidityValue = humidityValue;
    }

    public double getMinHumidityObserved() {
        return minHumidityObserved;
    }

    public void setMinHumidityObserved(double minHumidityObserved) {
        this.minHumidityObserved = minHumidityObserved;
    }

    public double getMaxHumidityObserved() {
        return maxHumidityObserved;
    }

    public void setMaxHumidityObserved(double maxHumidityObserved) {
        this.maxHumidityObserved = maxHumidityObserved;
    }

    public double getLightValue() {
        return lightValue;
    }

    public void setLightValue(double lightValue) {
        this.lightValue = lightValue;
    }

    public double getMinLightObserved() {
        return minLightObserved;
    }

    public void setMinLightObserved(double minLightObserved) {
        this.minLightObserved = minLightObserved;
    }

    public double getMaxLightObserved() {
        return maxLightObserved;
    }

    public void setMaxLightObserved(double maxLightObserved) {
        this.maxLightObserved = maxLightObserved;
    }

    public double getSoundValue() {
        return soundValue;
    }

    public void setSoundValue(double soundValue) {
        this.soundValue = soundValue;
    }

    public double getMinSoundObserved() {
        return minSoundObserved;
    }

    public void setMinSoundObserved(double minSoundObserved) {
        this.minSoundObserved = minSoundObserved;
    }

    public double getMaxSoundObserved() {
        return maxSoundObserved;
    }

    public void setMaxSoundObserved(double maxSoundObserved) {
        this.maxSoundObserved = maxSoundObserved;
    }

    public double getAirQualityValue() {
        return airQualityValue;
    }

    public void setAirQualityValue(double airQualityValue) {
        this.airQualityValue = airQualityValue;
        setAirQuality(calculateAirQuality(airQualityValue));
    }

    private AirQuality calculateAirQuality(double airQualityValue) {

        AirQuality airQualityStage = AirQuality.FRESH;

        if (airQualityValue >= 700) {
            airQualityStage = AirQuality.HIGH_POLLUTION;
        } else if (airQualityValue >= 300) {
            airQualityStage = AirQuality.LOW_POLLUTION;
        } else if (airQualityValue >= 30){
            airQualityStage = AirQuality.INDOOR;
        }

        return airQualityStage;
    }

    public AirQuality getAirQuality() {
        return airQuality;
    }

    private void setAirQuality(AirQuality airQuality) {
        this.airQuality = airQuality;
    }

    public double getMinAirQualityObserved() {
        return minAirQualityObserved;
    }

    public void setMinAirQualityObserved(double minAirQualityObserved) {
        this.minAirQualityObserved = minAirQualityObserved;
    }

    public double getMaxAirQualityObserved() {
        return maxAirQualityObserved;
    }

    public void setMaxAirQualityObserved(double maxAirQualityObserved) {
        this.maxAirQualityObserved = maxAirQualityObserved;
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(boolean motionDetected) {
        this.motionDetected = motionDetected;
    }

    public String toJson() {
        return String.format("{\"id\":\"%s\",\"temp\":%.1f,\"humi\":%.1f,\"light\":%.0f,\"sound\":%.0f,\"airq\":%.0f,\"airqstage\":\"%s\",\"motion\":%b}",
            id, temperatureValue, humidityValue, lightValue, soundValue, airQualityValue, airQuality, motionDetected);
    }

    public void resetRanges() {

        minTemperatureObserved = temperatureValue;
        maxTemperatureObserved = temperatureValue;

        minHumidityObserved = humidityValue;
        maxHumidityObserved = humidityValue;

        minLightObserved = lightValue;
        maxLightObserved = lightValue;

        minSoundObserved = soundValue;
        maxSoundObserved = soundValue;

        minAirQualityObserved = airQualityValue;
        maxAirQualityObserved = airQualityValue;
    }

    public void checkRanges() {

        if (temperatureValue < minTemperatureObserved) {
            minTemperatureObserved = temperatureValue;
        }
        if (temperatureValue > maxTemperatureObserved) {
            maxTemperatureObserved = temperatureValue;
        }

        if (humidityValue < minHumidityObserved) {
            minHumidityObserved = humidityValue;
        }
        if (humidityValue > maxHumidityObserved) {
            maxHumidityObserved = humidityValue;
        }

        if (lightValue < minLightObserved) {
            minLightObserved = lightValue;
        }
        if (lightValue > maxLightObserved) {
            maxLightObserved = lightValue;
        }

        if (soundValue < minSoundObserved) {
            minSoundObserved = soundValue;
        }
        if (soundValue > maxSoundObserved) {
            maxSoundObserved = soundValue;
        }

        if (airQualityValue < minAirQualityObserved) {
            minAirQualityObserved = airQualityValue;
        }
        if (airQualityValue > maxAirQualityObserved) {
            maxAirQualityObserved = airQualityValue;
        }
    }
}
