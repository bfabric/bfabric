# B-Fabric Documentation

## B-Fabric System Setup

This page describes the steps needed to install and configure a working B-Fabric system on a Debian-based 64-bit OS. The instructions focus on Debian, but can be adapted for other Linux distributions, Windows, or macOS.

Minimum hardware and storage
- At least 8 GB RAM for a development environment.
- For a new installation, plan for ~10 GB persistent storage; storage needs grow with use.

Core components
- `GlassFish` — runs the B-Fabric web application.
- `Postgres` — stores the application's persistent data.
- `Local Repository` — file storage used by B-Fabric.
- `Postfix` — sends outgoing e-mails.
- `SGE Client` — submits jobs to a cluster.

Pre-installation notes
- Follow the required versions exactly when specified (e.g., Postgres, DB drivers). Some components may work with newer versions, but compatibility is not guaranteed.
- Version constraints vary: some packages require a minimum version, others require a specific version.
- The provided environment variable examples assume the Bash shell. If you use a different shell or OS, set environment variables accordingly.
- Decide which OS user will run GlassFish/B-Fabric before installing. Do not run the web application as `root`. SGE jobs run as the submitting user, so the application user must exist and have the proper cluster permissions. This guide uses user `bfabric` for GlassFish.
- Installation order is flexible; the guide presents one sensible order based on component dependencies.

Environment variables used in this guide
- `$BFABRIC_CODE` — path to the code checkout
- `$BFABRIC_CONF` — path for configuration files
- `$GLASSFISH_HOME` — GlassFish installation directory
- `$ECLIPSE_HOME` — Eclipse installation directory