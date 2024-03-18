package nl.nekhvoya.uichecker.exceptions;

public class ConfigurationException extends Error{
    public ConfigurationException(Throwable e) {
        super("Could not load Pi configuration", e);
    }
}
