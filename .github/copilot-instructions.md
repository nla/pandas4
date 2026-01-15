# PANDAS 4 Copilot Instructions

This is PANDAS (Preservation and Digital Archive of National Significance) version 4, a web archiving workflow system used by the National Library of Australia.

## Technology Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **Framework**: Spring Boot 3.5.6
- **ORM**: Hibernate with Hibernate Search 7.2.4
- **Search**: Lucene 9.11.1
- **Databases**: PostgreSQL, Oracle, MariaDB (H2 for tests only)
- **Authentication**: OpenID Connect (Keycloak)
- **Web Crawlers**: Heritrix, Browsertrix, HTTrack
- **Web Archive Tools**: pywb, Bamboo

## Project Structure

- `ui/` - Main Spring Boot web application
- `gatherer/` - Web crawling and gathering components
- `common/` - Shared libraries and utilities
- `delivery/` - Archive delivery components
- `browser/` - Browser-based archiving
- `webrecorder/` - Web recording functionality
- `cli/` - Command-line tools
- `social/` - Social media archiving

## Build and Test Commands

Build the project:
```bash
mvn package
```

Run the application:
```bash
java -jar ui/target/pandas-admin.jar
```

Run tests:
```bash
mvn test
```

## Code Style and Conventions

- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Use Maven compiler parameters (`maven.compiler.parameters=true`)
- Use Java 17 language features where appropriate
- Tests use JUnit and are located in `test/` directories within each module

## Database Considerations

- Use Hibernate annotations for entity mappings
- Support for sequences is required (MySQL not currently supported)
- Queries must support recursive CTEs for hierarchical data
- H2 is used for automated tests but has CTE limitations

## Security

- Use OpenID Connect for authentication
- Sanitize user-generated HTML content using OWASP Java HTML Sanitizer
- Be mindful of security in web archiving contexts (handling external content)

## Dependencies

- Prefer existing dependencies over adding new ones
- Keep Spring Boot and security-related dependencies up to date
- Check for CVEs before adding new dependencies

## Testing

- Write JUnit tests for new functionality
- Tests should work with H2 in-memory database
- Integration tests use the `IT.java` suffix (e.g., `GathererIT.java`)
- Unit tests use the `Test.java` suffix

## Documentation

- Update README.md for major feature changes
- Document configuration environment variables
