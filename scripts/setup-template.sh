#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MARKER_FILE="$ROOT_DIR/.template-configured"

TEMPLATE_PROJECT_NAME="KmpTemplate"
TEMPLATE_PROJECT_KEY="kmptemplate"
TEMPLATE_PACKAGE="com.example.kmptemplate"
TEMPLATE_PACKAGE_PATH="com/example/kmptemplate"
CHANGES_STARTED=0

REQUIRED_COMMANDS="awk cat dirname find git grep mkdir mv rm rmdir tr"
KOTLIN_KEYWORDS="
  as break class continue do else false for fun if in interface is null object package return
  super this throw true try typealias typeof val var when while by catch constructor delegate
  dynamic field file finally get import init param property receiver set setparam where actual
  abstract annotation companion const crossinline data enum expect external final infix inline
  inner internal lateinit noinline open operator out override private protected public reified
  sealed suspend tailrec vararg
"

fail() {
  echo "Error: $*" >&2
  on_error
  exit 1
}

on_error() {
  if [[ "$CHANGES_STARTED" -eq 1 ]]; then
    echo >&2
    echo "Setup stopped after modifying files." >&2
    echo "Rollback from the repository root with:" >&2
    echo "  git restore ." >&2
    echo "  git clean -fd" >&2
    CHANGES_STARTED=0
  fi
}

trap on_error ERR

require_commands() {
  local command
  for command in $REQUIRED_COMMANDS; do
    command -v "$command" >/dev/null 2>&1 ||
      fail "required command '$command' is not available."
  done
}

is_kotlin_keyword() {
  local candidate="$1"
  local keyword

  for keyword in $KOTLIN_KEYWORDS; do
    [[ "$candidate" != "$keyword" ]] || return 0
  done
  return 1
}

validate_project_name() {
  [[ "$1" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]]
}

validate_package_name() {
  [[ "$1" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] || return 1

  local segments=()
  local segment
  local old_ifs="$IFS"
  IFS='.'
  read -r -a segments <<< "$1"
  IFS="$old_ifs"

  for segment in "${segments[@]}"; do
    if is_kotlin_keyword "$segment"; then
      return 1
    fi
  done
}

replace_in_file() {
  local file="$1"
  local search="$2"
  local replacement="$3"
  local temporary_file="${file}.template-setup-tmp"

  awk -v search="$search" -v replacement="$replacement" '
    {
      line = $0
      output = ""
      while ((position = index(line, search)) > 0) {
        output = output substr(line, 1, position - 1) replacement
        line = substr(line, position + length(search))
      }
      print output line
    }
  ' "$file" > "$temporary_file"
  cat "$temporary_file" > "$file"
  rm -f "$temporary_file"
}

replace_in_matching_files() {
  local search="$1"
  local replacement="$2"
  shift 2

  local file
  while IFS= read -r file; do
    if grep -q "$search" "$file"; then
      replace_in_file "$file" "$search" "$replacement"
    fi
  done < <(
    find "$ROOT_DIR" \
      -type f \
      \( "$@" \) \
      -not -path "$ROOT_DIR/.git/*" \
      -not -path "$ROOT_DIR/.gradle/*" \
      -not -path "$ROOT_DIR/.idea/*" \
      -not -path "$ROOT_DIR/.kotlin/*" \
      -not -path "*/build/*" \
      -not -path "$ROOT_DIR/scripts/setup-template.sh" \
      -not -name "local.properties" \
      -not -name ".template-configured"
  )
}

move_package_tree() {
  local source_root="$1"
  local source="$source_root/$TEMPLATE_PACKAGE_PATH"
  local destination="$source_root/$NEW_PACKAGE_PATH"

  [[ -d "$source" ]] || return

  mkdir -p "$(dirname "$destination")"
  mv "$source" "$destination"

  local directory
  directory="$(dirname "$source")"
  while [[ "$directory" != "$source_root" ]]; do
    rmdir "$directory" 2>/dev/null || break
    directory="$(dirname "$directory")"
  done
}

check_package_collisions() {
  local source_root="$1"
  local destination="$source_root/$NEW_PACKAGE_PATH"

  [[ ! -e "$destination" ]] ||
    fail "destination package path already exists: ${destination#"$ROOT_DIR/"}"
}

audit_template_references() {
  local failed=0
  local file

  while IFS= read -r file; do
    if grep -n -F \
      -e "$TEMPLATE_PROJECT_NAME" \
      -e "$TEMPLATE_PROJECT_KEY" \
      -e "$TEMPLATE_PACKAGE" \
      -e "$TEMPLATE_PACKAGE_PATH" \
      -e "KMP Notes" \
      "$file"; then
      failed=1
    fi
  done < <(
    find "$ROOT_DIR" \
      -type f \
      \( -name "*.kt" -o -name "*.kts" -o -name "*.sq" -o -name "*.md" -o -name "*.xml" \
      -o -name "*.plist" -o -name "*.pbxproj" -o -name "*.swift" -o -name "*.sh" \
      -o -name "*.yaml" -o -name "*.yml" \) \
      -not -path "$ROOT_DIR/.git/*" \
      -not -path "$ROOT_DIR/.gradle/*" \
      -not -path "$ROOT_DIR/.idea/*" \
      -not -path "$ROOT_DIR/.kotlin/*" \
      -not -path "*/build/*" \
      -not -path "$ROOT_DIR/scripts/setup-template.sh" \
      -not -name "local.properties" \
      -not -name ".template-configured"
  )

  if [[ "$failed" -ne 0 ]]; then
    fail "template references remain after setup."
  fi
}

require_commands
git -C "$ROOT_DIR" rev-parse --is-inside-work-tree >/dev/null 2>&1 ||
  fail "run this script from a Git checkout."
[[ ! -e "$MARKER_FILE" ]] || fail "this template has already been configured."
[[ -z "$(git -C "$ROOT_DIR" status --porcelain)" ]] ||
  fail "Git working tree must be clean before setup."
[[ -f "$ROOT_DIR/settings.gradle.kts" ]] || fail "run this script from a valid template checkout."
grep -q "rootProject.name = \"$TEMPLATE_PROJECT_NAME\"" "$ROOT_DIR/settings.gradle.kts" ||
  fail "the template identity is missing or has already been changed."

echo "KMP Template setup"
echo

read -r -p "Project name (for example, MyNotes): " PROJECT_NAME
validate_project_name "$PROJECT_NAME" ||
  fail "project name must start with a letter and contain only letters, numbers, or underscores."

read -r -p "Base package (for example, com.example.mynotes): " NEW_PACKAGE
validate_package_name "$NEW_PACKAGE" ||
  fail "package must use lowercase dot-separated segments and must not contain Kotlin keywords."

NEW_PROJECT_KEY="$(printf '%s' "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]')"
NEW_PACKAGE_PATH="$(printf '%s' "$NEW_PACKAGE" | tr '.' '/')"

[[ "$PROJECT_NAME" != "$TEMPLATE_PROJECT_NAME" ]] ||
  fail "project name must differ from the template name."
[[ "$NEW_PACKAGE" != "$TEMPLATE_PACKAGE" ]] ||
  fail "package must differ from the template package."

check_package_collisions "$ROOT_DIR/androidApp/src/main/kotlin"
check_package_collisions "$ROOT_DIR/shared/src/commonMain/kotlin"
check_package_collisions "$ROOT_DIR/shared/src/commonTest/kotlin"
check_package_collisions "$ROOT_DIR/shared/src/androidMain/kotlin"
check_package_collisions "$ROOT_DIR/shared/src/iosMain/kotlin"
check_package_collisions "$ROOT_DIR/shared/src/commonMain/sqldelight"

NEW_SWIFT_APP="$ROOT_DIR/iosApp/iosApp/${PROJECT_NAME}App.swift"
[[ ! -e "$NEW_SWIFT_APP" ]] ||
  fail "destination Swift file already exists: ${NEW_SWIFT_APP#"$ROOT_DIR/"}"

echo
echo "Project name: $PROJECT_NAME"
echo "Package:      $NEW_PACKAGE"
echo
read -r -p "Apply these changes? [y/N]: " CONFIRMATION
[[ "$CONFIRMATION" == "y" || "$CONFIRMATION" == "Y" ]] || fail "setup cancelled."
CHANGES_STARTED=1

replace_in_matching_files \
  "$TEMPLATE_PACKAGE" \
  "$NEW_PACKAGE" \
  -name "*.kt" -o -name "*.kts" -o -name "*.sq" -o -name "*.md" -o -name "*.xml" \
  -o -name "*.plist" -o -name "*.pbxproj" -o -name "*.sh"

replace_in_matching_files \
  "$TEMPLATE_PACKAGE_PATH" \
  "$NEW_PACKAGE_PATH" \
  -name "*.sh" -o -name "*.md"

replace_in_matching_files \
  "$TEMPLATE_PROJECT_KEY" \
  "$NEW_PROJECT_KEY" \
  -name "*.kt" -o -name "*.kts" -o -name "*.md" -o -name "*.yaml" -o -name "*.yml"

replace_in_matching_files \
  "$TEMPLATE_PROJECT_NAME" \
  "$PROJECT_NAME" \
  -name "*.kt" -o -name "*.kts" -o -name "*.md" -o -name "*.xml" -o -name "*.swift" \
  -o -name "*.plist" -o -name "*.pbxproj" -o -name "*.xcscheme"

move_package_tree "$ROOT_DIR/androidApp/src/main/kotlin"
move_package_tree "$ROOT_DIR/shared/src/commonMain/kotlin"
move_package_tree "$ROOT_DIR/shared/src/commonTest/kotlin"
move_package_tree "$ROOT_DIR/shared/src/androidMain/kotlin"
move_package_tree "$ROOT_DIR/shared/src/iosMain/kotlin"
move_package_tree "$ROOT_DIR/shared/src/commonMain/sqldelight"

OLD_SWIFT_APP="$ROOT_DIR/iosApp/iosApp/${TEMPLATE_PROJECT_NAME}App.swift"
if [[ -f "$OLD_SWIFT_APP" ]]; then
  mv "$OLD_SWIFT_APP" "$NEW_SWIFT_APP"
fi

replace_in_file "$ROOT_DIR/androidApp/src/main/res/values/strings.xml" "KMP Notes" "$PROJECT_NAME"
replace_in_file "$ROOT_DIR/iosApp/iosApp/Info.plist" "KMP Notes" "$PROJECT_NAME"

audit_template_references
"$ROOT_DIR/scripts/check-layer-boundaries.sh"

cat > "$MARKER_FILE" <<EOF
Project name: $PROJECT_NAME
Package: $NEW_PACKAGE
Configured by: scripts/setup-template.sh
EOF
CHANGES_STARTED=0

echo
echo "Template configured."
echo "Next steps:"
echo "  1. Review the generated changes."
echo "  2. Ensure local.properties or ANDROID_HOME points to your Android SDK."
echo "  3. Run ./scripts/check-layer-boundaries.sh and the platform builds."
