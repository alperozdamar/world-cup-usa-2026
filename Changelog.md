# Changelog

All notable changes to this project will be documented in this file.

## [v0.0.5-PRERELEASE]
  - WC-0039 - Changelog page (/changelog) linked from footer version; Maven build-info for accurate version display
  - WC-0040 - Success rate % and misses on leaderboard stats and My Predictions profile card
  - WC-0041 - Group Stage Predictions: upcoming/open matches first, locked/started matches at the bottom
  - WC-0042 - Group Standings page (/predictions/standings): live tables from admin-entered scores (MP/W/D/L/GF/GA/GD/Pts), top two highlighted; linked from Group Stage, Knockout, and My Predictions
  - WC-0043 - Knockout predictions page (/predictions/knockout): bracket overview with 90′ score picks; draw picks include penalty shootout and team to advance; local dev bootstrap for UI testing
  - WC-0044 - Knockout bracket overview: Round of 32 team names from current group standings; fixture slot codes (1A, 2F, W73, etc.) shown beside each team; placeholders restored from fixtures on startup
  - WC-0045 - Rules page: knockout stage section (90′ score, +1 penalty shootout, +2 correct advancer when level at 90′, round multipliers)
  - WC-0046 - Group Stage Predictions mobile layout: Group column removed; Status rightmost; smaller Locks in countdown; compact score inputs
  - WC-0048 - Others' Picks: per-group points shown in Group 1st & 2nd table after admin enters official group results
  - WC-0049 - Leaderboard: Match / Group / Final point columns so total breakdown is visible
  - WC-0050 - Knockout display: show admin-assigned teams (e.g. USA vs Bosnia) instead of provisional 3rd-place guess
  - WC-0051 - Knockout scoring: +2 for correct advancer when you predicted a draw at 90′, even if the 90′ score was wrong (e.g. 1–1 + Canada advances vs actual 1–2)
  - WC-0052 - Rules page: knockout scoring examples table (R32 picks vs results with point breakdown)
  - WC-0053 - Profile photo upload on Profile Settings (JPEG/PNG up to 2 MB, stored in DB; falls back to bundled default photo)
  - WC-0054 - Profile photo upload: show friendly error on oversize file instead of HTTP 413 page
  - WC-0055 - Profile photo storage: use MEDIUMBLOB for photo_data (fixes upload failure above 64 KB)
  - WC-0056 - Leaderboard tiebreakers: success rate % then correct champion pick when total points are equal
  - WC-0057 - Knockout page: static bracket image (26 Jun 2026 snapshot) replaces dynamic bracket overview; prediction forms unchanged
  - WC-0058 - Admin Enter Scores: today's matches first; unscored past games stay near the top; saved past games at the bottom
  - WC-0059 - Leaderboard: Knockout column separate from group-stage Match column (stage multipliers visible per round)
  - WC-0060 - Prod SQL scripts for manual R32 setup: `db/knockout_r32_prod_fix.sql` and `db/knockout_r32_group2.sql` (standings-based team assignment, accent-safe)
  - WC-0061 - Host's Picks: show host knockout predictions (90′ score, penalties, advancer) before kickoff, same transparency as group-stage scores
  - WC-0062 - Others' Picks: knockout section with 90′ / penalties / advancer columns; hidden until kickoff; Next match includes earliest group or knockout game

## [v0.0.4-PRERELEASE]
  - WC-0037 - Mobile-friendly header: hamburger nav on small screens, compact clocks/ticker, no wasted blue space on iPhone
  - WC-0038 - Remember-me startup fix: persistent_logins table managed via SQL; no crash when table already exists

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
  - WC-0023 - Daily email reminders for missing group/final picks; email on profile
  - WC-0024 - Host's Picks page for fairness and transparency: non-admin users can view pool host predictions before kickoff/tournament start while others' picks stay hidden until normal reveal rules
  - WC-0025 - GitHub Actions deploy passes optional mail secrets so SMTP config survives redeploys
  - WC-0026 - Player profile photos on My Predictions sidebar and Leaderboard
  - WC-0027 - Favorite club badges (Beşiktaş, Fenerbahçe, Galatasaray) on profile card and Leaderboard
  - WC-0028 - The Bird Watch fun stats page (8 categories, top 3 each, Crystal Ball hidden until champion known)
  - WC-0029 - Second friend pool (Group 2) on separate EC2: bootstrap profile, player photos in `images/group2/`, club badges, display names; pool membership filters ticker, leaderboard, and related views
  - WC-0030 - Hourly match reminders in the final 3 hours before kickoff (up to 3 emails per missing pick); Group 2 mail setup documented
  - WC-0031 - Leaderboard lists all pool members at 0 pts until scored; stray test users excluded via pool membership
  - WC-0032 - Others' Picks: newest started matches first; next upcoming match shows who picked with scores hidden until kickoff
  - WC-0033 - Header countdown tracks next upcoming match (days/hours/minutes) instead of tournament start
  - WC-0034 - My Predictions sidebar shows exact score and correct W/D/L counts for scored matches
  - WC-0035 - Leaderboard player rows show exact scores, correct W/D/L, and missed match counts
  - WC-0036 - Login "Keep me signed in for 30 days" with persistent remember-me across redeploys

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
