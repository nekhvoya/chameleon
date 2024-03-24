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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nl.nekhvoya.chameleon.Config.TEST_RESULTS_DIR;
import static nl.nekhvoya.chameleon.Chameleon.IMAGE_FORMAT;
import static nl.nekhvoya.chameleon.report.ReportConfig.REPORT_DIR;
import static nl.nekhvoya.chameleon.report.ReportConfig.REPORT_IMAGES_DIR;

public class ReportGenerator {
    private final Map<String, Object> dataModel = new HashMap<>();

    public ReportGenerator(List<ComparisonResult> comparisonResults) {
        dataModel.put("results",
                comparisonResults
                .stream()
                .map(r -> {
                    Path scaledResult = resizeImage(r.getResult(), "test_" + r.getResult().getFileName().toString());
                    Path scaledRef = resizeImage(r.getRef(), "ref_" + r.getRef().getFileName().toString());
                    String assetsPath = REPORT_DIR.relativize(REPORT_IMAGES_DIR) + File.separator;
                    return ReportedResult.builder()
                        .name(r.getName())
                        .passed(r.isPassed())
                        .resultImagePath(nonNull(r.getResult()) ? REPORT_DIR.relativize(TEST_RESULTS_DIR) + File.separator + r.getResult().getFileName() : null)
                        .scaledResultImagePath(nonNull(scaledResult) ? assetsPath + scaledResult.getFileName() : null)
                        .refImagePath(nonNull(r.getRef()) ? REPORT_DIR.relativize(Config.REF_DIR) + File.separator + r.getRef().getFileName() : null)
                        .scaledRefImagePath(nonNull(scaledRef) ? assetsPath + scaledRef.getFileName() : null)
                        .diffImagePath(isNull(r.getDiff())? null : r.getDiff() .toFile().getAbsolutePath())
                        .build(); })
                .toList()
        );
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

    public void run() {
        try (Writer out = new FileWriter(Paths.get(REPORT_DIR.toFile().getAbsolutePath(),"index.html").toFile())) {
            Template main = ReportConfig.REPORT_CONFIG.getTemplate("main.ftl");
            main.process(dataModel, out);
        } catch (IOException | TemplateException e) {
            throw new ReportGenerationError(e);
        }
    }
}
