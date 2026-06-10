package org.example.rest.controller;

public class PuzzleSolveTimeoutException extends RuntimeException {
    private final long timeoutSeconds;

    public PuzzleSolveTimeoutException(long timeoutSeconds) {
        super("No solution found within " + timeoutSeconds + " seconds");
        this.timeoutSeconds = timeoutSeconds;
    }

    public long timeoutSeconds() {
        return timeoutSeconds;
    }
}
