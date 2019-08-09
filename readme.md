# deors-devices-ambientstation-edison

Ambient station built on the Edison platform with Arduino dev board, Grove shield and Grove sensors, Intel's mraa and upm libraries, and publishing data via MQTT or IBM Watson IoT service.

## configuring the device

The application configuration file can be fed via the `AMBIENT_PROP_FILE` environment variable or the `ambient.prop.file` JVM system property. The default configuration provided configures a station publishing data to Eclipse IoT MQTT server on topic `AmbientStation/org/location/space/default`.
