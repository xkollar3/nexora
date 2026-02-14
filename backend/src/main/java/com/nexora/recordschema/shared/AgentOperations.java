package com.nexora.recordschema.shared;

public record AgentOperations(
    boolean createEnabled,
    boolean updateEnabled,
    boolean deleteEnabled
) {

    public static AgentOperations allEnabled() {
        return new AgentOperations(true, true, true);
    }
}
