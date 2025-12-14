# Animus 1.21.1 Porting Status

**Port Target**: Forge 1.20.1 → NeoForge 1.21.1
**Current State**: BUILD SUCCESSFUL (0 compilation errors, 3 deprecation warnings)
**Last Updated**: 2024-12-13

## Quick Reference

### Key Directories
- **Animus Source**: `C:\Users\Saereth\Documents\code\animus\Animus_1.20.1`
- **Blood Magic 1.21.1**: `C:\Users\Saereth\Documents\code\BloodMagic` (branch: 1.21.1)
- **Blood Magic 1.20.1**: Same repo (branch: 1.20.1)
- **Disabled Compat**: `compat_disabled/` folder
- **Patchouli Book**: `src/main/resources/assets/animus/patchouli_books/codex_animus/`

### Build Commands
```bash
./gradlew compileJava  # Check compilation
./gradlew build        # Full build
./gradlew runData      # Run datagen
./gradlew runClient    # Test in-game
```

---

## Priority Tasks

### Phase 1: Core Compilation Fixes (COMPLETE)

**All compilation errors fixed!** Key fixes made:

1. **RitualCulling.java** - `EntityTypeTags.WITHER` doesn't exist in 1.21, replaced with direct `EntityType.WITHER` and `EntityType.ENDER_DRAGON` checks
2. **RitualEndlessGreed.java** - Blood Magic renamed `ExperienceTomeItem.addExperience()` to `addXpToTome()`
3. **AnimusFakePlayer.java** - Enchantments now use `Holder<Enchantment>` in 1.21, lookup via `level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)`
4. **AnimusConfiguredFeatures.java** - `ResourceLocation(String, String)` constructor is private in 1.21, use `ResourceLocation.fromNamespaceAndPath()` instead
5. **BloodCoreDecorator.java** - `TreeDecoratorType` now requires `MapCodec` instead of `Codec`, changed `Codec.unit()` to `MapCodec.unit()`

**Previous fixes (100 errors → 5 errors):**
- Blood Magic API package restructuring
- NeoForge API changes (`saveAdditional`/`loadAdditional`, `putBulkData`, etc.)
- Imperfect ritual registration via `DeferredRegister`
- `Holder<MobEffect>` usage
- JEI API updates

**Remaining deprecation warnings (non-blocking):**
- `IRecipeCategory.getBackground()` in JEI
- `FluidType.initializeClient()` in NeoForge

---

### Phase 2: Documentation (COMPLETE - Patchouli Works!)

**Patchouli is available for NeoForge 1.21.1** (version 1.21-88-NEOFORGE), so migration to Modonomicon is **optional**.

Current status:
- Patchouli dependency: Configured and working
- Documentation: Updated from 1.20.1 branch (87 entries, 8 categories)
- Build: Successful with Patchouli

#### Optional Future Migration to Modonomicon
If desired later, would require:
1. Add Modonomicon dependency to build.gradle
2. Convert book JSON structure (manual - no automated converter)
3. Remove Patchouli dependency
4. Update any Java code referencing Patchouli API

---

### Phase 3: Compat Module Updates (IN PROGRESS)

**Dependencies added to build.gradle:**
```groovy
// Ars Nouveau
compileOnly "curse.maven:ars-nouveau-401955:7100043"
runtimeOnly "curse.maven:ars-nouveau-401955:7100043"

// Iron's Spells n Spellbooks
compileOnly "curse.maven:irons-spells-n-spellbooks-855414:7271541"
runtimeOnly "curse.maven:irons-spells-n-spellbooks-855414:7271541"

// Malum
compileOnly "curse.maven:malum-484064:7307339"
runtimeOnly "curse.maven:malum-484064:7307339"
```

| Module | Status | Notes |
|--------|--------|-------|
| **Botania** | DISABLED | Not ported to 1.21.1 yet |
| **Ars Nouveau** | PARTIAL | SourceJarHelper working (reflection-based), full compat needs porting |
| **Iron's Spells** | DISABLED | ~100 errors, needs full API port |
| **Malum** | DISABLED | Needs porting |

**Working compat code:**
- `src/main/java/com/teamdman/animus/compat/arsnouveau/SourceJarHelper.java` - Uses reflection, no hard dependency
- `RitualSourceVitaeum.java` - Works with SourceJarHelper

**Disabled compat files** (38 files in `src/disabled/`):
- `ArsNouveauCompat.java` + 4 arsnouveau/*.java files
- `BotaniaCompat.java` + 4 botania/*.java files
- `IronsSpellsCompat.java` + 10 ironsspells/*.java files
- `MalumCompat.java` + 1 malum/*.java file
- 8 datagen/*.java files
- Various item files

**To port compat modules, fix these API changes:**

#### NeoForge API Changes
| Old | New |
|-----|-----|
| `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| `ForgeRegistries.ITEMS` | `Registries.ITEM` or `BuiltInRegistries.ITEM` |
| `RegistryObject<T>` | `DeferredHolder<T, T>` |
| `TickEvent.PlayerTickEvent` | `PlayerTickEvent` (different package) |
| `DistExecutor` | Removed - use other patterns |

#### Blood Magic 1.21.1 API Changes
| Old | New |
|-----|-----|
| `wayoftime.bloodmagic.core.living.*` | `wayoftime.bloodmagic.common.living.*` |
| `wayoftime.bloodmagic.core.living.LivingStats` | `wayoftime.bloodmagic.common.datacomponent.LivingStats` |
| `TileAltar` | `BloodAltarTile` (in `common.blockentity`) |
| `BlockAltar` | `BloodAltarBlock` (in `common.block`) |
| `ItemBloodOrb` | `BloodOrbItem` (in `common.item`) |
| `LivingUtil` | Appears removed |
| `LivingArmourRegistrar` | Registration method changed |

---

## Next Steps

1. **Port compat modules one at a time** (optional)
   - Identify new patterns for ritual registration
   - Document API changes

2. **Fix compilation errors systematically**
   - Start with import fixes
   - Then constructor/method signature changes
   - Finally implement missing methods

3. **Convert documentation after core works**

4. **Re-enable compat modules one by one** (except Botania)

---

## Notes

- Blood Magic jar is at `libs/bloodmagic-3.4.0-1-beta.jar`
- Java 21 required for NeoForge 1.21.1
- Compat modules are optional soft dependencies
- Perfect rituals still use @RitualRegister annotation (verify)
