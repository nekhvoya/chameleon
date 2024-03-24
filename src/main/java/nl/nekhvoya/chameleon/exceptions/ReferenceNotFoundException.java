package nl.nekhvoya.chameleon.exceptions;

import java.nio.file.Path;

public class ReferenceNotFoundException  extends Error {

    public ReferenceNotFoundException(Path testResult) {
        super("Reference was not for test result %s".formatted(testResult.toFile().getAbsolutePath()));
    }
}
