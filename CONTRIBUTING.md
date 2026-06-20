# Contributing to M-Explorer

First off, thank you for considering contributing to M-Explorer! It's people like you that make open-source software such a great community.

## How Can I Contribute?

### Reporting Bugs
If you find a bug, please create an issue in the repository. Provide as much detail as possible, including:
- Your device model and Android version.
- M-Explorer version.
- Steps to reproduce the bug.
- Logcat output (if applicable).

### Suggesting Enhancements
Feature requests are always welcome! When proposing a new feature, please explain:
- What the feature is.
- Why it is useful to the broader user base.
- How it might be integrated into the existing UI/UX (Home, Browse, Settings, etc.).

### Pull Requests
We accept pull requests for bug fixes, performance improvements, and new features.

1. **Fork the repo** and create your branch from `main`.
2. **If you've added code**, ensure it complies with the existing Kotlin styling standards.
3. **If your PR modifies the UI**, please attach screenshots of the before and after states.
4. **Testing**: Run the application locally and verify that existing modules (MediaStore pagination, Shizuku bindings, SFTP Client) have not regressed.
5. **Commit Messages**: Write clear, descriptive commit messages.

## Development Setup
- Use the latest stable version of Android Studio.
- Ensure you have the Android SDK required by the `build.gradle` file.
- We recommend testing on an emulator running API 33+ and a physical device running Shizuku for root feature testing.

Thank you for your support!
