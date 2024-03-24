package nl.nekhvoya.chameleon.exceptions;

public class ReportGenerationError extends Error {
    public ReportGenerationError(Throwable cause) {
        super("Unable generate Chameleon report", cause);
    }
}
