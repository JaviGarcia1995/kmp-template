#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_PACKAGE="com.example.kmptemplate"
BASE_PACKAGE_PATH="$(printf '%s' "$BASE_PACKAGE" | tr '.' '/')"
BASE_PACKAGE_REGEX="${BASE_PACKAGE//./\\.}"
COMMON_MAIN_DIR="$ROOT_DIR/shared/src/commonMain/kotlin/$BASE_PACKAGE_PATH"
DOMAIN_DIR="$COMMON_MAIN_DIR/domain"
DATA_DIR="$COMMON_MAIN_DIR/data"
PRESENTATION_DIR="$COMMON_MAIN_DIR/presentation"
UI_DIR="$COMMON_MAIN_DIR/ui"
NAV_DIR="$COMMON_MAIN_DIR/navigation"

FAILED=0

search_code() {
  local pattern="$1"
  local target="$2"

  if command -v rg >/dev/null 2>&1; then
    rg -n "$pattern" "$target"
  else
    grep -R -n -E --include="*.kt" --include="*.kts" "$pattern" "$target"
  fi
}

check_no_forbidden_imports() {
  local target_dir="$1"
  local label="$2"
  local forbidden_pattern="$3"
  local message="$4"

  if search_code "^import ${BASE_PACKAGE_REGEX}\\.(${forbidden_pattern})\\." "$target_dir"; then
    echo ""
    echo "[ARCH GUARD] $label $message"
    FAILED=1
  fi
}

check_no_forbidden_apis() {
  local pattern="$1"
  local message="$2"

  if search_code "$pattern" "$COMMON_MAIN_DIR"; then
    echo ""
    echo "[ARCH GUARD] $message"
    FAILED=1
  fi
}

check_no_forbidden_imports "$DOMAIN_DIR" "domain/**" "data|presentation|ui|navigation|database|di" "must not import outer layers."
check_no_forbidden_imports "$DATA_DIR" "data/**" "presentation|ui|navigation" "must not depend on presentation or UI."
check_no_forbidden_imports "$PRESENTATION_DIR" "presentation/**" "data|database" "must depend on domain contracts, not data implementations."
check_no_forbidden_imports "$UI_DIR" "ui/**" "domain|data" "must use presentation contracts."
check_no_forbidden_imports "$NAV_DIR" "navigation/**" "domain|data" "must use presentation contracts."

check_no_forbidden_apis "Dispatchers\\.(IO|Main|Default|Unconfined)" "commonMain must use DispatcherProvider instead of direct dispatchers."
check_no_forbidden_apis "Clock\\.System\\.now\\(" "commonMain must use TimeProvider instead of reading system time directly."

if [[ "$FAILED" -ne 0 ]]; then
  exit 1
fi

echo "[ARCH GUARD] OK: no layer violations or forbidden APIs found in commonMain."
