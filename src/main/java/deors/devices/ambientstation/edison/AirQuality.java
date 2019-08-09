package deors.devices.ambientstation.edison;

public enum AirQuality {

    FRESH("fresh air"),
    INDOOR("normal indoor air"),
    LOW_POLLUTION("low pollution"),
    HIGH_POLLUTION("high pollution!!");

    private String stageText;

    AirQuality(String text) {
        stageText = text;
    }

    public String toString() {
        return stageText;
    }
}
