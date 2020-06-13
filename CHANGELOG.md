# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- [2020-06-13] Allow for the two log levels to be configurable by [@lflare].

### Changed
- [2020-06-13] Modified AsyncAppender queue size to 1024 by [@lflare].

### Deprecated

### Removed

### Fixed

## [1.0.0-RC14] - 2020-06-12
### Fixed
- [2020-06-12] Fixed not actually creating the directories before moving cache files by [@lflare].

## [1.0.0-RC13] - 2020-06-12
### Added
- [2020-06-12] Added CHANGELOG.md by [@lflare].
- [2020-06-12] Added on-read atomic image migrator to 4-deep subdirectory format by [@lflare].

### Changed
- [2020-06-12] Raised ApacheClient socket limit to `2**18` by [@lflare].
- [2020-06-12] Changed gradle versioning to using `git describe` by [@lflare].
- [2020-06-12] Made Netty thread count global instead of per-cpu by [@lflare].
- [2020-06-12] Store cache files in a 4-deep subdirectory to improve performance by [@lflare].

### Fixed
- [2020-06-12] Re-added missing default `threads_per_cpu` setting by [@lflare].
- [2020-06-12] Replaced exponential calculation for ApacheClient threads by [@lflare].

### Security
- [2020-06-12] Update ClientSettings.java changed showing client secret in logs back to hidden by [@dskilly].

## [1.0.0-RC12] - 2020-06-12
### Fixed
- [2020-06-12] Fixed hourly refresh bug by [@carbotaniuman].

## [1.0.0-RC11] - 2020-06-11
### Added
- [2020-06-11] New setting `threads_per_cpu` to faciliate with Netty multi-threading by [@lflare].

### Changed
- [2020-06-11] Swapped threading to Netty instead of ApacheClient by [@lflare].

### Fixed
- [2020-06-11] Tweaked logging configuration to reduce log file sizes by [@carbotaniuman].

[Unreleased]: https://gitlab.com/mangadex/mangadex_at_home/compare/v1.0.0-rc14...HEAD
[1.0.0-rc14]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc13...1.0.0-rc14
[1.0.0-rc13]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc12...1.0.0-rc13
[1.0.0-rc12]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc11...1.0.0-rc12
[1.0.0-rc11]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc10...1.0.0-rc11
