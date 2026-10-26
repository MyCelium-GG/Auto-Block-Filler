# Changelog

All notable changes to AutoBlockRefiller are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.4] - 2026-10-26

### Added

- An icon in the mod list on Fabric, Forge and NeoForge.

### Changed

- Requires Mycel 1.4.1.

## [1.0.3] - 2026-10-25

### Changed

- Requires Mycel 1.4.0. Operators now get one chat line per session when a
  MyCelium mod has an update, and `/mycel mods` marks it.

## [1.0.2] - 2026-10-19

### Changed

- Updated Mycel lib to 1.3.0: Mycel's chat messages now use the MyCelium wiki's
  red style.
- Checks for new versions through the MyCelium wiki (one log line when an
  update is out; turn off with `enableUpdateChecker` in `config/mycel.json`).

## [1.0.1] - 2026-10-07

### Changed

- Updated the Mycel shared library from 1.1.0 to 1.2.1
  (`com.github.MyCelium-GG:MyCel-Lib:1.2.1` via JitPack).
  No mod code changes were needed (the update is backwards compatible).
  This pulls in live config editing (`/mycel get/set/reset`) and the shared
  hex-styled `[Mycel]` message palette for `/mycel` output.
