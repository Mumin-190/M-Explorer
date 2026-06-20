# Security Policy

Security is a primary design pillar for M-Explorer. Because file managers handle highly sensitive documents, credentials, and system-level modifications, the app employs several security mechanisms.

## Authentication Handling & SFTP Security Model
M-Explorer interacts with remote SFTP and FTP servers. To protect your credentials:
- **Encrypted Storage**: Passwords and private SSH keys are never stored in plaintext. M-Explorer uses the Android Keystore-backed `androidx.security.crypto.EncryptedSharedPreferences` to encrypt all sensitive network credentials at rest.
- **Host Verification (TOFU)**: The SFTP client enforces a Trust-On-First-Use mechanism. Upon your first connection to a server, the host fingerprint is recorded securely. If the remote fingerprint changes unexpectedly in the future, the connection is forcibly blocked to prevent Man-in-the-Middle (MITM) attacks.
- **Modern Cryptography**: The internal SSH/SFTP library natively supports Ed25519 and ECDSA elliptic curve cryptography.

## Root Access Safeguards & Shizuku
M-Explorer interfaces with the Shizuku API to provide root-level file management:
- **No Arbitrary Shell Injections**: The app executes privileged tasks via a robust Java IPC binder, effectively mitigating the severe security flaws associated with executing string-concatenated `sh -c` shell commands.
- **Principle of Least Privilege**: Root access remains completely detached and inactive unless you explicitly enable the "Enable Root Access" toggle. Furthermore, active lifecycle checks (`Shizuku.pingBinder()`) are invoked before every privileged operation. 

## Network Exposure Considerations
When hosting the internal SFTP Server from M-Explorer:
- The server binds to the port you specify (default is often `2222`).
- The server is accessible to anyone on your local subnet. Please ensure you are connected to a trusted Wi-Fi network and have configured a strong username/password or SSH key requirement before starting the server.

## Reporting a Vulnerability
If you discover a security vulnerability within M-Explorer, please **do not** open a public issue. Instead, contact the repository owner privately or follow the responsible disclosure guidelines provided in the repository's security advisory section.
