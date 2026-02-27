# Repository Guidelines

## Project Structure & Module Organization
This repository is currently a clean scaffold with no committed source files yet. Use this layout when adding code:
- `src/`: application/runtime code.
- `tests/`: automated tests mirroring `src/` structure.
- `assets/`: static files (sample data, fixtures, images).
- `docs/`: design notes, architecture decisions, and onboarding docs.

Keep modules focused and small. Prefer feature-based subfolders (for example, `src/parser/`, `src/runtime/`) over large mixed directories.

## Build, Test, and Development Commands
No project-specific toolchain is defined yet. After selecting a stack, add scripts and document them here. Minimum expected commands:
- `make build` or equivalent: compile/package the project.
- `make test` or equivalent: run the full automated test suite.
- `make lint` or equivalent: run static analysis and formatting checks.

If you use another task runner (`npm`, `poetry`, `cargo`, `mvn`), provide the matching commands in `README.md` and keep this section synchronized.

## Coding Style & Naming Conventions
- Use 4 spaces for indentation unless the language ecosystem strongly prefers otherwise.
- Use descriptive names: `verb_noun` for functions, `PascalCase` for types/classes, and `snake_case` for files unless language conventions differ.
- Keep functions single-purpose; avoid files that mix unrelated responsibilities.
- Add and enforce a formatter/linter early (for example, Prettier/ESLint, Black/Ruff, or equivalent).

## Testing Guidelines
- Place tests under `tests/` with paths that mirror source modules.
- Name tests by behavior (example: `test_parser_rejects_empty_input`).
- Add unit tests for new logic and regression tests for bug fixes.
- Target meaningful coverage on changed code; avoid untested new branches.

## Commit & Pull Request Guidelines
Git history is not available in this workspace, so no existing commit convention can be inferred. Use Conventional Commits going forward (for example, `feat: add expression parser`, `fix: handle null token stream`).

For pull requests:
- Explain what changed and why.
- Link related issues/tasks.
- Include test evidence (command + result summary).
- Keep PRs focused; split large refactors from behavioral changes.

## Security & Configuration Tips
Never commit secrets. Store local credentials in ignored env files (for example, `.env.local`) and provide a sanitized `.env.example` for required variables.
