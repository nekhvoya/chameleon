package nl.nekhvoya.uichecker.exceptions;

public class ScreenshotError extends Error {
    public ScreenshotError(String message, Throwable cause) {
        super("Unable to save screenshot: %s".formatted(message), cause);
    }
}
