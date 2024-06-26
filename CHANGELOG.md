# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to
[Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] — 2023-06-17

### Added

- Added support for `reitit` as a router, as well as extension points allowing
  other routers to be supported, via the `hype.core/Backend` and
  `hype.core/BackendFactory` protocols.

### Changed

- Forked from b-social/hype.
- All dependencies have been upgraded to the latest available versions.
- All documentation has been updated and corrected.
- `hype.core/base-url-for` has been renamed to `hype.core/base-url` since it
  isn't really a factory based on parameters, instead it is a lookup of the
  request.

## [1.0.0] — 2019-10-18

### Added

- Prepare for 1.0.0 release.

## [0.0.21] — 2019-10-18

### Added

- Support for path template parameters.
- Support for path template parameter key conversion.
- Updated documentation for path template parameters and key conversion.
- Usage in README.

## [0.0.20] — 2019-10-18

### Added

- Further documentation in the Getting Started guide.

## [0.0.19] — 2019-10-18

### Added

- Customisable query parameter key conversion.

### Changed

- Query parameter and query template parameter keys are now camel cased by
  default.

## [0.0.18] — 2019-10-18

### Added

- Full documentation including Getting Started guide.

## [0.0.17] — 2019-09-10

### Added

- Support for query parameters and query template parameters to
  `absolute-url-for`.
- An `absolute-path-for` function for generating path component only.
- An `absolute-path->absolute-url` function for resolving an absolute path
  against the base URL from a request.

### Changed

- The signature of `absolute-url-for` (breaking).

### Removed

- `parameterised-url-for` since `absolute-url-for` supersedes its functionality.

## [0.0.16] — 2019-09-10

Released without _CHANGELOG.md_.

[0.0.16]: https://github.com/b-social/hype/compare/0.0.1...0.0.16

[0.0.17]: https://github.com/b-social/hype/compare/0.0.16...0.0.17

[0.0.18]: https://github.com/b-social/hype/compare/0.0.17...0.0.18

[0.0.19]: https://github.com/b-social/hype/compare/0.0.18...0.0.19

[0.0.20]: https://github.com/b-social/hype/compare/0.0.19...0.0.20

[0.0.21]: https://github.com/b-social/hype/compare/0.0.20...0.0.21

[1.0.0]: https://github.com/b-social/hype/compare/0.0.21...1.0.0

[2.0.0]: https://github.com/b-social/hype/compare/1.0.0...2.0.0
[Unreleased]: https://github.com/b-social/hype/compare/2.0.0...HEAD
