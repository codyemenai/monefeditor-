# Monef Editor

Monef Editor is a native Android code editor built with Kotlin and Jetpack Compose.

## What is included
- Basic editor screen
- Multi-tab concept through ViewModel state
- Search and replace support
- Auto-indent toggle
- Session save/restore concept
- No AI features included

## Project structure
- app/src/main/java/com/example/monefeditor/MainActivity.kt
- app/src/main/java/com/example/monefeditor/ui/EditorScreen.kt
- app/src/main/java/com/example/monefeditor/ui/EditorViewModel.kt

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
This repository is intended to be a solid foundation for a mobile-first code editor with future support for syntax highlighting, folding, file tree, and plugins.
