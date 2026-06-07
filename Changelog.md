# Changelog

All notable changes to this project will be documented in this file.

## [v0.0.3-PRERELEASE]
  - WC-0009 - Prediction audit table and admin Audit page with optional player filter
  - WC-0010 - Production deploy workflow (build on PR, deploy on main only) and DEPLOY.md
  - WC-0011 - Document first-time EC2 seed of users/authorities via db/setup.sql
  - WC-0012 - Feedback & Wishes page for shared app comments and suggestions
  - WC-0013 - Group 1st/2nd predictions page, admin group results, leaderboard includes group points
  - WC-0014 - Group teams loaded from fixtures; +1 pt for qualifying team in wrong place
  - WC-0015 - Group prediction audit trail and tournament kickoff countdown in header
  - WC-0016 - Distinct admin page styling and clearer Group Results warning
  - WC-0017 - Final prediction page (champion/runner-up), admin final result, audit, rules, leaderboard
  - WC-0018 - Per-match "Locks in" countdown on predictions list (minute granularity)
  - WC-0019 - Other players' predictions page with kickoff/tournament visibility rules
  - WC-0020 - Collapsible Support footer (Venmo QR) plus panel on Profile and Feedback
  - WC-0021 - Scrolling standings ticker in header (ESPN-style, all player pages)
  - WC-0022 - Upcoming matches (next 5) merged into header ticker with NEXT label

## [v0.0.2-PRERELEASE]
  - WC-0005 - Implemented approved scoring rules (5/2/+1, knockout multipliers) in PointsServiceImpl
  - WC-0006 - Added friend group users with display names; alper is admin
  - WC-0007 - Password change page at /profile/password ({noop} storage)
  - WC-0008 - Rules page, leaderboard shows full names, profile settings updated

## [v0.0.1-PRERELEASE]
  - WC-0001 - Initial prerelease: Spring Boot app, MySQL, Docker local setup
  - WC-0002 - Group stage fixtures import (72 matches), predictions until kickoff, user timezone
  - WC-0003 - Admin match scores, leaderboard stub, JDBC auth
  - WC-0004 - Draft Principles & Rules (EN/TR) in `document/` for group review
