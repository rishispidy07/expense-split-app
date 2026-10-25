#!/bin/bash
# ExpenseSplit App - Run Script (Linux / macOS / Termux / WSL)
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND="$ROOT/backend"
LIB="$BACKEND/lib"
OUT="$BACKEND/bin"

echo "========================================"
echo "  ExpenseSplit App - Starting Server"
echo "========================================"

if [ ! -d "$OUT" ]; then
    echo "[ERROR] Build not found. Run ./build.sh first."
    exit 1
fi

# Build classpath (output dir + all jars in backend/lib)
CLASSPATH="$OUT"
for f in "$LIB"/*.jar; do
    [ -f "$f" ] && CLASSPATH="$CLASSPATH:$f"
done

echo "Starting server on port 8080..."
echo "Access at: http://localhost:8080"
echo "Press Ctrl+C to stop."
echo "========================================"

# Run from the repo root so Config.STATIC_FILES_DIR ("frontend") resolves.
cd "$ROOT"
java -cp "$CLASSPATH" com.expensesplit.App
