package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public final class ConfigUtils {

    public static final int DEFAULT_THREAD_COUNT = 10;
    private static final String CONFIG_FILE = "resources/config.properties";
    private static final Logger logger = Logger.getLogger(ConfigUtils.class.getName());
    private static final Properties properties = new Properties();

    static {
        try (InputStream configStream = ConfigUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (configStream == null) {
                logger.log(SEVERE, "Unable to find " + CONFIG_FILE);
            } else {
                properties.load(configStream);
            }
        } catch (IOException ex) {
            logger.log(SEVERE, "Exception occurred while loading " + CONFIG_FILE + ": " + ex.getMessage());
        }
    }

    private ConfigUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static int getThreadCount() {
        String threadCountValue = properties.getProperty("threadCount", String.valueOf(DEFAULT_THREAD_COUNT));

        try {
            return Integer.parseInt(threadCountValue);
        } catch (NumberFormatException e) {
            logger.log(SEVERE, "Invalid threadCount in config.properties. Using default value " + DEFAULT_THREAD_COUNT);
            return DEFAULT_THREAD_COUNT;
        }
    }
}
