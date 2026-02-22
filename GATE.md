# GATE.md - The Project Gate

The Gate is the ultimate authority on correctness in this project. All agent work MUST pass the gate before completion.

## The Gate Cycle

1. **Modify**: The agent makes changes to the code or documentation.
2. **Gate**: Run `./gate.sh` to verify changes.
3. **Fix**: If the Gate fails, the agent must fix the issue and return to step 2.
4. **Finalize**: Once the Gate passes, the agent can commit and push the changes.

## Gate Components

- **Code Quality**: Linting and formatting checks.
- **Correctness**: Unit and integration tests.
- **Completeness**: Documentation updates (if required).
- **Safety**: Security and secrets scanning.

## Automated Gate

The Gate is also integrated into GitHub Actions to ensure no broken code enters the main branch.
