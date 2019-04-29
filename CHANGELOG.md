# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.1.2-SNAPSHOT]

### Added

- Add digital object management (digital objects and contained file resources)
- Add entity to entities relations
- Add entity to file resources relations
- CHANGELOG.md

### Changed

- Migrate from Bootstrap 3 to Bootstrap 4
- Migrate database schema and SQL from multiple joins to PostgreSql inheritance (where applicable)

## [3.1.1](https://github.com/dbmdz/cudami/releases/tag/3.1.1) - 2019-01-23

### Changed

- Fix webpage endpoints for v1 of the model

## [3.1.0](https://github.com/dbmdz/cudami/releases/tag/3.1.0) - 2019-01-23

### Added

- Add support for multi-language contents
- Add logic for locale specific content retrieval
- Introduce docker compose setup for local development
- Add hierarchical creation of webpages
- Add creation of content nodes
- Add automatic snapshot deployments to sonatype nexus
- Introduce articles
- Add new objects to main page and new section resources
- Introduce adding of identifiables to Article, ContentNode and Webpage
- Add file upload functionality
- Add openjdk11 to build matrix
- Add endpoint versioning
- Add new template fragments for rendering of identifiables

### Changed

- Refactor to use new dc-model
- Fix query for retrieving the sort index
- Fix webapp and actuator security configuration for the admin webapp
- Fix rendering of marks
- Bump versions of various dependencies
- Fix badges

### Removed

- Remove no longer used model module
