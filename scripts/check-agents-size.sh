#!/bin/sh
set -eu

repository=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
target_file="${1:-$repository/AGENTS.md}"

if [ ! -f "$target_file" ]; then
  echo "Error: AGENTS.md not found at '$target_file'" >&2
  exit 1
fi

line_count=$(wc -l < "$target_file" | tr -d ' ')
max_lines=500

if [ "$line_count" -gt "$max_lines" ]; then
  echo "Error: $target_file exceeds $max_lines lines (current: $line_count)." >&2
  echo "Instrucción: Por favor, separa el fichero en varios más pequeños enlazados por categoría." >&2
  exit 1
fi

echo "AGENTS.md line count check passed ($line_count/$max_lines lines)."
exit 0
