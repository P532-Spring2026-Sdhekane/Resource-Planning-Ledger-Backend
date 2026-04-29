package com.rpl.domain.state;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String currentState, String event) {
        super("Cannot trigger '" + event + "' from state '" + currentState + "'");
    }
}
