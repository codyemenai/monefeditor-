# Monef Editor

Monef Editor is a mobile-first Android code editor inspired by the spirit of Notepad++ but tailored for phones and tablets.

## Included features
- Multi-tab editing experience
- Search and replace with optional regex
- Auto-indent support
- Session save/restore
- Save, open, and delete text files using internal storage
- Basic syntax-aware text styling
- Theme toggle and font-size control
- File picking from the device for real text files
- Mobile-friendly Compose UI
- No AI features inside the editor

## Project structure
- app/src/main/java/com/example/monefeditor/MainActivity.kt
- app/src/main/java/com/example/monefeditor/ui/EditorScreen.kt
- app/src/main/java/com/example/monefeditor/ui/EditorViewModel.kt
- app/src/main/java/com/example/monefeditor/data/InternalStorageTextFileRepository.kt
- app/src/main/java/com/example/monefeditor/domain/TextFileRepository.kt

## Requirements
- JDK 17
- Android SDK with API 35 installed
- Gradle 8.10.2 (wrapper included)

## Build locally
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew assembleDebug
```

## Notes
The app is structured for future expansion with syntax highlighting, folding, a file tree, and plugin support.
