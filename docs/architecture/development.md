# Development rules

## Testing
- New functionality MUST come with unit tests.
- Unit tests MUST mock `EngineProcess` instead of starting real chess engines. Tests that need real engines MUST be integration tests in `*IT.java` files.

## Module dependencies
- `services` depends on `engine`. `cli` depends on `services` and `engine`.
- Changes MUST NOT add any other dependency between these Maven modules.

## Platforms
- The application MUST work on Linux, macOS, and Windows.
- Code MUST NOT use OS-specific APIs or hard-coded path separators.

## Engine state machine
- Engine implementations MUST keep the explicit state machine `CreatedEngine` → `ConfiguredEngine` → `IdlingEngine` → `ForcedEngine` → `ActiveEngine`.
- Each state MUST stay an immutable record. Each transition MUST return a new instance of the next state.
