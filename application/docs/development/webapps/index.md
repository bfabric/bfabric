# B-Fabric — Web Apps

This page describes WebApps: self-contained applications that integrate with B-Fabric to run arbitrary code on external servers. WebApps can run with or without a UI and interact with B-Fabric using a standardized authentication and invocation flow.

## How a Python WebApp works

When a WebApp built from the Python WebApp Template is launched from B-Fabric:

1. B-Fabric generates a unique, timestamped token and calls the WebApp URL with the token as a request parameter.
2. The WebApp uses the token to authenticate against B-Fabric and receives invocation parameters such as:
    - the job id that tracks the WebApp execution,
    - the invoking user’s username and web service password,
    - the invocation context (entity id and class name).
3. Using the provided job id, the WebApp logs execution details back to B-Fabric via the job log.

This flow enables secure, auditable execution and automatic integration with B-Fabric job tracking.

## Key features of the WebApp template

- Automatic registration of applications
- Automatic job creation and logging
- Entity-bound applications (contextual invocation)
- Automatic data querying and hydration (e.g., Application, Entity, Job, User, Workunit)
- Built-in error reporting and bug report hooks
- Redis queuing support for background tasks
- Automatic charge and resource registration
- Automatic registration of workunits and report linking
- Full authentication flow included

## When to use

Use WebApps to:
- Extend B-Fabric with custom processing pipelines or tools
- Integrate external services or compute platforms
- Provide reproducible, auditable executions tied to entities and jobs

## References

- bfabric_web_apps docs: https://bfabric-docs.gwc-solutions.ch/
- bfabric_web_apps repository: https://github.com/GWCustom/bfabric-web-apps
- WebApp template repository: https://github.com/GWCustom/bfabric-web-app-template
- Example: B-Fabric NextFlow rnaseq: https://github.com/GWCustom/rnaseq
- Example: NextFlow Demultiplex WebApp: https://github.com/GWCustom/bfabric_app_demultiplex
- Example: Sushi WebApp: https://github.com/GWCustom/SushiRunner
- Video guides (deployment and programming): https://www.youtube.com/watch?v=vjKlpi1b83U&list=PLqcpqZOaygmkdTR1ahQBVVB7iZh4-cMnH