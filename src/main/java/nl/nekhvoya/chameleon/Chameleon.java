package nl.nekhvoya.chameleon;

import nl.nekhvoya.chameleon.exceptions.*;
import nl.nekhvoya.chameleon.report.ReportGenerator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static nl.nekhvoya.chameleon.Config.*;

public class Chameleon {
    public static final String IMAGE_FORMAT = "png";

    public static void saveScreenshot(byte[] screenshot, String testName) {
        Path destinationFile = Paths.get(TEST_RESULTS_DIR.toFile().getAbsolutePath(), convertToFileName(testName) );
        try {
            Files.write(destinationFile, screenshot);
        } catch (IOException e) {
            throw new ScreenshotError("Failed to save file %s".formatted(destinationFile.toFile().getAbsolutePath()), e);
        }
    }

    public static void compare() {
        List<ComparisonResult> comparisonResults = new LinkedList<>();

        try (Stream<Path> results = Files.list(TEST_RESULTS_DIR).filter(result -> result.toFile().isFile())) {
            results.forEach(result -> {
                try (Stream<Path> refs = Files.list(REF_DIR)) {
                    Path reference = refs.filter(ref -> ref.getFileName().toString().equals(result.getFileName().toString()))
                            .findFirst().orElseThrow(() -> new ReferenceNotFoundException(result));
                    Path diff = createDiff(result, reference);

                    ComparisonResult comparisonResult = ComparisonResult.builder()
                            .name(convertToTestName(result.toFile().getName()))
                            .passed(isNull(diff))
                            .result(result)
                            .ref(reference)
                            .diff(diff)
                            .build();

                    comparisonResults.add(comparisonResult);

                } catch (IOException e) {
                    throw new DiffGenerationError("Unable to find references under path %s".formatted(REF_DIR.toFile().getAbsolutePath()), e);
                }
            });
        } catch (IOException e) {
            throw new ImageComparisonError("Unable to get files for verification", e);
        }

        new ReportGenerator(comparisonResults).run();
    }

    private static Path createDiff(Path result, Path reference) {
        try {
            boolean passed = true;
            Path diff = Paths.get(DIFF_DIR.toFile().getAbsolutePath(), result.getFileName().toString());

            BufferedImage resultImg = ImageIO.read(result.toFile());
            BufferedImage refImg = ImageIO.read(reference.toFile());
            BufferedImage diffImg = ImageIO.read(result.toFile());

            if (resultImg.getWidth() != refImg.getWidth() || resultImg.getHeight() != refImg.getHeight()) {
                throw new InvalidReferenceError();
            }

            for (int x = 0; x < resultImg.getWidth(); x++) {
                for (int y = 0; y < resultImg.getHeight(); y++) {
                    int resultPxl = resultImg.getRGB(x, y);
                    int refPxl = refImg.getRGB(x, y);
                    if (resultPxl == refPxl) {
                        diffImg.setRGB(x, y, resultPxl);
                    } else {
                        diffImg.setRGB(x, y, Color.CYAN.getRGB());
                        passed = false;
                    }
                }
            }
            if (!passed) {
                if (!ImageIO.write(diffImg, IMAGE_FORMAT, diff.toFile())) {
                    throw new DiffGenerationError("Unable to save diff image in file %s".formatted(diff.toFile().getAbsolutePath()));
                }
                return diff;
            }
            return null;
        } catch (IOException e) {
            throw new ImageComparisonError("Unable to verify images", e);
        }
    }

    private static String convertToFileName(String testName) {
        return "%s.%s".formatted(testName.replaceAll(" ", "_"), IMAGE_FORMAT);
    }

    private static String convertToTestName(String fileName) {
        return fileName.replaceAll("_", " ").replaceAll(".%s".formatted(IMAGE_FORMAT), "");
    }
}
