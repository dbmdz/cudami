# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.2.1](https://github.com/dbmdz/cudami/releases/tag/3.2.1) - 2019-11-13

### Changed

- Fixed the sorting of the language tabs
- Fixed language switch

### Removed

- Removed placeholder from editor when content is empty

## [3.2.0](https://github.com/dbmdz/cudami/releases/tag/3.2.0) - 2019-10-28

### Added

- Added digital object management (digital objects and contained file resources)
- Added entity to entities relations
- Added entity to file resources relations
- Added `CHANGELOG.md`
- Added spotbugs code checks
- Added documentation how to configure and use client
- Added deploy stage to ci configuration
- Added digital objects
- Refactored docker-compose setup to include a iiif server
- Added identifier types management
- Added `LinkedDataFileResource`
- Added auto-formatting via google style
- Added prometheus and some more monitoring endpoints
- Added management of `Collection`, `Corporation` and `Project`

### Changed

- Migrated from Bootstrap 3 to Bootstrap 4
- Migrated database schema and SQL from multiple joins to PostgreSql inheritance (where applicable)
- Bumped versions of various dependencies
- Fixed batch execution to avoid already closed connection
- Fixed link rendering (missing blank after opening a tag)
- Refactored from deprecated jdbi `findOnly` to `findOne`
- Refactored `label`, `description` and `text` of identifiables to an easier data structure
- Refactored locale to language
- Refactored the editor to `React`
- Added simplified version of client
- Added webjar versions from `pom`
- Refactored logging configuration to log to `STDOUT`
- Fixed `v2` endpoint for retrieving webpages and add `v3`

### Removed

- Removed `OracleJDK` from build matrix

## [3.1.1](https://github.com/dbmdz/cudami/releases/tag/3.1.1) - 2019-01-23

### Changed

- Fixed webpage endpoints for `v1` of the model

## [3.1.0](https://github.com/dbmdz/cudami/releases/tag/3.1.0) - 2019-01-23

### Added

- Added support for multi-language contents
- Added logic for locale specific content retrieval
- Introduced docker compose setup for local development
- Added hierarchical creation of webpages
- Added creation of content nodes
- Added automatic snapshot deployments to sonatype nexus
- Introduced articles
- Added new objects to main page and new section resources
- Introduced adding of identifiables to `Article`, `ContentNode` and `Webpage`
- Added file upload functionality
- Added `openjdk11` to build matrix
- Added endpoint versioning
- Added new template fragments for rendering of identifiables

### Changed

- Refactored to use new `dc-model`
- Fixed query for retrieving the sort index
- Fixed webapp and actuator security configuration for the admin webapp
- Fixed rendering of marks
- Bumped versions of various dependencies
- Fixed badges

### Removed

- Removed no longer used model module
