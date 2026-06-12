#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MARKER_FILE="$ROOT_DIR/.template-configured"

TEMPLATE_PROJECT_NAME="KmpTemplate"
TEMPLATE_PROJECT_KEY="kmptemplate"
TEMPLATE_PACKAGE="com.example.kmptemplate"
TEMPLATE_PACKAGE_PATH="com/example/kmptemplate"

fail() {
  echo "Error: $*" >&2
  exit 1
}

validate_project_name() {
  [[ "$1" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]]
}

validate_package_name() {
  [[ "$1" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]
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

[[ ! -e "$MARKER_FILE" ]] || fail "this template has already been configured."
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
  fail "package must contain at least two lowercase dot-separated segments."

NEW_PROJECT_KEY="$(printf '%s' "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]')"
NEW_PACKAGE_PATH="$(printf '%s' "$NEW_PACKAGE" | tr '.' '/')"

echo
echo "Project name: $PROJECT_NAME"
echo "Package:      $NEW_PACKAGE"
echo
read -r -p "Apply these changes? [y/N]: " CONFIRMATION
[[ "$CONFIRMATION" == "y" || "$CONFIRMATION" == "Y" ]] || fail "setup cancelled."

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
NEW_SWIFT_APP="$ROOT_DIR/iosApp/iosApp/${PROJECT_NAME}App.swift"
if [[ -f "$OLD_SWIFT_APP" ]]; then
  mv "$OLD_SWIFT_APP" "$NEW_SWIFT_APP"
fi

replace_in_file "$ROOT_DIR/androidApp/src/main/res/values/strings.xml" "KMP Notes" "$PROJECT_NAME"
replace_in_file "$ROOT_DIR/iosApp/iosApp/Info.plist" "KMP Notes" "$PROJECT_NAME"

cat > "$MARKER_FILE" <<EOF
Project name: $PROJECT_NAME
Package: $NEW_PACKAGE
Configured by: scripts/setup-template.sh
EOF

echo
echo "Template configured."
echo "Next steps:"
echo "  1. Review the generated changes."
echo "  2. Ensure local.properties or ANDROID_HOME points to your Android SDK."
echo "  3. Run ./scripts/check-layer-boundaries.sh and the platform builds."
