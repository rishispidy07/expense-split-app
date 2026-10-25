#!/bin/bash
# ExpenseSplit App - Build Script (Linux / macOS / Termux / WSL)
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND="$ROOT/backend"
SRC="$BACKEND/src"
LIB="$BACKEND/lib"
OUT="$BACKEND/bin"

echo "========================================"
echo "  ExpenseSplit App - Build Script"
echo "========================================"
echo ""

# Check Java
if ! command -v javac &> /dev/null; then
    echo "[ERROR] javac (JDK) is not installed. Install a JDK, then retry."
    exit 1
fi

echo "[1/3] Compiling Java sources..."
mkdir -p "$OUT"

# Build classpath (output dir + all jars in backend/lib)
CLASSPATH="$OUT"
for f in "$LIB"/*.jar; do
    [ -f "$f" ] && CLASSPATH="$CLASSPATH:$f"
done

find "$SRC" -name "*.java" > "$BACKEND/sources.txt"
javac -encoding UTF-8 -cp "$CLASSPATH" -d "$OUT" @"$BACKEND/sources.txt"
echo "[OK] Compilation successful"

echo "[2/3] Copying resources..."
cp "$LIB"/*.jar "$OUT"/ 2>/dev/null || true

echo "[3/3] Build complete!"
echo ""
echo "To run: ./run.sh"
