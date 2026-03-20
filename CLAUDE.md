# Animus - Blood Magic Addon

## Project Overview

Animus is a comprehensive Blood Magic addon for Minecraft that adds new sigils, rituals, tools, blocks, and cross-mod compatibility features. Originally created by TeamDman, now maintained by Saereth.

**Current Version**: 4.0.1 (NeoForge 1.21.1)
**Mod ID**: `animus`
**Package**: `com.teamdman.animus`

## Blood Magic Reference Repository

For Blood Magic 1.21.1 source code reference, use:

- **Repository**: C:\Users\Saereth\Documents\code\BloodMagic
- **Branch**: `1.21.1`

---

## NeoVitae (Blood Magic) 1.21.1 API Reference

Blood Magic has been renamed to **NeoVitae** for 1.21.1. All packages, namespaces, and mod IDs have changed.

- **Mod ID**: `neovitae` (NOT `bloodmagic` or `bloodmagicnv`)
- **Package**: `com.breakinblocks.neovitae` (NOT `wayoftime.bloodmagic`)
- **Source**: `C:\Users\Saereth\Documents\code\NeoVitae`
- **Maven**: `com.breakinblocks.neovitae:NeoVitae:3.4.0-3-beta` (from mavenLocal)
- **API entry point**: `NeoVitaeAPI.get()` returns `INeoVitaeAPI`

### Key NeoVitae Classes

#### Core Systems

- `com.breakinblocks.neovitae.NeoVitae` - Main mod class, MODID = "neovitae"
- `com.breakinblocks.neovitae.api.NeoVitaeAPI` - API entry point, use `NeoVitaeAPI.get()`
- `com.breakinblocks.neovitae.api.INeoVitaeAPI` - Main API interface

#### Soul Network (LP System)

- `com.breakinblocks.neovitae.api.soul.ISoulNetwork` - Interface for soul network access
- `com.breakinblocks.neovitae.common.datacomponent.SoulNetwork` - Soul network data component
- `com.breakinblocks.neovitae.util.helper.SoulNetworkHelper` - Helper for `getSoulNetwork(UUID)`
- `com.breakinblocks.neovitae.api.soul.SoulTicket` - Create with `SoulTicket.create(amount)` for syphoning
- `com.breakinblocks.neovitae.common.datacomponent.Binding` - Record `Binding(UUID uuid, String name)` for item ownership

#### Blood Altar

- `com.breakinblocks.neovitae.common.blockentity.BloodAltarTile` - Blood altar tile entity
- `com.breakinblocks.neovitae.api.altar.IBloodAltar` - Altar interface
- `com.breakinblocks.neovitae.api.altar.IAltarRuneRegistry` - Custom rune registration

#### Rituals

- `com.breakinblocks.neovitae.ritual.Ritual` - Abstract base class for rituals
- `com.breakinblocks.neovitae.ritual.ImperfectRitual` - Base class for imperfect rituals
- `com.breakinblocks.neovitae.ritual.RitualRegistry` - Registry keys for ritual DeferredRegister
- `com.breakinblocks.neovitae.api.ritual.IMasterRitualStone` - MRS interface for ritual logic
- `com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone` - Imperfect ritual stone interface
- `com.breakinblocks.neovitae.api.ritual.RitualComponent` - Rune placement component
- `com.breakinblocks.neovitae.api.ritual.EnumRuneType` - Rune types (BLANK, WATER, FIRE, EARTH, AIR, DUSK, DAWN)
- `com.breakinblocks.neovitae.api.ritual.AreaDescriptor` - Area definitions for ritual ranges

#### Demon Will

- `com.breakinblocks.neovitae.common.datacomponent.EnumWillType` - Will types (DEFAULT, CORROSIVE, DESTRUCTIVE, VENGEFUL, STEADFAST)
- `com.breakinblocks.neovitae.will.WorldDemonWillHandler` - Chunk will management
- `com.breakinblocks.neovitae.will.PlayerDemonWillHandler` - Player inventory will management
- `com.breakinblocks.neovitae.will.IDemonWill` - Will item interface (createWill, getWill, getType)
- `com.breakinblocks.neovitae.will.IDemonWillGem` - Tartaric gem interface
- `com.breakinblocks.neovitae.common.item.soul.ISentientTool` - Sentient weapon interface (replaces IDemonWillWeapon)

#### Sigils

- `com.breakinblocks.neovitae.api.sigil.ISigilEffect` - Custom sigil effect interface
- `com.breakinblocks.neovitae.api.registry.NeoVitaeRegistries.SIGIL_EFFECT_TYPE_KEY` - Registry key for sigil effects
- Sigil type data: `data/<namespace>/neovitae/sigil_type/*.json`

#### Items & Blocks

- `com.breakinblocks.neovitae.common.block.BMBlocks` - Block registration
- `com.breakinblocks.neovitae.common.item.BMItems` - Item registration (MONSTER_SOUL_RAW, etc.)
- `com.breakinblocks.neovitae.common.datacomponent.BMDataComponents` - Data components (BINDING, ANOINTMENT_HOLDER, etc.)

#### Events

- `com.breakinblocks.neovitae.api.event.SoulNetworkEvent.PreSyphon` - LP syphon event (cancellable, modifiable)
- `com.breakinblocks.neovitae.common.event.SacrificialDaggerEvent` - Self-sacrifice event (lpAdded modifiable)

#### Recipes

- `com.breakinblocks.neovitae.common.recipe.BMRecipes` - Recipe types and serializers registry
- Soul Forge: type `neovitae:soul_forge`
- Blood Altar: type `neovitae:blood_altar_recipe`
- Alchemy Array: type `neovitae:array`

---

## NeoForge 1.21.1 Event System

### EventBusSubscriber Pattern (IMPORTANT)

In NeoForge 1.21.x, the `bus` parameter in `@EventBusSubscriber` is **deprecated**. NeoForge now auto-detects which bus to use based on whether the event implements `IModBusEvent`.

#### Correct Pattern (NeoForge 1.21.1+)

```java
// For game/NeoForge bus events (most events like PlayerTickEvent, LivingDeathEvent, etc.)
@EventBusSubscriber(modid = "yourmodid")
public class MyEventHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) { }
}

// For client-side only events
@EventBusSubscriber(value = Dist.CLIENT, modid = "yourmodid")
public class MyClientHandler {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) { }
}

// For mod bus events (GatherDataEvent, FMLClientSetupEvent, etc.)
// These events implement IModBusEvent and are auto-detected
@EventBusSubscriber(modid = "yourmodid")
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) { }
}
```

#### Deprecated Pattern (DO NOT USE)

```java
// DEPRECATED - causes warnings
@EventBusSubscriber(modid = "yourmodid", bus = EventBusSubscriber.Bus.GAME)  // Don't specify bus
@EventBusSubscriber(modid = "yourmodid", bus = EventBusSubscriber.Bus.MOD)   // Don't specify bus
```

#### Key Points

- **Do NOT specify `bus` parameter** - it's deprecated and marked for removal
- NeoForge auto-detects the correct bus based on `IModBusEvent` interface
- Always specify `modid` for easier debugging
- Use `value = Dist.CLIENT` for client-only event handlers
- All handler methods must be `static` when using the annotation

---

## Recipe JSON Formats (NeoForge 1.21.1)

### src/generated Files

- Should not be edited, they will be regenerated anytime runData is run.
- If there are recipe errors the recipes need to be correct in the dataGen Providers.

### CRITICAL: NeoForge 1.21 Recipe Conventions

- Recipe path: `data/<namespace>/recipe/` (singular, NOT `recipes/`)
- Tag path: `data/<namespace>/tags/item/` (singular, NOT `tags/items/`)
- Item output format: `{"id": "mod:item", "count": 1}` (NOT `{"item": "mod:item"}`)
- NeoVitae namespace: `neovitae`
- Blood Magic item names use snake_case: `blank_slate`, `reinforced_slate`, `imbued_slate`, `demonic_slate`, `ethereal_slate`, `demon_slate`
- Tags use `c:` prefix (NOT `forge:`)

### Soul Forge (Hellfire Forge) Recipes

**Type**: `neovitae:soul_forge`
**Path**: `data/animus/recipe/soulforge/*.json`

```json
{
  "type": "neovitae:soul_forge",
  "minDrain": 128.0,
  "drain": 64.0,
  "inputs": [
    { "item": "minecraft:item1" },
    { "item": "minecraft:item2" },
    { "tag": "c:some_tag" },
    { "item": "minecraft:item4" }
  ],
  "output": {
    "id": "animus:output_item",
    "count": 1
  },
  "willType": "destructive"
}
```

- `minDrain`: Minimum demon will required (double)
- `drain`: Will consumed on craft (double)
- `inputs`: Array of 1-4 Ingredient objects
- `output`: ItemStack.CODEC format with `id` and `count`
- `willType`: Optional - specific will type required (DEFAULT, CORROSIVE, DESTRUCTIVE, VENGEFUL, STEADFAST)

### Blood Altar Recipes

**Type**: `neovitae:blood_altar_recipe`
**Path**: `data/animus/recipe/blood_altar/*.json`

```json
{
  "type": "neovitae:blood_altar_recipe",
  "input": {
    "item": "minecraft:input_item"
  },
  "output": {
    "id": "animus:output_item",
    "count": 1
  },
  "minTier": 3,
  "bloodNeeded": 10000,
  "craftSpeed": 100,
  "drainSpeed": 50,
  "copyInputComponents": false
}
```

- `input`: Ingredient (single item or tag)
- `output`: ItemStack.CODEC format
- `minTier`: Required altar tier (0-5, where 0 = tier 1)
- `bloodNeeded`: Total LP consumed
- `craftSpeed`: Ticks per craft operation
- `drainSpeed`: LP drained per tick
- `copyInputComponents`: Optional - copy data components from input

### Alchemy Array Recipes

**Type**: `neovitae:array`
**Path**: `data/animus/recipe/array/*.json`

```json
{
  "type": "neovitae:array",
  "texture": "neovitae:textures/models/alchemyarrays/sigil.png",
  "baseinput": {
    "item": "animus:reagent_item"
  },
  "addedinput": {
    "item": "neovitae:reinforced_slate"
  },
  "output": {
    "id": "animus:sigil_output",
    "count": 1
  },
  "effect_type": "crafting"
}
```

- `texture`: Resource location for array texture
- `baseinput`: Ingredient placed on array first (reagent)
- `addedinput`: Ingredient added to activate (usually slate)
- `output`: ItemStack.CODEC format (can be empty for non-crafting effects)
- `effect_type`: Optional - CRAFTING (default), BINDING, etc.

---

## Ritual Implementation Pattern

### Regular Rituals

```java
public class MyRitual extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public MyRitual() {
        super("ritual_name", 1, 5000, "ritual.modid.ritual_name");
        // crystalLevel: 1 = weak crystal, 2 = awakened crystal
        // activationCost: LP to activate

        // Define modifiable area ranges
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(
            new BlockPos(-10, -10, -10), 21, 21, 21));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        BlockPos pos = masterRitualStone.getMasterBlockPos();
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(masterRitualStone.getOwner());

        // Get area and perform effect
        AreaDescriptor range = getBlockRange(EFFECT_RANGE);
        AABB aabb = range.getAABB(pos);

        // Drain LP
        network.syphon(SoulTicket.create(getRefreshCost()));
    }

    @Override
    public int getRefreshCost() { return 100; }

    @Override
    public int getRefreshTime() { return 20; } // ticks

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Use helper methods: addRune, addCornerRunes, addParallelRunes, addOffsetRunes
        addRune(components, 1, 0, 1, EnumRuneType.FIRE);
        addCornerRunes(components, 2, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() { return new MyRitual(); }
}
```

### Imperfect Rituals

```java
public class MyImperfectRitual extends ImperfectRitual {
    public MyImperfectRitual() {
        super("imperfect_name",
              state -> state.is(Blocks.COAL_BLOCK), // block requirement
              5000, // activation cost
              true, // lightning show
              "ritual.modid.imperfect_name");
    }

    @Override
    public boolean onActivate(IImperfectRitualStone stone, Player player) {
        // Perform one-time effect
        return true; // success
    }
}
```

### Ritual Registration (Animus Pattern)

```java
public class AnimusRituals {
    public static final DeferredRegister<Ritual> RITUALS =
        DeferredRegister.create(RitualRegistry.RITUAL_REGISTRY_KEY, MODID);
    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, MODID);

    public static final DeferredHolder<Ritual, MyRitual> MY_RITUAL =
        RITUALS.register("my_ritual", MyRitual::new);

    public static void init() {} // Force class loading
}
```

---

## Project Structure

```
src/main/java/com/teamdman/animus/
├── Animus.java              # Main mod class
├── Constants.java           # Mod constants, ritual names, NBT keys
├── AnimusConfig.java        # Configuration
├── registry/
│   ├── AnimusItems.java     # Item registration
│   ├── AnimusBlocks.java    # Block registration
│   ├── AnimusRituals.java   # Ritual registration
│   └── ...
├── items/
│   ├── sigils/              # Sigil implementations
│   └── ...
├── rituals/
│   ├── RitualCulling.java   # Perfect ritual example
│   └── imperfect/           # Imperfect rituals
├── blocks/
├── blockentity/
├── network/
├── compat/
│   ├── ArsNouveauCompat.java
│   ├── MalumCompat.java
│   └── IronsSpellsCompat.java
├── worldgen/
└── events/
```

## Resource Structure

```
src/main/resources/data/animus/
├── recipe/
│   ├── blood_altar/         # Blood altar recipes
│   ├── soulforge/           # Hellfire forge recipes
│   ├── array/               # Alchemy array recipes
│   └── crafting/            # Regular crafting recipes
├── tags/
│   └── item/                # Item tags (singular!)
└── advancement/
```

## Key Features

Animus adds over a dozen new sigils, 18 new rituals (16 perfect, 8 imperfect), a new tier 6 blood altar, a new tier 6 blood orb, and a variety of new blocks, items, and weapons. It also features deep integration with other magic mods such as Ars Nouveau, Botania, Iron's Spells 'n Skills, and Malum.
