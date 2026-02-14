package com.nexora.shared.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all commands in the system.
 * Commands represent intentions to change state.
 */
public interface Command {

    UUID commandId();

    Instant timestamp();
}
