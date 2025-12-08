# Config files

This folder contains local configuration files for the application. For security reasons, the actual config files are not committed to the repository (they are listed in `.gitignore`).

How to use:
1. Copy `db.properties.example` to `db.properties` and provide your real database credentials.
2. Copy `api.properties.example` to `api.properties` and provide the real API secrets and credentials.
3. Never commit `db.properties` or `api.properties` to the repository.

To prevent accidental commits of secrets consider running:

```
pre-commit install
```

This repository includes a pre-commit configuration based on `detect-secrets` which will help flag accidental secrets during commit.
