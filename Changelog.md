# Changelog

All notable changes to this project will be documented in this file.

## [v0.0.11-PRERELEASE]
  - WC-0105 - End Tournament celebration: admin button triggers fireworks + winner screen for all users (canvas-confetti); `app_settings` table for flags
  - WC-0104 - Bracket snapshots: updated SF with France 0–2 Spain (`semiFinalBracket_14July2026.png`); added 3rd-place & Final (`finalBracket_14July2026.png`)
  - WC-0103 - Bird Watch: list all qualifying pool players per category (not only top 3)
  - WC-0102 - Bracket snapshot: Semi-finals (`semiFinalBracket_11July2026.png`); default tab Semifinals
  - WC-0101 - Third-place play-off: fix fixture stage import (`Play-off for third place`); points ×1.75 (same as semi-final)
  - WC-0100 - Others' Picks: Final table at top; eliminated champion/runner-up picks shown struck through
  - WC-0099 - Bracket snapshots: R16 complete (`roundOf16Bracket_7July2026.png`); Quarter-finals (`quarterFinalBracket_7July2026.png`); default tab QF
  - WC-0098 - Others' Picks: saved-at time next to each player (last audit entry; falls back to prediction updated_at)
  - WC-0097 - Missing-pick alerts (red): next match day on Knockout and Others' Picks — who has not saved; personal banner if you are missing
  - WC-0096 - Knockout points keep fractions (no rounding); e.g. R16 ×1.25 → 6.25 / 1.25; leaderboard & UI show decimals

## [v0.0.10-PRERELEASE]
  - WC-0091 - Knockout predictions: active rounds (e.g. Round of 16) listed first; finished rounds (e.g. Round of 32) at the bottom
  - WC-0092 - Round of 16: Jul 7 slots fixed — Argentina–Egypt (Atlanta), Switzerland–W88 (Vancouver)
  - WC-0093 - Knockout page: phase tabs for bracket snapshots; default Round of 16 (`roundOf16Bracket_4July2026.png`)
  - WC-0094 - Bird Watch: First-Pick Falcons (most times first to create a pick) and Night Owls (most times last to save a pick)
  - WC-0095 - Love/Hate teams: avg points when a team plays (min 3 games); Bird Watch Love Birds & Team Nemeses; profile + prediction sidebar

## [v0.0.9-PRERELEASE]
  - WC-0090 - Knockout scoring: correct advancer always +1 (even when 90′ path is wrong); draw-path advancer no longer +2

## [v0.0.8-PRERELEASE]
  - WC-0087 - Knockout scoring: +1 advancer when 90′ outcome is correct (non-draw pick); rules in Turkish (default) and English
  - WC-0088 - Knockout page: bracket image updated to 1 July 2026 snapshot
  - WC-0089 - Round of 16 bracket: fix W-placeholder pairings in fixtures (Canada–Morocco, Paraguay–France, Brazil–Norway); prod SQL `db/knockout_r16_bracket_fix.sql`; bracket display uses advancer on R32 draws

## [v0.0.7-PRERELEASE]
  - WC-0086 - Leaderboard: rank movement arrows (↑ ↓ →) vs end of previous day (user timezone)

## [v0.0.6-PRERELEASE]
  - WC-0060 - Prod SQL scripts for manual R32 setup: `db/knockout_r32_prod_fix.sql` and `db/knockout_r32_group2.sql` (standings-based team assignment, accent-safe)
  - WC-0061 - Host's Picks: show host knockout predictions (90′ score, penalties, advancer) before kickoff, same transparency as group-stage scores
  - WC-0062 - Others' Picks: knockout section with 90′ / penalties / advancer columns; hidden until kickoff; Next match includes earliest group or knockout game
  - WC-0063 - Prediction Audit: knockout picks show 90′ score, penalties, and advancer; type badge "Knockout"
  - WC-0064 - Turkuaz Market ad: left rail (desktop), footer banner (mobile); links to Yelp
  - WC-0065 - Nav: Group Predictions + Knockout — My Predictions; Others' Picks shows Group 1st/2nd before match scores
  - WC-0066 - Others' Picks: Final (champion & runner-up) moved above match scores, next to Group 1st & 2nd
  - WC-0067 - Knockout bracket image updated to 28 June 2026 snapshot (post group stage)
  - WC-0068 - Admin: Sync knockout bracket from standings (fills empty slots only; keeps existing picks)
  - WC-0069 - Admin: saving knockout scores auto-syncs next round; draw at 90′ requires advancer pick
  - WC-0070 - Prod SQL `db/knockout_r32_28june.sql`: full R32 from 28 Jun bracket (fixes Uruguay display / partial slots)
  - WC-0071 - Bird Watch: Group Sage Grouse (group 1st/2nd points) and Knockout Kestrels (knockout match points)
  - WC-0072 - Country flags (JSON map + SVG from flag-icons): knockout, group list, standings, others' picks
  - WC-0073 - Header ticker: club badges on standings, country flags on NEXT matches; knockout games in ticker
  - WC-0074 - Bird Watch: Match Magpies (most group-stage match points — standings "Match" column)
  - WC-0075 - Mobile flag fix: eager load, iOS SVG sizing, wider match column, Safari ticker mask; Thymeleaf fragment split (fixes Whitelabel error)
  - WC-0076 - Others' Picks: country flags on Final (champion & runner-up) table
  - WC-0077 - Header countdown includes knockout; per-match Locks in on Knockout predictions page
  - WC-0078 - Others' Picks: show all upcoming matches on the next match day (not just one)
  - WC-0079 - Host's Picks: Final at top; knockout and group matches newest first; flags on final picks
  - WC-0080 - Others' Picks: merge started group + knockout into one list; live/awaiting-score games pinned above older results
  - WC-0081 - Others' Picks: knockout started matches always listed above group stage
  - WC-0082 - Knockout scoring: advancer bonus +1 (not +2) when actual 90′ was not a draw
  - WC-0083 - Admin Enter Scores: penalty shootout Yes/No when knockout match is level at 90′ (fixes missing +1 pens bonus)
  - WC-0084 - Leaderboard: cumulative points race chart (by match day, user timezone)
  - WC-0085 - Leaderboard chart: include group 1st/2nd and final points (aligned with table total)

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
