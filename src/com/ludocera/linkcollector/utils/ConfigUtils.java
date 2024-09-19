package com.ludocera.linkcollector.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public final class ConfigUtils {
    private static final int DEFAULT_THREAD_COUNT = 10;
    private static final String CONFIG_FILE = "com/ludocera/linkcollector/resources/config.properties";
    private static final Logger logger = Logger.getLogger(ConfigUtils.class.getName());
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    private ConfigUtils() {
        throw new IllegalStateException("Utility class, cannot be instantiated");
    }

    public static boolean useVirtualThreads() {
        loadProperties();

        String useVirtualThreadsValue = properties.getProperty("useVirtualThreads", "false");
        return Boolean.parseBoolean(useVirtualThreadsValue);
    }

    private static void loadProperties() {
        if (isLoaded) return;

        try (InputStream configStream = ConfigUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (configStream == null) {
                logger.log(SEVERE, "Configuration file not found: " + CONFIG_FILE);
            } else {
                properties.load(configStream);
                isLoaded = true;
            }
        } catch (IOException ex) {
            logger.log(SEVERE, "Exception occurred while loading configuration: ", ex);
        }
    }

    public static int getThreadCount() {
        loadProperties();

        String threadCountValue = properties.getProperty("threadCount", String.valueOf(DEFAULT_THREAD_COUNT));

        try {
            return Integer.parseInt(threadCountValue);
        } catch (NumberFormatException e) {
            logger.log(SEVERE, "Invalid threadCount in config.properties. Using default value: " + DEFAULT_THREAD_COUNT);
            return DEFAULT_THREAD_COUNT;
        }
    }
}
