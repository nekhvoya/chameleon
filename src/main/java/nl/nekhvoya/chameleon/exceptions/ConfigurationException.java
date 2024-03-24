package nl.nekhvoya.chameleon.exceptions;

public class ConfigurationException extends Error{
    public ConfigurationException(Throwable e) {
        super("Could not load Chameleon configuration", e);
    }
}
