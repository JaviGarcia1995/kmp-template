# Pipeline SVG -> CMP / Android / iOS

## 1) Shared UI icon (Compose Multiplatform)
1. Receive `input.svg` and confirm final name: `my_icon`.
2. Generate `my_icon.xml` vector drawable and place it in:
   - `shared/src/commonMain/composeResources/drawable/my_icon.xml`
3. Add generated import in `CustomIcons.kt`:
   - `import kmptemplate.shared.generated.resources.my_icon`
4. Expose composable function:
   - `fun MyIcon(): ImageVector = vector(Res.drawable.my_icon)`
5. Replace/add usage in feature composables.

## 2) Android launcher icon (if requested)
1. Update assets in `androidApp/src/main/res/mipmap-*` for `ic_launcher` and `ic_launcher_round`.
2. Check adaptive icon XML:
   - `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
   - `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
3. Check foreground/background:
   - `androidApp/src/main/res/drawable/ic_launcher_foreground.xml`
   - `androidApp/src/main/res/drawable/ic_launcher_background.xml`
4. Confirm references in `AndroidManifest.xml`:
   - `android:icon="@mipmap/ic_launcher"`
   - `android:roundIcon="@mipmap/ic_launcher_round"`

## 3) iOS AppIcon (if iOS module exists)
1. Look for `iosApp` and `Assets.xcassets/AppIcon.appiconset`.
2. If it exists, generate/update iOS-required sizes and `Contents.json`.
3. If iOS module does not exist, report explicit pending item without inventing structure.

## 4) Simplicity rules
1. Do not create new generic utilities if the change is for a single icon.
2. Keep naming consistent (`snake_case` resource, `PascalCase` function).
3. Limit changes to requested scope (UI icon vs launcher vs both).

## 5) Minimum validation
1. `./gradlew :shared:compileDebugKotlinAndroid`
2. If Android launcher was modified: compile Android app.
3. Manual visual verification on affected screen.
