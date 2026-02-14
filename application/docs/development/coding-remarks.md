# B-Fabric — Coding Remarks

Follow these rules before committing to keep code quality high and predictable.

## Quick checklist (before commit)
- Fix all IDE errors and warnings (Eclipse, IntelliJ, etc.).
- Fix all FindBugs issues; generate a local report with `mvn clean site`
- Document non-obvious classes, methods, and in-method statements.
- Update the B-Fabric user manual at `bfabric.org` for new or changed features.

## Naming conventions
- Class names: PascalCase (e.g., `MyEntity`).
- Attributes and variables: camelCase (e.g., `sampleValue`).
- URLs: lower-case.
- Prefer concise, meaningful names; avoid single-letter names and unclear abbreviations.
- Database table names: use singular nouns.
    - Many-to-many join tables: use a clear combined name (example: `projectFormerMember`).
## Java Beans architecture (B-Fabric conventions)
Use these roles and naming patterns consistently.

### Entity Beans
- Purpose: represent persistent domain objects with attributes, associations, and entity-specific behavior.
- Requirements:
    - Annotate with `@Entity`.
    - Use appropriate cascade types for associations.
- Naming: PascalCase.

### Service Beans
- Purpose: provide database and business operations for entities (typically one service per entity).
- Requirements:
    - Stateless, container-managed transactions.
    - Annotate with `@Stateless`.
- Naming: append `Service` to the entity name (example: `ProjectService`).

### Manager Beans
- Purpose: handle UI interactions (xhtml), call services, and add Faces messages.
- Requirements:
    - Named, view-scoped beans.
    - Prefer one manager per entity request.
    - Often extend a common `AbstractEntityManager` and initialized with request parameters.
    - Annotate with `@ViewScoped`.
- Naming: append `Manager` to the entity name (example: `ProjectManager`).

### List Beans
- Purpose: cache results of common queries for reuse across views and managers.
- Requirements:
    - Named, view-scoped beans without request parameters so they can be injected safely.
    - Annotate with `@ViewScoped`.
- Naming: append `List` to the entity name (example: `ProjectList`).

## Documentation and code comments
- Add a clear class-level Javadoc for any class whose purpose is not obvious.
- Inside methods, comment any statement that is non-trivial or could confuse future readers.
- Keep comments up to date when code changes.

## Tools and quality gates
- Run unit tests and static analysis locally before pushing.
- Use `mvn clean site` to produce reports (FindBugs, PMD, etc.) and resolve reported issues.
- Fix high- and medium-severity warnings before merging.

## Summary
Be explicit: meaningful names, clear documentation, and consistent bean patterns. Fix IDE and static-analysis issues early to keep the codebase maintainable.