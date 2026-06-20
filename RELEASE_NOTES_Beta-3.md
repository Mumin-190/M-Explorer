# M-Explorer Release Notes

## Version Beta-3

We are incredibly excited to announce the **Beta-3** release of M-Explorer! 

This release delivers massive performance milestones, comprehensive security audits, and a greatly simplified user experience over Beta-2.

### What's New

- **Lightning-Fast Categories**: The Home tab's categories (Images, Audio, Videos, Documents) now leverage the Android MediaStore. Say goodbye to slow loading spinners—massive media libraries now load almost instantly.
- **Infinite Scrolling Pagination**: Loading massive folders no longer crashes the app. Directory loading has been refactored to implement memory-efficient, chunked pagination directly within the list's scroll views.
- **Shizuku Security Overhaul**: Root file management has been completely sandboxed via Shizuku IPC, eliminating legacy and vulnerable `sh` command strings.
- **Enterprise-Grade Network Security**: Remote SFTP connections now fully enforce Trust-On-First-Use (TOFU) host key fingerprinting, and all of your SSH keys and passwords are now encrypted at rest within the Android Keystore.
- **Simplified Settings**: We have drastically cleaned up the Settings menu, removing bloat and confusing enums in favor of direct toggles like "Enable Root Access", and straightforward UI configurations like "Pitch Dark Mode".
- **Crash & Stability Fixes**: Resolved severe bugs affecting Navigation Fragment lifecycles and background threading for thumbnail rendering.

### Upgrade Instructions
Simply install the latest `Beta-3` APK over your existing `Beta-2` installation. Your existing remote servers and internal preferences will be seamlessly migrated.

Thank you for using M-Explorer!
