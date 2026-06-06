# World Cup 2026 — Principles & Rules (Draft)

**Status:** For review by the group before implementation in the app.  
**Version:** Draft 1.0 · June 2026

---

## Core principles

1. **Fun over finance** — This is a private pool among friends. No money, no buy-ins, no prizes beyond bragging rights.

2. **Fair and simple** — Rules must be explainable in one minute. If a rule needs a spreadsheet to score, we drop it.

3. **Skill + luck** — Most points come from **match-by-match** predictions so everyone stays in the race until the final. Big one-off bets (e.g. tournament winner) are capped so they cannot dominate the table.

4. **Lock at kickoff** — A prediction is final when the match starts. Kickoff is stored in **UTC**; each player sees local time in their profile. No edits after kickoff.

5. **Admin enters truth** — Only admins record official results. Points are calculated from admin scores, not from live APIs (keeps the pool simple and trusted).

6. **Phased rollout** — Group stage first (known teams and times). Knockout and bracket extras unlock when teams are known.

7. **Transparency** — Leaderboard, rules, and points per match are visible to all players. Disputes are settled by the group, not the app.

8. **No negative points** — You never lose points for a wrong guess. Zero is the floor.

---

## Proposed scoring — match predictions (group + knockout)

Same rules for every match unless noted.

| Result | Points | Notes |
|--------|--------|--------|
| **Exact score** (e.g. predicted 2–1, actual 2–1) | **5** | Highest reward |
| **Correct outcome** (win / draw / loss) but wrong score | **2** | e.g. predicted 2–1, actual 3–1 |
| **Correct goal difference** (not exact score) | **+1 bonus** | e.g. predicted 3–1 (+2), actual 2–0 (+2). Does not stack with exact score |
| **Wrong outcome** | **0** | |

**Maximum per match (recommended cap):** 6 points (5 exact, or 2 + 1 goal-diff bonus).

### Knockout round multiplier (optional)

Multiply match points by round so late games still matter:

| Round | Multiplier |
|-------|------------|
| Round of 32 | ×1.0 |
| Round of 16 | ×1.25 |
| Quarter-final | ×1.5 |
| Semi-final | ×1.75 |
| Third place | ×1.5 |
| Final | ×2.0 |

Round to nearest whole point after multiplying.

---

## Scoring — group 1st & 2nd predictions (implemented)

Locked **before that group’s first match**. Enter picks at `/predictions/groups`.

| Pick | Points | Lock |
|------|--------|------|
| **Correct 1st place** | **3** per group | Before group’s first match |
| **Correct 2nd place** | **2** per group | Same |
| **Qualifying team, wrong place** | **1** per slot | e.g. picked 1st, they finished 2nd |
| **Wrong pick** | **0** | |

Maximum **5 points per group** if both 1st and 2nd are correct. Admin enters official group results at `/admin/group-results` after the group stage ends.

---

## Scoring — final prediction (champion & runner-up) (implemented)

Locked **before tournament kickoff** (first group-stage match). Enter picks at `/predictions/final`.

| Pick | Points | Lock |
|------|--------|------|
| **Correct champion** | **10** | Before tournament kickoff |
| **Correct runner-up** | **5** | Same |
| **Finalist, wrong place** | **3** per slot | e.g. picked champion, they finished runner-up |
| **Wrong pick** (not in final) | **0** | |

Maximum **15 points** if both champion and runner-up are correct. Example: final is Turkey (champion) vs Spain (runner-up); you pick Spain champion and Turkey runner-up → 3 + 3 = 6 points. Admin enters the official final at `/admin/final-result` after the final is played.

---

## Proposed scoring — other extras (not yet implemented)

| Pick | Points | Lock |
|------|--------|------|
| **Knockout match winner** (not score) | 3 | Kickoff |
| **Reach semi-final** (pre–Round of 32) | 5 | Before Round of 32 |
| **Reach final** | 8 | Before Round of 32 |

We **do not** recommend scoring “best third-place” qualifiers unless the whole group wants high complexity.

---

## Tiebreakers (if total points are equal)

1. Most **exact scores**  
2. Most **correct outcomes** (win/draw/loss)  
3. Closest prediction on the **final** (exact score, then goal difference, then one side’s goals)  
4. Coin flip or friendly vote

---

## Player responsibilities

- Enter group 1st/2nd picks **before each group's first match**
- Enter final champion & runner-up **before tournament kickoff**
- Enter and update match predictions **before kickoff**  
- Set your **timezone** in profile so kickoff times are clear  
- Do not share accounts  
- Accept admin-entered results as final for scoring

## Admin responsibilities

- Enter actual match scores promptly after matches
- Enter group 1st/2nd results after each group finishes
- Enter the final champion and runner-up after the final is played  
- Do not change a result once agreed unless FIFA correction  
- Enable knockout / bracket picks only when teams are known

---

## Out of scope (for now)

- Money or prizes  
- Live odds or betting integration  
- Predicting scorers, cards, or VAR events  
- Automated FIFA data feeds  

---

## Open questions for the group

Please comment before we implement in code:

- [x] Include group winner/runner-up picks yes/no? **Yes — implemented**
- [x] Final champion & runner-up pick (10 / 5 / 3 pts)? **Yes — implemented**
- [ ] Use knockout multiplier yes/no?  
- [ ] Daily “round leader” +1 bonus yes/no?  
- [ ] Global lock for all pre-tournament picks: opening match or per group?  

---

*Once approved, these rules will be implemented in `PointsServiceImpl` and shown on a “Rules” page in the app.*
