# IAU Portal — Database Migration & Flyway

This repository includes SQL migration scripts under `src/main/resources/db/migration/`.
By adding `flyway-core` to the project, Flyway will run migrations automatically at application startup when connected to the configured database.

Recommended usage:

- Configure your `application-*.properties` with the DB connection details (JDBC URL, username, password).
- Provide the environment-specific `spring.profiles.active` property or patch the properties file used by your environment.
- Keep migration files immutable once applied in production; use new `V{n}__description.sql` files for schema changes.

Apply migrations manually (if you prefer):

```bash
# Using flyway CLI (optional):
flyway -url="jdbc:mysql://host:3306/dbname" -user=username -password=secret migrate
```

Or let Spring Boot + Flyway run them on startup (default behaviour).

Note: Ensure `evidence.encryption.key` is set in your production config before starting the app to ensure evidence files are encrypted on upload.
