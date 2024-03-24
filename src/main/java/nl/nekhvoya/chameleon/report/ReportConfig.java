package nl.nekhvoya.chameleon.report;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import nl.nekhvoya.chameleon.exceptions.ReportGenerationError;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

import static java.nio.file.Files.createDirectories;
import static nl.nekhvoya.chameleon.Config.TEST_RESULTS_DIR;

public class ReportConfig {
    public static final Configuration REPORT_CONFIG = new Configuration(Configuration.VERSION_2_3_32);
    public static final Path TEMPLATE_DIR = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "report", "templates");
    public static final Path REPORT_DIR;
    public static final Path REPORT_IMAGES_DIR;


    static {
        try {
            REPORT_CONFIG.setDirectoryForTemplateLoading(TEMPLATE_DIR.toFile());
            REPORT_CONFIG.setDefaultEncoding("UTF-8");
            REPORT_CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            REPORT_CONFIG.setLogTemplateExceptions(false);
            REPORT_CONFIG.setWrapUncheckedExceptions(true);
            REPORT_CONFIG.setFallbackOnNullLoopVariable(false);
            REPORT_CONFIG.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
            REPORT_DIR = createDirectories(Paths.get(TEST_RESULTS_DIR.toString(), "report"));
            REPORT_IMAGES_DIR = createDirectories(Paths.get(REPORT_DIR.toString(), "images"));
        } catch (IOException e) {
            throw new ReportGenerationError(e);
        }
    }
}
