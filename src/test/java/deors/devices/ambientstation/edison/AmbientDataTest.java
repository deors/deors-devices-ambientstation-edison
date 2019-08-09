package deors.devices.ambientstation.edison;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AmbientDataTest {

    @Test
    public void testJsonTransformation() {

        AmbientData d = new AmbientData("id1");
        d.setTemperatureValue(20.1);
        d.setHumidityValue(58.1);
        d.setLightValue(50);
        d.setSoundValue(53);
        d.setAirQualityValue(49);
        d.setMotionDetected(true);
        String s = d.toJson();

        assertEquals("{\"id\":\"id1\",\"temp\":20.1,\"humi\":58.1,\"light\":50,\"sound\":53,\"airq\":49,\"airqstage\":\"normal indoor air\",\"motion\":true}", s);
    }
}
