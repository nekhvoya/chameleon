package nl.nekhvoya.chameleon.report;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.nekhvoya.chameleon.ComparisonResult;
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
import static nl.nekhvoya.chameleon.report.ReportConfig.*;

public class ReportGenerator {
    private final Map<String, Object> dataModel = new HashMap<>();

    public ReportGenerator(List<ComparisonResult> comparisonResults) {
        dataModel.put("results",
                comparisonResults
                        .stream()
                        .map(r -> {
                            Path copiedRef = nonNull(r.getRef()) ? copyImageToReport(r.getRef(),"ref_") : null;
                            Path copiedResult = nonNull(r.getResult()) ? copyImageToReport(r.getResult(),"test_") : null;
                            Path copiedDiff = nonNull(r.getDiff())? copyImageToReport(r.getDiff(),"diff_") : null;

                            return ReportedResult.builder()
                                    .name(r.getName())
                                    .passed(r.isPassed())
                                    .resultImagePath(nonNull(copiedResult) ? REPORT_DIR.relativize(REPORT_IMAGES_DIR) + File.separator + copiedResult.getFileName() : null)
                                    .refImagePath(nonNull(copiedRef) ? REPORT_DIR.relativize(REPORT_IMAGES_DIR) + File.separator + copiedRef.getFileName() : null)
                                    .diffImagePath(nonNull(copiedDiff)? REPORT_DIR.relativize(REPORT_IMAGES_DIR) + File.separator + copiedDiff.getFileName() : null)
                                    .build();
                        })
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

    private Path copyImageToReport(Path sourceImage, String imagePrefix) {
        try {
            return Files.copy(sourceImage,
                    Paths.get(REPORT_IMAGES_DIR.toString(), imagePrefix + sourceImage.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return null;
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
