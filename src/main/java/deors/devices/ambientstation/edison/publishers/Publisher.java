package deors.devices.ambientstation.edison.publishers;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

public interface Publisher extends Closeable {

    void connect(Properties properties) throws IOException;

    void publish(String message) throws IOException;
}
