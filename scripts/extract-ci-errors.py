#!/usr/bin/env python3
"""Extract high-signal Gradle/Android failure context from a noisy CI log."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ERROR_PATTERNS = [
    re.compile(r"\* What went wrong", re.I),
    re.compile(r"\* Exception is", re.I),
    re.compile(r"Execution failed for task", re.I),
    re.compile(r"FAILURE: Build failed", re.I),
    re.compile(r"Caused by:", re.I),
    re.compile(r"^> Task .* FAILED$", re.I),
    re.compile(r"\berror:\s*", re.I),
    re.compile(r"\bERROR\b", re.I),
    re.compile(r"AAPT2? error", re.I),
    re.compile(r"Manifest merger failed", re.I),
    re.compile(r"Duplicate class", re.I),
    re.compile(r"Could not resolve", re.I),
    re.compile(r"Could not find", re.I),
    re.compile(r"Program type already present", re.I),
    re.compile(r"R8: missing", re.I),
    re.compile(r"DexArchiveMergerException", re.I),
    re.compile(r"Compilation failed", re.I),
    re.compile(r"BUILD FAILED", re.I),
    re.compile(r"> Failed to", re.I),
    re.compile(r"java\.lang\.[A-Za-z]+Exception", re.I),
    re.compile(r"java\.lang\.[A-Za-z]+Error", re.I),
    re.compile(r"org\.gradle\.api\.tasks\.TaskExecutionException", re.I),
]

NOISE_PATTERNS = [
    re.compile(r"^\s*Downloading .*", re.I),
    re.compile(r"^\s*Downloading from .*", re.I),
    re.compile(r"^\s*Progress:\s*", re.I),
    re.compile(r"^\s*Daemon will be stopped", re.I),
]

ANSI_RE = re.compile(r"\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])")
MAX_OUTPUT_LINES = 1400
CONTEXT_BEFORE = 4
CONTEXT_AFTER = 16
TAIL_LINES = 100


def clean(line: str) -> str:
    return ANSI_RE.sub("", line).rstrip()


def is_noise(line: str) -> bool:
    return any(p.search(line) for p in NOISE_PATTERNS)


def is_error(line: str) -> bool:
    return any(p.search(line) for p in ERROR_PATTERNS)


def main() -> int:
    if len(sys.argv) != 3:
        print("usage: extract-ci-errors.py <input-log> <output-log>", file=sys.stderr)
        return 2

    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    if not source.exists():
        target.write_text("Build log was not produced. Check the failed workflow step output.\n", encoding="utf-8")
        return 0

    lines = [clean(line) for line in source.read_text(encoding="utf-8", errors="replace").splitlines()]
    interesting: set[int] = set()

    for index, line in enumerate(lines):
        if not line or is_noise(line):
            continue
        if is_error(line):
            start = max(0, index - CONTEXT_BEFORE)
            end = min(len(lines), index + CONTEXT_AFTER + 1)
            interesting.update(range(start, end))

    # Always retain the end of the Gradle output, where the final task/exception
    # and build result normally appear.
    if lines:
        interesting.update(range(max(0, len(lines) - TAIL_LINES), len(lines)))

    selected: list[str] = []
    last_blank = False
    for index in sorted(interesting):
        line = lines[index]
        if is_noise(line):
            continue
        if not line:
            if not last_blank and selected:
                selected.append("")
            last_blank = True
            continue
        selected.append(line)
        last_blank = False

    # Remove repeated identical lines while preserving useful ordering.
    deduped: list[str] = []
    seen: set[str] = set()
    for line in selected:
        key = line.strip()
        if key and key in seen:
            continue
        if key:
            seen.add(key)
        deduped.append(line)

    # Keep artifact small enough to be easy to download/copy into an issue.
    if len(deduped) > MAX_OUTPUT_LINES:
        deduped = deduped[:MAX_OUTPUT_LINES] + ["", "[error report truncated for readability]"]

    if not deduped:
        deduped = [
            "No standard Gradle error marker was detected.",
            "Inspect the failed step in GitHub Actions for the command that exited non-zero.",
        ]

    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(deduped) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
