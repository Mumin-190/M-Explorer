# Changelog

All notable changes to this project will be documented in this file.

## [Beta-3] - Release Candidate

### Navigation & UI
- **Redesigned Navigation**: Overhauled the Home, Browse, and Server tabs for intuitive contextual routing.
- **Home Screen Layout**: Storage items are now rigidly and cleanly ordered (Internal Storage, Root, Connected Servers).
- **Settings Simplification**: Cleaned up the settings menu. Removed redundant Storage ordering, Default Folder configs, and Archive Encodings.
- **Root Toggle**: Replaced complex Root Strategy enums with a straightforward "Enable Root Access" toggle.
- **Theme Overhaul**: Renamed and refined "Dark Mode" and "Pitch Dark Mode" settings.

### File Loading & Performance
- **MediaStore Categories**: Completely migrated Home categories (Downloads, Images, Audio, Videos, Documents) from recursive filesystem scanning to near-instant Android MediaStore queries.
- **List Pagination**: Implemented memory-efficient scroll-based pagination limiters for massive directories, completely preventing OutOfMemory exceptions.
- **Scroll Bug Fixes**: Resolved critical `RecyclerView` jumping/recomposition bugs during paginated loading sequences.
- **Thumbnail Optimizations**: Restricted the Coil ImageLoader memory cache to 15% RAM limit and 2% disk cache, decoupling preview generation from the main thread.

### Root & Shizuku
- **Shizuku Integration**: Implemented strict `Shizuku.pingBinder()` lifecycle checks.
- **Secure File Provider**: Safely sandboxed Root operations through IPC to completely avoid legacy `sh -c` shell injections.
- **On-Demand Root**: Eliminated passive, global root initializations. Shizuku requests are scoped securely to explicit user intent.

### SFTP & Network Servers
- **SFTP Server**: Stabilized internal SFTP server hosting functionality.
- **SFTP Client Support**: Full compatibility with modern SSH keys (Ed25519, ECDSA).
- **TOFU Fingerprinting**: Implemented strict Trust-On-First-Use (TOFU) host-key verifiers to prevent Man-in-the-Middle (MITM) attacks.
- **Credential Security**: Completely eliminated plain-text credential storage. All passwords and SSH keys are now securely wrapped inside Android's native `EncryptedSharedPreferences`.
- **Server Status Check**: Improved background polling for connected server latency and availability statuses.

### Bug Fixes
- Fixed a major regression where switching between the Home Root view and Browse tab caused application crashes due to shared view models.
- Resolved SFTP shortcut navigation mismatches sending users to the wrong tab contexts.
- Removed SQLite limits that crashed devices running older Android configurations during MediaStore sorts.
- Stabilized generic ListAdapter `DiffUtil` updates to remove UI flashing.
