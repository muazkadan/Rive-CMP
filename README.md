# Rive CMP

A Compose Multiplatform wrapper library for integrating Rive animations, providing a unified API to use rive-android and rive-ios seamlessly across Android and iOS platforms.

> **⚠️ EXPERIMENTAL STATUS**
> 
> This library is currently in an experimental state. Features, APIs, and implementation details may change significantly or the project might be discontinued. Use at your own risk in production applications.
> 
> **Current Limitations:**
> - Currently only supports loading animations from URLs
> - Not all features and properties from the native Rive libraries are supported yet
> - Additional features like local file loading coming in future releases

## Features

- **Unified API**: Single `CustomRiveAnimation` composable that works across Android and iOS
- **Native Performance**: Uses platform-specific Rive implementations for optimal performance
- **Easy Integration**: Simple Compose-style API with familiar modifier patterns
- **State Machine Support**: Support for Rive state machines on both platforms
- **Flexible Configuration**: Customizable alignment, fit, and playback options

## Platform Support

| Platform | Implementation | Dependency |
|----------|----------------|------------|
| Android  | Native rive-android | `app.rive.runtime.kotlin` |
| iOS      | Swift Package Manager | `rive-ios` via spm4kmp |

## Installation

### Gradle (Kotlin Multiplatform)

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.muazkadan:rive-cmp:0.0.1")
}
```

### Version Catalog

Add to your `libs.versions.toml`:

```toml
[versions]
rive-cmp = "0.0.1"

[libraries]
rive-cmp = { module = "dev.muazkadan:rive-cmp", version.ref = "rive-cmp" }
```

## Basic Usage

```kotlin
import dev.muazkadan.rivecmp.CustomRiveAnimation

@Composable
fun MyScreen() {
    CustomRiveAnimation(
        modifier = Modifier.size(200.dp),
        url = "https://your-rive-animation-url.riv"
    )
}
```

## API Reference

### CustomRiveAnimation

```kotlin
@Composable
fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    url: String,
    stateMachineName: String? = null
)
```

#### Parameters

- `modifier`: Compose modifier for styling and layout
- `url`: Url to the Rive animation file
- `stateMachineName`: Optional name of the state machine to use

## Requirements

### Android
- Minimum SDK: 21
- Target SDK: 34
- Kotlin: 1.9+
- Compose: 1.5+

### iOS
- Minimum iOS: 14.0
- Xcode: 15+
- Swift: 5.9+

## Building

This library uses Kotlin Multiplatform with the following plugins:
- `kotlinMultiplatform`
- `androidLibrary`
- `composeMultiplatform`
- `composeCompiler`
- `spmForKmp` (for iOS Swift Package Manager integration)

```bash
# Build all targets
./gradlew build

# Build Android AAR
./gradlew :library:assembleRelease

# Build iOS Framework
./gradlew :library:linkReleaseFrameworkIosArm64
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

1. Clone the repository
2. Open in Android Studio or IntelliJ IDEA
3. Sync Gradle dependencies
4. For iOS development, ensure Xcode is installed

## License

```
Copyright 2025 Muaz KADAN

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Author

**Muaz KADAN**
- Website: [muazkadan.dev](https://muazkadan.dev/)
- Email: muaz.kadan@gmail.com
- GitHub: [@muazkadan](https://github.com/muazkadan)

## Acknowledgments

- [Rive](https://rive.app/) for the amazing animation platform
- [rive-android](https://github.com/rive-app/rive-android) for Android implementation
- [rive-ios](https://github.com/rive-app/rive-ios) for iOS implementation
- [spm4kmp](https://github.com/frankois944/spm4kmp) for Swift Package Manager integration


