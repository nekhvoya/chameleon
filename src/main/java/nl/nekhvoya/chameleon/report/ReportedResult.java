package nl.nekhvoya.chameleon.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportedResult {
    private String name;
    private boolean passed;
    private String resultImagePath;
    private String scaledResultImagePath;
    private String refImagePath;
    private String scaledRefImagePath;
    private String diffImagePath;
}
