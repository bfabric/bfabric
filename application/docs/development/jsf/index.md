# B-Fabric — JavaServer Faces

This page explains how JavaServer Faces (JSF) is used in the project, where to configure it, which UI library we use and a few practical tips for development.

## Key files and places

- `WEB-INF/faces-config.xml` — Optional central JSF configuration for navigation rules, component registration and other JSF settings. Modern code often uses annotations (for beans, components and converters) instead of extensive XML configuration; keep `faces-config.xml` for settings that cannot be expressed by annotations or for global configuration.
- JSF pages — Facelets XHTML pages (usually `*.xhtml`) under the web application webapp root.
- Component classes — Custom components, renderers, converters and validators live in the usual Java source tree and can be registered via annotations or `faces-config.xml`.

## Libraries

- UI library: PrimeFaces (`https://www.primefaces.org/`). Use the project-approved PrimeFaces version and add the dependency in `pom.xml`.
- Prefer Facelets for templating and component composition (use `.xhtml` with the PrimeFaces XML namespace, e.g. `xmlns:p="http://primefaces.org/ui"`).

## Common configuration tips

- Enable development diagnostics during development:
    - Set the JSF project stage (`javax.faces.PROJECT_STAGE`) to `Development` in `web.xml` or via environment-specific configuration.
- Prefer CDI or JSF managed beans with appropriate scopes (`@RequestScoped`, `@ViewScoped`, `@SessionScoped`) depending on lifecycle needs.
- Use annotation-driven registration where possible (`@FacesComponent`, `@FacesConverter`, `@FacesValidator`) to minimize `faces-config.xml` boilerplate.

## Best practices

- Avoid server-side scriptlets in views; keep logic in beans or services.
- Use component libraries (PrimeFaces) for consistent UX and built-in features.
- Protect against XSS: components derived from `UIOutput` escape by default — keep escaping enabled for untrusted content or sanitize explicitly before rendering.
- Keep long-running work out of JSF request thread; delegate to background tasks or REST endpoints when appropriate.
- Use `exploded` deployment for faster iteration when developing with an application server.

## Troubleshooting

- If pages do not render or components are missing, verify:
    - PrimeFaces dependency is present and on the correct classpath.
    - Faces servlet mapping exists in `web.xml` (or proper servlet configuration is present).
    - No conflicting JSF implementations are bundled.
- Check server logs and enable JSF debug output when diagnosing lifecycle or rendering issues.

## References

- PrimeFaces: `https://www.primefaces.org/`
- JSF / Facelets overview: the Jakarta Faces and Facelets documentation for the JSF version used by the project.