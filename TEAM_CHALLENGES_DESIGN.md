# Team Challenges — Design

A new *behavioural class* of challenge that is only available to islands with a
team, and that can require a proportion of the team to be online to complete.
Team behaviour is implemented as optional modifiers on the existing `Challenge`
object — **not** a new challenge type or a new `Requirements` subclass — so all
existing challenges, GUIs, importers and handlers keep working unchanged.

## Decisions (locked)

| Question | Decision |
| --- | --- |
| Aggregation scope (whose items/stats count) | **Online members only** |
| Presence metric | **Percentage of team** (fraction `0.0`–`1.0`), rounded with `ceil`, floor of 1 |
| Data model | **Extend existing `Challenge`** with optional fields, default off |
| Per-member ("everyone pays") primitive | **Yes** — Roll Call Feast requires each online member to contribute individually |
| Pooled removal split | **Option A — equal absolute contribution ("water level")** |
| Visibility to non-teams | **Per-challenge flag, default show** (greyed/disabled); admin can set hidden |
| Roll Call Feast per-member amount | **`requirement / requiredPresentCount`** (the configured amount is the *team total*) |

## The five reference challenges

These are chosen to exercise three reusable primitives so the feature is
general rather than one-off.

| # | Name | `teamChallenge` | `teamPresence` | Aggregation | Existing requirement reused | Primitive exercised |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | **All Hands on Deck** | ✓ | `1.0` | — | none (presence only) | Presence gate |
| 2 | **Pooled Tribute** | ✓ | `0.5` | aggregate | Inventory | Aggregate-across-online-members |
| 3 | **Synchronized Build** | ✓ | `0.5` | — | Island | Composability (presence + existing requirement) |
| 4 | **Combined Effort** | ✓ | `0.0` | aggregate | Statistic | Aggregate statistic across members |
| 5 | **Roll Call Feast** | ✓ | `1.0` | per-member | Inventory | Per-member ("everyone pays") |

Three primitives total: **presence gate**, **aggregate across online members**
(inventory + statistic), **per-member contribution**.

## Data model — extend `Challenge`

New optional `@Expose` fields, all defaulting to "off" so existing challenges
deserialize unchanged. Placed near the existing condition/environment block and
cloned in `copy()`.

```java
@Expose private boolean teamChallenge = false;  // gate: only teams see / attempt it
@Expose private double  teamPresence  = 0.0;     // 0.0–1.0 fraction of team online required
@Expose private boolean aggregateTeam = false;   // sum inventory/stats across online members
@Expose private boolean perMember     = false;   // each online member must individually satisfy (Roll Call Feast)
@Expose private boolean hideIfNoTeam  = false;   // default false = show greyed/disabled to soloists; true = hide entirely
```

`teamPresence` is a **fraction**, not a whole percent, to avoid a units bug and
to serialize cleanly.

Required-present count:

```java
int required = Math.max(1, (int) Math.ceil(teamSize * teamPresence));
```

A positive presence never rounds down to 0. The requirement that a team exists
at all (at least two members) is enforced separately by the team-size check, not
by this presence gate - so 50% of a 2-member team is 1, not 2. A presence of
`0.0` (Combined Effort) skips the gate entirely and only aggregates.

## Primitives in `TryToComplete`

The pipeline already iterates team members for the Statistic requirement
(`island.getMemberSet()` + `Bukkit.getPlayer()` null-check for online status),
so "aggregate across online members" generalises existing code.

### 1. Presence gate

A new `checkTeamPresence()` slotted into `checkIfCanCompleteChallenge`,
alongside the environment check. Online members:

```java
Island island = addon.getIslands().getIsland(world, user);
List<Player> online = island == null ? List.of() :
    island.getMemberSet().stream()
          .map(Bukkit::getPlayer)
          .filter(Objects::nonNull)
          .toList();
int required = Math.max(1, (int) Math.ceil(island.getMemberSet().size() * challenge.getTeamPresence()));
// fail if online.size() < required
```

### 2. Aggregate across online members

Generalise the existing statistic loop into a helper that walks the online
members, sums each member's contribution (matched inventory quantity or
statistic value), and validates the total. Reused by both `checkInventory()`
and `checkStatistic()` when `aggregateTeam` is true.

A single member is allowed to hold the entire required amount. We deliberately
do **not** require the amount to be spread across members — forcing a spread
would push players into "stupid" actions (dividing a stack between members just
to pass).

### 3. Per-member contribution (Roll Call Feast)

The configured requirement is the **team total**. Each present member must
individually hand in `share = ceil(requirement / requiredPresentCount)`. Check
every online member; if any one falls short, the challenge fails. On completion,
remove `share` from each member. This is the one mechanic that cannot be
expressed as a normal challenge.

**Why divide by present count (not a fixed per-head amount):** a fixed per-head
bill lets members log out to dodge their share while the remaining minimum still
completes the challenge — the team's *total* cost shrinks as people leave, so
dodging is rewarded. Dividing the fixed total by the present count makes the
team's aggregate cost invariant to turnout: fewer present → each present member
pays *more*, removing any incentive to thin the crowd. This must be made clear
to the admin in the editor: **"the amount you enter is split across the present
team, not charged per person."**

## Pooled removal — Option A (equal absolute contribution / "water level")

When an aggregate inventory challenge **consumes** items (cost, not just a
possession check), the required quantity `R` is removed across online members
as equitably as possible: everyone pays the same absolute amount, capped at
what they hold; anyone short gives all they have and the rest cover the
difference.

### Algorithm

```
R         = required quantity to remove
holdings  = matched quantity each online member has
# Find the integer "water level" L: the largest L with sum_i min(holdings_i, L) <= R
base      = sum_i min(holdings_i, L)
remainder = R - base                     # 0 <= remainder < (members above L)
# Each member pays min(holdings_i, L), plus 1 extra for each of the `remainder` LARGEST holders above L
take_i    = min(holdings_i, L) + (member i among the `remainder` largest holders above L ? 1 : 0)
```

The integer rounding remainder is absorbed by the **largest holders first** —
deterministic, order-independent, and mildly progressive (the richest pay the
odd unit, since they are least hurt by it).

### Worked example — 5 members, `R = 100`, holdings `10, 15, 20, 40, 100`

- Water level `L = 27`: `min(10,27)+min(15,27)+min(20,27)+min(40,27)+min(100,27) = 10+15+20+27+27 = 99`
- `remainder = 100 − 99 = 1`, given to the largest holder above `L` (the 100-holder)
- **Removed: `10, 15, 20, 27, 28`** → the three small holders are wiped; the leftover `55` splits as evenly as possible (`27/28`) between the 40- and 100-holder, the richest absorbing the odd unit.

Properties: order-independent, integer-clean, one-line explainable —
*"Everyone chips in equally; if you don't have enough you give all you have and
the rest cover the difference."*

`take items: true/false` (consume vs. possession-check only) is the existing
cost/requirement distinction and is independent of this split.

## Visibility / GUI

- Visibility to soloists is a **per-challenge flag** (`hideIfNoTeam`), default
  `false` → the challenge is shown greyed/disabled to players without a team
  (a recruitment nudge). An admin can set it `true` to hide the challenge
  entirely until the player has a team. This replaces the earlier idea of a
  single global `Settings` toggle.
- Filter in `ChallengesPanel.updateFreeChallengeList()` /
  `updateChallengeList()`: `removeIf(c -> c.isTeamChallenge() && c.isHideIfNoTeam()
  && !playerHasTeam(...))`; non-hidden team challenges are rendered disabled.
- `generateChallengeDescription()` gains a line such as `Team: 2/3 online` so
  players understand the gate.

## Admin editor + resources

- Admin GUI (`panel/admin/`) toggles for the four new fields.
- `default.json` / `template.yml`: document the new keys, ship the five
  reference challenges as examples.
- `locales/en-US.yml`: the new description line and an `insufficient-team`
  error string.

## Edge cases

- **Member logs off mid-completion.** Re-check presence inside
  `fullFillRequirements`, not only at validation — otherwise a team could pass
  the gate, then have a member leave before costs are taken.
- **`teamPresence = 0.0`.** Skip the presence gate; aggregate only
  (Combined Effort).
- **Team smaller than the rounded requirement.** `ceil` + floor-of-1 (plus the
  separate team-exists check) means a 2-person team always has an achievable
  target for `teamPresence <= 1.0` (50% -> 1 present).

## Exploit analysis (game theory)

Three facts about the current code create most of the exploit surface, so they
are addressed once as **cross-cutting mitigations** and then referenced per
challenge.

### Cross-cutting findings & mitigations

**X1 — Rewards go only to the clicking player** (`TryToComplete.java:256-274`,
all rewards to `this.user`). In a team challenge, many members pay or contribute
but a single member claims → a free-rider/leech problem in *every* team
challenge. The clicker can have teammates fund a cost they personally reap.
**Mitigation (master lever):** team-challenge rewards go to **every qualifying
present member** (each receives the reward), or to a team bank — never to the
clicker alone. This single change neutralises the leech angle across all five.

**X2 — Completion & cooldown are per-player by default** (`ChallengesManager.java:1010`),
though an existing `isStoreAsIslandData()` mode keys them by **island UUID**
(`:1005`). If left per-player, all N members can each "complete" the same team
effort in turn → N× rewards and N× cooldowns for one team action.
**Mitigation:** team challenges **force island-keyed storage** regardless of the
global setting, so one team effort = one completion + one shared cooldown.

**X3 — Statistics are absolute lifetime values, no delta** (`TryToComplete.java:1494`;
`getStatistic()` at `ChallengesManager.java:1339`). Aggregating absolute
lifetime stats across online members lets a team **borrow a veteran**: invite a
player with 1,000,000 lifetime mob kills, have them online at completion to
satisfy "team total 1,000 kills," then they leave. Nothing was earned together.
**Mitigation:** for team statistic challenges, snapshot each member's stat as a
**baseline when the team unlocks the challenge** and require the *increase*
(`current − baseline`) to reach the target; only members present at unlock count
toward the baseline, so a late-joining veteran's lifetime total is excluded.
(Stored alongside the island-keyed completion record from X2.)

### Per-challenge

**1. All Hands on Deck (presence only, no cost).** Pure reward for turnout.
- *Exploit:* zero-cost reward printing — relog the crew (or a set of idle alts)
  and claim; if repeatable, an infinite faucet.
- *Mitigations:* non-repeatable or long, **island-keyed** cooldown (X2); keep
  the reward modest (it is a social/tutorial milestone); optionally require
  members to be **within island protection bounds**, not merely connected, so
  parked alts in another world do not count. X1 (reward all present) removes the
  "one clicker profits" angle but raises the alt incentive — hence keep the
  reward small here specifically.

**2. Pooled Tribute (aggregate inventory, optional consume).**
- *Exploit A — borrowing (possession-only):* if `take items = false`, the team
  only needs to *hold* N collectively for one instant; borrow from a non-member
  friend, complete, return. Inherent to any possession check.
  *Mitigation:* prefer **consume** for valuable rewards; treat possession-only
  as a soft "show your wealth" flex with cosmetic rewards.
- *Exploit B — leech:* contributors lose items (water-level removal), clicker
  reaps reward. *Mitigation:* X1.
- *Exploit C — dodge by logging out:* the total N is fixed, so logging members
  out does **not** reduce the team's bill — it shifts a larger share onto those
  who stay (equitable removal caps how badly, but stayers still pay more). This
  is "unfair to teammates," not a system cheat; a griefer could stay offline so
  loyalists pay. *Mitigation:* require a higher `teamPresence` for consuming
  Pooled Tribute challenges so a quorum must share the load.
- *Note:* a single member holding all N is **allowed by design** (avoids forcing
  silly stack-splitting) and is not treated as an exploit.

**3. Synchronized Build (presence + island requirement).**
- *Exploit:* the island state can be built by one player over time; presence
  only requires bodies online at the click, so "the team" may have done nothing
  collectively beyond showing up (possibly with alts).
- *Mitigations:* non-repeatable + island-keyed (X2); to make "synchronized"
  meaningful, require present members to be **inside island protection range**
  at completion, not merely online. Reward sizing kept in line with a one-time
  build milestone.

**4. Combined Effort (aggregate statistic).** The veteran-borrow case — see
**X3**; the baseline-delta fix is essential for this challenge, not optional.
Without it this challenge is trivially defeated by a single recruited veteran.

**5. Roll Call Feast (per-member contribution).**
- *Exploit — log out to dodge the bill:* defeated by `requirement /
  requiredPresentCount` (the team total is invariant to turnout — see the
  primitive section). At `teamPresence = 1.0` there is no dodging at all
  (everyone must be present).
- *Exploit — alt padding to lower each share:* bringing alts online lowers every
  member's share, **but each alt must also pay its own share** or the per-member
  check fails, so empty alts cannot help; funding alts is a net loss.
- *Residual:* a single poor member who cannot cover their (turnout-reduced)
  share blocks the whole team — but this *encourages* turnout (more present →
  smaller shares), which is the desired incentive. Combined with X1 (only
  present payers are rewarded), the incentive is fully aligned: show up, pay your
  share, get rewarded; dodge, pay nothing, get nothing.

### Summary of required vs. optional mitigations

| Mitigation | Status |
| --- | --- |
| X1 reward all present members | **Required** (defeats leeching everywhere) |
| X2 island-keyed completion/cooldown for team challenges | **Required** (defeats N× claim) |
| X3 statistic baseline-delta | **Required for Combined Effort**; recommended for any team statistic |
| Higher presence for consuming challenges | Recommended |
| "Within island bounds," not just online | Recommended for presence-flex challenges (1, 3) |
| Modest reward + non-repeatable for no-cost challenges | Recommended (challenge 1) |

## Touchpoints (file references)

| Component | File | Notes |
| --- | --- | --- |
| Data model | `database/object/Challenge.java` | new fields + `copy()` |
| Validation / presence gate | `tasks/TryToComplete.java` `checkIfCanCompleteChallenge` | new `checkTeamPresence()` |
| Aggregate inventory/stat | `tasks/TryToComplete.java` `checkInventory()` / `checkStatistic()` | generalise existing member loop |
| Per-member + pooled removal | `tasks/TryToComplete.java` `fullFillRequirements` | per-member check; water-level removal |
| Reward all present (X1) | `tasks/TryToComplete.java` reward block `:256-274` | loop rewards over present members instead of `this.user` |
| Island-keyed storage (X2) | `managers/ChallengesManager.java` `getDataUniqueID` `:1005-1010` | force island key when `teamChallenge` |
| Statistic baseline-delta (X3) | `ChallengesPlayerData` + `checkStatistic()` `:1494` | store per-team baseline at unlock; compare `current − baseline` |
| Team / online detection | `island.getMemberSet()` + `Bukkit.getPlayer()` | pattern already in codebase |
| Visibility | `panel/user/ChallengesPanel.java` | filter + description line |
| Admin editing | `panel/admin/` | toggles |
| Resources | `default.json`, `template.yml`, `locales/en-US.yml` | examples + strings |

No new `Requirements` subclass and no new Gson adapter are required — team
behaviour is a cross-cutting modifier on the existing requirement types.

## Build plan (phased)

Foundation first. X1/X2/X3 are prerequisites, not polish: without them the five
reference challenges are individually exploitable regardless of how the
team-gate mechanics are built. Each phase is independently shippable, leaves the
build green, and is verifiable in isolation.

### Phase 0 — Data model & scaffolding

*Goal:* new fields exist and round-trip; nothing behaves differently yet.

- Add the five `@Expose` fields to `Challenge.java` (`teamChallenge`,
  `teamPresence`, `aggregateTeam`, `perMember`, `hideIfNoTeam`), all default-off.
- Update `Challenge.copy()` to clone them.
- A `Settings`/`Constants` home for the `insufficient-team` locale key (string
  added later in Phase 5).
- *Tests:* serialize → deserialize a `Challenge` with the fields set and assert
  equality; assert a legacy `default.json` challenge (no team fields) still
  loads with all-off defaults. *Acceptance:* `mvn test` green; no GUI/behaviour
  change.

### Phase 1 — X2 island-keyed completion (foundation)

*Goal:* a team challenge completes and cools down **once per island**, not once
per player.

- In `ChallengesManager.getDataUniqueID` (`:1005-1010`), force the island key
  when the challenge `isTeamChallenge()`, regardless of the global
  `isStoreAsIslandData()` setting.
- *Tests:* two members of one island; member A completes a `teamChallenge`;
  assert member B sees it already completed and the cooldown is shared. Assert a
  non-team challenge is unaffected by this change. *Acceptance:* one team effort
  = one completion record.

### Phase 2 — X1 reward all present members (foundation)

*Goal:* rewards are distributed to every qualifying present member, not the
clicker alone.

- Refactor the reward block (`TryToComplete.java:256-274`) so that for a
  `teamChallenge` it iterates the present members and gives each the item/money/
  exp/command reward; non-team challenges keep the single-`user` path unchanged.
- Decide command-reward semantics (run per member with their name substituted).
- *Tests:* mock a 3-present team, complete a team challenge, assert each present
  member received the reward and offline members received nothing; assert a
  normal challenge still rewards only the completer. *Acceptance:* no leech path.

### Phase 3 — X3 statistic baseline-delta (foundation)

*Goal:* team statistic challenges measure progress earned while on the team, not
borrowed lifetime totals.

- Add per-team baseline storage (in the island-keyed `ChallengesPlayerData`,
  alongside completion): snapshot each member's statistic when the challenge
  unlocks for the team.
- In `checkStatistic()` (`:1494`), for a team challenge compare
  `current − baseline` against the requirement; exclude members with no baseline
  (i.e. joined after unlock).
- *Tests:* veteran with high lifetime stat joins after unlock → contributes 0;
  members present at unlock contribute only their increase. *Acceptance:*
  veteran-borrow defeated.

### Phase 4 — Presence gate primitive

*Goal:* `checkTeamPresence()` gates completion by `ceil(teamSize · teamPresence)`
(floor 2), re-checked at fulfilment.

- New `checkTeamPresence()` in `checkIfCanCompleteChallenge` (near the
  environment check); re-check inside `fullFillRequirements` to close the
  gate-then-leave window.
- This phase alone makes **All Hands on Deck** and **Synchronized Build**
  functional (the latter just composes with the existing Island requirement).
- *Tests:* present-count below/at/above threshold; rounding at odd team sizes;
  member logs off between validation and fulfilment → fails cleanly.

### Phase 5 — Aggregate & per-member primitives

*Goal:* the inventory/statistic aggregation and the per-member cost.

- Generalise the existing member-iteration loop into an
  "aggregate across present members" helper; wire into `checkInventory()` and
  `checkStatistic()` when `aggregateTeam` is set. Enables **Pooled Tribute** and
  **Combined Effort** (with Phase 3).
- Per-member check + `requirement / requiredPresentCount` share; enables
  **Roll Call Feast**.
- Water-level removal (Option A) for consuming Pooled Tribute challenges — a
  small standalone, unit-testable method.
- Add the `insufficient-team` and `Team: x/y online` locale strings
  (`en-US.yml`).
- *Tests:* the worked water-level example (`10,15,20,40,100` → `10,15,20,27,28`)
  as a direct unit test; aggregate pass with a single holder; per-member failure
  when one member is short; possession-only vs. consume paths.

### Phase 6 — Visibility & description

*Goal:* soloists see team challenges per the `hideIfNoTeam` flag; tooltips
explain the gate.

- Filter/disable in `ChallengesPanel.updateFreeChallengeList()` /
  `updateChallengeList()` per `hideIfNoTeam`.
- `generateChallengeDescription()` gains the `Team: x/y online` line and a
  disabled-state hint.
- *Tests:* solо player with `hideIfNoTeam=false` sees it disabled; with `true`
  does not see it; team player sees it enabled with the presence line.

### Phase 7 — Admin editor & reference content

*Goal:* admins can author team challenges; the five examples ship.

- Toggles/inputs in `panel/admin/` for the five fields, with the Roll Call Feast
  "amount is the team total, split across present members" note surfaced.
- Add the five reference challenges to `default.json` / `template.yml`.
- *Tests:* admin-panel tests (JUnit 5 + Mockito per `PanelTestHelper`) for the
  new toggles; import round-trip of the five examples.

### Sequencing notes

- Phases 0–3 are the **foundation** and should land before any reference
  challenge is exposed to players, or early adopters will hit the exploits.
- Phases 4–5 deliver the mechanics; 6–7 deliver UX and content.
- Each phase keeps `mvn verify` green and is independently reviewable.
