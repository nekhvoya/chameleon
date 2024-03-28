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
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static nl.nekhvoya.chameleon.Config.*;

/**
 * Util methods enabling comparing of images.
 */
public class Chameleon {
    public static final String IMAGE_FORMAT = "png";
    private static final List<String> tests = new ArrayList<>();

    /**
     * Allows to save an image in the directory of actual results.
     * @param screenshot a byte array of the actual image
     * @param testName the unique name of the test (used to bind the actual image and the reference)
     */
    public static void saveScreenshot(byte[] screenshot, String testName) {
        tests.add(testName);
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

            tests.forEach(test -> {
                ComparisonResult comparisonResult = ComparisonResult.builder()
                        .name(test)
                        .passed(true)
                        .warnings(new ArrayList<>())
                        .errors(new ArrayList<>())
                        .build();

                try (Stream<Path> refs = Files.list(REF_DIR)) {
                    refs.filter(ref -> convertToTestName(ref.getFileName().toString()).equals(test))
                            .findFirst().ifPresentOrElse(
                                    comparisonResult::setRef,
                                    () -> {
                                        comparisonResult.getErrors().add("Reference was not for test %s".formatted(test));
                                        comparisonResult.setPassed(false);
                                    });

                } catch (IOException e) {
                    throw new ConfigurationException("Unable to get files for comparison", e);
                }

                try (Stream<Path> testResults = Files.list(TEST_RESULTS_DIR)) {
                    testResults.filter(result -> result.toFile().isFile())
                            .filter(result -> convertToTestName(result.getFileName().toString()).equals(test))
                            .findFirst().ifPresentOrElse(
                                    comparisonResult::setResult,
                                    () -> {
                                        comparisonResult.getErrors().add("Test image %s was not found".formatted(test));
                                        comparisonResult.setPassed(false);
                                    });
                } catch (IOException e) {
                    throw new ConfigurationException("Unable to get files for comparison", e);
                }

                if (nonNull(comparisonResult.getResult()) && nonNull(comparisonResult.getRef())) {
                    addDiff(comparisonResult);
                }

                comparisonResults.add(comparisonResult);
            });

        if (generateReport) {
            new ReportGenerator(comparisonResults).run();
        }

        return comparisonResults;
    }

    private static void addDiff(ComparisonResult comparisonResult) {
        try {
            boolean passed = true;
            Path diff = Paths.get(DIFF_DIR.toFile().getAbsolutePath(), comparisonResult.getResult().getFileName().toString());

            BufferedImage resultImg = ImageIO.read(comparisonResult.getResult().toFile());
            BufferedImage refImg = ImageIO.read(comparisonResult.getRef().toFile());
            BufferedImage diffImg = ImageIO.read(comparisonResult.getResult().toFile());
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
