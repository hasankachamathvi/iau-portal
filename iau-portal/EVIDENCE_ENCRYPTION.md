Evidence encryption
===================

This project supports AES-256-GCM encryption for uploaded evidence files.

How it works
- The application stores encrypted file blobs to disk. Each encrypted file contains a 12-byte IV prefix followed by AES-GCM ciphertext.
- The encryption key is expected as a Base64-encoded 32-byte (256-bit) key.

Configuration
- Set the Spring property `evidence.encryption.key` to the Base64 key in your `application-*.properties` or pass as env var `EVIDENCE_ENCRYPTION_KEY_BASE64`.

Generate a key (example):
```powershell
# Generate a 32-byte key and output as base64 (Windows PowerShell)
[Convert]::ToBase64String((New-Object Byte[](32) | ForEach-Object {Get-Random -Maximum 256}))
```

Or using OpenSSL on *nix:
```bash
openssl rand -base64 32
```

Notes
- Keep the key secure; rotate keys carefully and coordinate re-encryption if needed.
- For production use, store the key in a secrets manager (Azure Key Vault, AWS KMS, HashiCorp Vault) and inject via environment variable or externalized configuration.
