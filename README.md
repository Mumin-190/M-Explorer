# M-Explorer

An open-source Material Design file manager for Android 5.0+, built with premium UX, Google Files-style architecture, and integrated network sharing features.

## Preview

<p>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="32%" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="32%" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="32%" />
</p>

## Key Features

- **Google Files UI Layout**: 
  - **Home**: Dashboard displaying storage cards, connection statuses, and interactive file categories (Downloads, Images, Videos, Audio, Documents, Apps).
  - **Browse**: Intuitive exploration of your internal storage.
  - **Server**: Quick configuration and controls for sharing protocols.
- **Junk Cleaner**: Built-in caching utility to scan, analyze, and clear unused app resources and temporary data in one tap.
- **NAS & Network Support**: Integrated controls to explore and host FTP and SFTP configurations, as well as SMB and WebDAV network mounts.
- **Modern Navigation**: Fully integrates predictive back gestures and standard Material Design transitions.
- **Linux-Aware Engine**: Standard system-level file operations implementing direct bindings to Linux syscalls, with support for symbolic links, file permissions, and SELinux contexts.
- **Secure & Robust**: Implements high-performance desugared Java NIO2 File APIs alongside decoupled ViewModel architecture.

## Architecture & Design Decisions

### Decoupled Backend
By avoiding standard parsed output methods (like standard `ls` parses) and ancient `java.io.File` limitations, M-Explorer leverages custom Unix filesystem bindings. This ensures maximum file transfer performance, robust error management, and proper representation of file names with invalid character sets.

### User Experience
M-Explorer prioritizes clean visual structures, smooth animations, dynamic color themes (including true black night mode), and seamless gesture navigation.

## License

This project is licensed under the GNU General Public License v3.0. See the `LICENSE` file for details.
