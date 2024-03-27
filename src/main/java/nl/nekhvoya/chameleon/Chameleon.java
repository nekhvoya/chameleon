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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static nl.nekhvoya.chameleon.Config.*;

/**
 * Util methods enabling comparing of images.
 */
public class Chameleon {
    public static final String IMAGE_FORMAT = "png";

    /**
     * Allows to save an image in the directory of actual results.
     * @param screenshot a byte array of the actual image
     * @param testName the unique name of the test (used to bind the actual image and the reference)
     */
    public static void saveScreenshot(byte[] screenshot, String testName) {
        Path destinationFile = Paths.get(TEST_RESULTS_DIR.toFile().getAbsolutePath(), convertToFileName(testName) );
        try {
            Files.write(destinationFile, screenshot);
        } catch (IOException e) {
            throw new ScreenshotError("Failed to save file %s".formatted(destinationFile.toFile().getAbsolutePath()), e);
        }
    }


    /**
     * Runs the comparison analysis of actual images against the reference images.
     * @param generateReport allows to define if the report should be generated based on the comparison results.
     * @return list of the comparison results.
     */
    public static List<ComparisonResult> compare(boolean generateReport) {
        List<ComparisonResult> comparisonResults = new LinkedList<>();

        try (Stream<Path> results = Files.list(TEST_RESULTS_DIR).filter(result -> result.toFile().isFile())) {
            results.forEach(result -> {
                try (Stream<Path> refs = Files.list(REF_DIR)) {
                    Path reference = refs.filter(ref -> ref.getFileName().toString().equals(result.getFileName().toString()))
                            .findFirst().orElseThrow(() -> new ReferenceNotFoundException(result));


                    ComparisonResult comparisonResult = ComparisonResult.builder()
                            .name(convertToTestName(result.toFile().getName()))
                            .passed(true)
                            .result(result)
                            .ref(reference)
                            .warnings(new ArrayList<>())
                            .errors(new ArrayList<>())
                            .build();

                    addDiff(comparisonResult, result, reference);

                    comparisonResults.add(comparisonResult);

                } catch (IOException e) {
                    throw new DiffGenerationError("Unable to find references under path %s".formatted(REF_DIR.toFile().getAbsolutePath()), e);
                }
            });
        } catch (IOException e) {
            throw new ImageComparisonError("Unable to get test results for verification", e);
        }

        if (generateReport) {
            new ReportGenerator(comparisonResults).run();
        }

        return comparisonResults;
    }

    private static void addDiff(ComparisonResult comparisonResult, Path result, Path reference) {
        try {
            boolean passed = true;
            Path diff = Paths.get(DIFF_DIR.toFile().getAbsolutePath(), result.getFileName().toString());

            BufferedImage resultImg = ImageIO.read(result.toFile());
            BufferedImage refImg = ImageIO.read(reference.toFile());
            BufferedImage diffImg = ImageIO.read(result.toFile());
            int diffCount = 0;

            if (resultImg.getWidth() != refImg.getWidth() || resultImg.getHeight() != refImg.getHeight()) {
                comparisonResult.getWarnings().add("Reference image and test image are different in size! Reference: %dx%d. Result: %dx%d"
                        .formatted(refImg.getWidth(), refImg.getHeight(), resultImg.getWidth(), resultImg.getHeight()));
            }

            for (int x = 0; x < resultImg.getWidth(); x++) {
                for (int y = 0; y < resultImg.getHeight(); y++) {
                    int resultPxl = resultImg.getRGB(x, y);
                    int refPxl;
                    try {
                        refPxl = refImg.getRGB(x, y);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        refPxl = -1;
                    }
                    if (resultPxl == refPxl) {
                        diffImg.setRGB(x, y, resultPxl);
                    } else {
                        diffImg.setRGB(x, y, Color.CYAN.getRGB());
                        diffCount++;
                    }
                }
            }
            double diffPercent = (double) (diffCount * 100) / (resultImg.getWidth() * resultImg.getHeight());
            if (diffPercent > DEVIATION) {
                passed = false;
            }
            comparisonResult.setPassed(passed);

            if (!passed) {
                if (!ImageIO.write(diffImg, IMAGE_FORMAT, diff.toFile())) {
                   comparisonResult.getErrors().add("Unable to save diff image in file %s".formatted(diff.toFile().getAbsolutePath()));
                }
                comparisonResult.setDiff(diff);
            }
        } catch (IOException e) {
            comparisonResult.getErrors().add("Unable to verify images: %s".formatted(e.getMessage()));
        }
    }

    private static String convertToFileName(String testName) {
        return "%s.%s".formatted(testName.replaceAll(" ", "_"), IMAGE_FORMAT);
    }

    private static String convertToTestName(String fileName) {
        return fileName.replaceAll("_", " ").replaceAll(".%s".formatted(IMAGE_FORMAT), "");
    }
}
