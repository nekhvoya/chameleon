package nl.nekhvoya.chameleon.exceptions;

public class ConfigurationException extends Error {
    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }
}
