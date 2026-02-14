package com.nexora.shared.command;

/**
 * Interface for command handlers.
 * Each command type should have exactly one handler.
 *
 * @param <C> the command type this handler processes
 * @param <R> the result type returned after processing
 */
public interface CommandHandler<C extends Command, R> {

    R handle(C command);

    Class<C> commandType();
}
