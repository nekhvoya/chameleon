package nl.nekhvoya.chameleon.report;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.nekhvoya.chameleon.ComparisonResult;
import nl.nekhvoya.chameleon.Config;
import nl.nekhvoya.chameleon.exceptions.ReportGenerationError;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static nl.nekhvoya.chameleon.Chameleon.IMAGE_FORMAT;
import static nl.nekhvoya.chameleon.Config.TEST_RESULTS_DIR;
import static nl.nekhvoya.chameleon.report.ReportConfig.*;

public class ReportGenerator {
    private final Map<String, Object> dataModel = new HashMap<>();

    public ReportGenerator(List<ComparisonResult> comparisonResults) {
        dataModel.put("results",
                comparisonResults
                .stream()
                .map(r -> ReportedResult.builder()
                    .name(r.getName())
                    .passed(r.isPassed())
                    .resultImagePath(nonNull(r.getResult()) ? REPORT_DIR.relativize(TEST_RESULTS_DIR) + File.separator + r.getResult().getFileName() : null)
                    .refImagePath(nonNull(r.getRef()) ? REPORT_DIR.relativize(Config.REF_DIR) + File.separator + r.getRef().getFileName() : null)
                    .diffImagePath(nonNull(r.getDiff())? REPORT_DIR.relativize(Config.DIFF_DIR) + File.separator + r.getRef().getFileName() : null)
                    .build())
                .toList()
        );
    }

    public void run() {
        try {
            Files.copy(Paths.get(ASSETS_DIR.toString(), "logo.png"),
                    Paths.get(REPORT_DIR.toString(), "logo.png"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // ignore
        }
        try (Writer out = new FileWriter(Paths.get(REPORT_DIR.toString(),"index.html").toFile())) {
            Template main = ReportConfig.REPORT_CONFIG.getTemplate("main.ftl");
            main.process(dataModel, out);
        } catch (IOException | TemplateException e) {
            throw new ReportGenerationError(e);
        }
    }

    private Path resizeImage(Path imagePath, String imageName) {
        try {
            Path scaledImagePath = Paths.get(REPORT_IMAGES_DIR.toString(), imageName);
            BufferedImage image = ImageIO.read(imagePath.toFile());
            BufferedImage scaledImage = Scalr.resize(image, image.getWidth() / 2);
            ImageIO.write(scaledImage, IMAGE_FORMAT, scaledImagePath.toFile());
            return scaledImagePath;
        } catch (IOException e) {
            return null;
        }
    }
}
