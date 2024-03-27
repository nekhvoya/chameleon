package nl.nekhvoya.chameleon;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
public class ComparisonResult {
    private String name;
    private boolean passed;
    private Path result;
    private Path ref;
    private Path diff;
    private List<String> warnings;
    private List<String> errors;
}
