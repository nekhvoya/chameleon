package nl.nekhvoya.chameleon;

import nl.nekhvoya.chameleon.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.nio.file.Files.createDirectories;

public class Config {
    public static final Path TEST_RESULTS_DIR;
    public static final Path REF_DIR;
    public static final Path DIFF_DIR;

    public static final double DEVIATION;

    static {
        try (InputStream is = Chameleon.class.getClassLoader().getResourceAsStream("chameleon.properties")) {

            Properties configProps = new Properties();
            configProps.load(is);
            TEST_RESULTS_DIR = createDirectories(Paths.get(System.getProperty("user.dir"),
                    configProps.getProperty("test.results.dir"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm-ss").format(Instant.now().atZone(ZoneOffset.systemDefault()))));
            REF_DIR = createDirectories(Paths.get(System.getProperty("user.dir"), configProps.getProperty("ref.dir")));
            DIFF_DIR = createDirectories(Paths.get(TEST_RESULTS_DIR.toFile().getAbsolutePath(), "diff"));
            DEVIATION = Double.parseDouble(configProps.getProperty("deviation", "0.0"));
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
}
