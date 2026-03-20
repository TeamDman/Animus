# Animus Codebase Improvements

Systematic improvement plan based on comprehensive code review.

## Guidelines

Before implementing any item, consider whether it should be exposed as a public API addition in NeoVitae (in the `api/` package) for other addon developers. If so, implement it there first with proper Javadocs, then have internal code delegate to it.

## Phase 1 — Quick Wins

- [x] **1.1** Replace all `System.out.println` with `Animus.LOGGER.debug()` (3 files, ~28 occurrences)
- [x] **1.2** Fix 3 hardcoded `"animus"` mod IDs in `@EventBusSubscriber` to use `Constants.Mod.MODID`
- [x] **1.3** Remove duplicate `getRadiusStatic()` in EquivalencySigilEffect, updated caller
- [x] **1.4** Extract `DemonWillTypeHelper` for shared will type management across 4 weapon items

## Phase 2 — Medium Effort

- [x] **2.1** Extract `AnimusRitualHelper.getOwnerNetwork()` — updated 15 rituals
- [x] **2.2** Extract `AnimusRitualHelper.getItemHandler()` — updated 6 rituals
- [ ] **2.3** DRY up `InventorySearchHelper` triple-loop into `forEachInventoryItem()`
- [ ] **2.4** Extract `RitualZoneTracker<T>` for RitualEndlessGreed and RitualSerenity
- [ ] **2.5** Extract shared particle/sound helpers for RitualSiphon and RitualRelentlessTides
- [ ] **2.6** Extract `LPHelper.consumeLP()` for ItemSpearBound and ItemHellforgedBow

## Phase 3 — Larger Refactors

- [ ] **3.1** Extract `CenterOutwardSearcher` generic utility for RitualSol, RitualLuna, RitualSiphon, RitualRelentlessTides
- [ ] **3.2** Consolidate `WillWeaponStats` config class for ItemSentientBow, ItemSpearSentient, ItemHellforgedBow
- [ ] **3.3** Break up `RitualCulling` (425 lines) — extract entity filtering and fake player logic

## Completed

_(items moved here after completion)_
