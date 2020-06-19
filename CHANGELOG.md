# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- [2020-06-19] Errored out on invalid settings.json tokens [@carbotaniuman]

### Changed
- [2020-06-19] Changed default CPU thread count to `4` by [@lflare].
- [2020-06-19] Removed ability to set log files but increased throughput [@carbotaniuman]

### Deprecated

### Removed

### Fixed

### Security

## [1.0.0-RC19] - 2020-06-18
### Added
- [2020-06-16] Added WebUI versions to constants by [@RedMatriz].
- [2020-06-16] Added WebUI PWA support for mobile by [@RedMatriz].
- [2020-06-16] Added WebUI local data caching [@RedMatriz].

### Changed
- [2020-06-16] Reworked graceful shutdown [@carbotaniuman].
- [2020-06-17] Revamped configuration & units by [@lflare].

### Fixed
- [2020-06-17] Fixed minor typo with threads error logging by [@lflare].

## [1.0.0-RC18] - 2020-06-16
### Changed
- [2020-06-16] Changed log level of response timings to INFO by [@lflare].
- [2020-06-16] Added server ping logging [@carbotaniuman].
- [2020-06-16] Added access control headers by [@Fugi].

## [1.0.0-RC17] - 2020-06-15
### Added
- [2020-06-15] Added logging of backend assigned URL to logs by [@lflare].
- [2020-06-15] Added logging of `compromised` softban to logs by [@lflare].

### Changed
- [2020-06-14] Migrated cache metadata over to a sqlite3 handler [@carbotaniuman].
- [2020-06-15] Properly describe dirty builds as dirty by [@lflare].

### Deprecated
- [2020-06-14] Removed old cache subdirectory migration system by [@carbotaniuman].

### Fixed
- [2020-06-14] Switched cache metadata over to a MySql instance [@carbotaniuman].
- [2020-06-15] Fixed tokenized data-saver parser not working by [@lflare].
- [2020-06-15] Properly synchronised sqlite3 handler across threads by [@lflare].

## [1.0.0-RC16] - 2020-06-14
### Added
- [2020-06-14] Added new `client_hostname` selector to allow for custom address binding for Netty by [@lflare].
- [2020-06-14] Added new `ui_hostname` selector to allow for custom address binding for WebUiNetty by [@lflare].
- [2020-06-14] Added response timings to trace logs and response headers by [@lflare].

## [1.0.0-RC15] - 2020-06-13
### Added
- [2020-06-13] Allow for the two log levels to be configurable by [@lflare].
- [2020-06-13] Added X-Cache header to image responses by [@lflare].
- [2020-06-13] Added .gitattributes to help sort out CHANGELOG.md merge conflicts by [@lflare].
- [2020-06-13] Added rudimentary web-ui by [@carbotaniuman & @RedMatriz].
- [2020-06-13] Added additional entry to server ping for network speed by [@lflare].
- [2020-06-13] Added colouring to web-ui pie chart by [@lflare].

### Changed
- [2020-06-13] Modified AsyncAppender queue size to 1024 by [@lflare].
- [2020-06-13] Bumped client version to 5 by [@lflare].
- [2020-06-13] Modularized the image server by [@carbotaniuman].
- [2020-06-13] Suppressed log output for IOException by [@carbotaniuman].
- [2020-06-13] Migration of Java to Kotlin for most handlers by [@carbotaniuman]

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

[Unreleased]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc19...HEAD
[1.0.0-rc19]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc18...1.0.0-rc19
[1.0.0-rc18]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc17...1.0.0-rc18
[1.0.0-rc17]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc16...1.0.0-rc17
[1.0.0-rc16]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc15...1.0.0-rc16
[1.0.0-rc15]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc14...1.0.0-rc15
[1.0.0-rc14]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc13...1.0.0-rc14
[1.0.0-rc13]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc12...1.0.0-rc13
[1.0.0-rc12]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc11...1.0.0-rc12
[1.0.0-rc11]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc10...1.0.0-rc11
