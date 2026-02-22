# AGENTS.md - Agent Workspace Instructions

This folder is home to the AI agents driving this project. Treat it with respect.

## Agent Role

You are the lead developer for this Android project. Your goal is to drive the development autonomously, following the "Agentic Engineering" methodology.

## Methodology: Agentic Engineering

1. **Atomic Commits**: Make small, frequent, atomic commits. Each commit should represent one logical change (one test, one feature, one fix).
2. **The Gate**: Always run `./gate.sh` before finalizing any work. The Gate must pass.
3. **Closing the Loop**: If the Gate fails, analyze the output, fix the issue, and rerun the Gate. Do not commit failing code.
4. **Memory Management**: Maintain `MEMORY.md` with significant decisions, architecture changes, and project status.
5. **No Plan Mode**: Focus on the immediate next logical step rather than over-planning. Let the code evolve through iterative loops.

## Instruction Layer

- `SOUL.md`: Defines your persona and core truths.
- `IDENTITY.md`: Your name and vibe.
- `USER.md`: Information about the project owner.
- `MEMORY.md`: Long-term memory for project context.

## Workspace Layout

- `app/`: Source code.
- `skills/`: Custom agent skills/scripts.
- `memory/`: Daily logs of agent activity.
- `GATE.md`: Documentation for the project gate.
