package nl.nekhvoya.uichecker;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class ComparisonResult {
    private boolean passed;
    private Path result;
    private Path ref;
    private Path diff;
}
