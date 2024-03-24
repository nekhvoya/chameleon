package nl.nekhvoya.chameleon.exceptions;

public class DiffGenerationError extends Error {
    public DiffGenerationError(String message, Throwable t) {
        super(message, t);
    }

    public DiffGenerationError(String message) {
        super(message);
    }
}
