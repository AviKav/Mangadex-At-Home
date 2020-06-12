# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- [2020-06-12] Added CHANGELOG.md by [@lflare].

### Changed
- [2020-06-12] Raised ApacheClient socket limit to `2**18` by [@lflare].
- [2020-06-12] Changed gradle versioning to using `git describe` by [@lflare].

### Deprecated

### Removed

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

[Unreleased]: https://gitlab.com/mangadex/mangadex_at_home/compare/v1.0.0-rc12...HEAD
[1.0.0-rc12]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc11...1.0.0-rc12
[1.0.0-rc11]: https://gitlab.com/mangadex/mangadex_at_home/-/compare/1.0.0-rc10...1.0.0-rc11
