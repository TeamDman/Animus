<p align="center">
  <img src="https://raw.githubusercontent.com/TeamDman/Animus/1.21.1-neovitae/src/main/resources/assets/animusnv/textures/misc/animus_512px.png" alt="Animus Logo" width="256"/>
</p>

<p align="center">
  <a href="https://minecraft.curseforge.com/projects/animus"><img src="http://cf.way2muchnoise.eu/full_animus_downloads.svg" alt="CurseForge Downloads"/></a>
  <a href="https://minecraft.curseforge.com/projects/animus"><img src="http://cf.way2muchnoise.eu/versions/animus.svg" alt="Minecraft Versions"/></a>
</p>

<p align="center">
  <strong>A comprehensive addon for <a href="https://github.com/BreakInBlocks/NeoVitae/">NeoVitae</a></strong><br/>
  Expanding the dark arts with new sigils, rituals, weapons, and cross-mod compatibility
</p>

---

## What is Animus?

Animus extends NeoVitae with dozens of new features: sigils, rituals, sentient and bound weapons, and deep integration with other magic mods. Manipulate time, corrupt the world with AntiLife, cast Iron's Spells using your Anima, harvest souls for Malum, or bridge EV with EvilCraft blood. The blood is the way.

> **Terminology**
>
> - **Anima**, your personal life-pool
> - **Essentia Vitae** (EV), the fluid your Anima holds
> - **Ara Vitae**, the altar
> - **Spiritus**, the aspected essence harvested from souls; aspects are **Raw**, **Ruina**, **Nihilum**, **Vindicta**, **Invictus**
> - **Tabula** slate progression, Rasa, Robur, Animata, Spiritus, Aetherea
> - **Sentient Armor**, the upgradable armor set
> - **Spiritus Gem**, the gem that stores Spiritus

---

## Features

### Sigils

Portable tools powered by your Anima.

| Sigil | Description |
|-------|-------------|
| **Sigil of Transposition** | Store and place blocks, or bind to a Teleposer to teleport entities |
| **Sigil of the Storm** | Summon lightning bolts at the block you target |
| **Sigil of Consumption** | Convert blocks you touch into corrupting AntiLife |
| **Sigil of Heavenly Wrath** | Levitate enemies, then drop them for devastating fall damage while counteracting flight |
| **Sigil of Temporal Dominance** | Accelerate block entities up to 32x speed |
| **Sigil of Equivalency** | Mass-replace blocks in a configurable radius |
| **Sigil of the Free Soul** | Enter ghost mode for scouting; also functions as a Totem of Undying |
| **Sigil of Remedium** | Automatically cleanse negative potion effects from you |
| **Sigil of Reparare** | Passively repair damaged items in your inventory |
| **Sigil of Nature's Leach** | Consume nearby plants to restore hunger |
| **Sigil of the Phantom Chain** | Capture entities into Mob Souls you can release elsewhere |
| **Sigil of the Fast Builder** | Accelerated block-placement speed |
| **Sigil of the Demon Monk** | Unarmed combat mode with bonus damage scaling on Spiritus |

### Perfect Rituals

Continuous ceremonial magic for automation and world manipulation.

| Ritual | Effect |
|--------|--------|
| **Ritual of Culling** | Automated mob killing in a radius; generates EV per kill and feeds the altar |
| **Ritual of Entropy** | Converts items inserted via chest into cobblestone |
| **Ritual of Luna** | Harvests light-emitting blocks in the area into a chest |
| **Ritual of Nature's Leach** | Consumes nearby plants to fill the altar with EV |
| **Ritual of Nolite Ignem** | Extinguishes all fires within range |
| **Ritual of Animal Luring** | Spawns passive animals within range |
| **Ritual of Persistence** | Keeps chunks loaded across the affected area |
| **Ritual of Relentless Tides** | Places fluids from a tank above into the world below |
| **Ritual of Reparare** | Repairs items in inventories above the ritual |
| **Ritual of Serenity** | Prevents hostile mob spawning in the radius |
| **Ritual of Siphon** | Extracts fluids from the world into a tank above |
| **Ritual of Sol** | Places light sources (from a chest above) into dark areas |
| **Ritual of the Steadfast Heart** | Grants Absorption hearts to nearby players; also generates Invictus Spiritus |
| **Ritual of Unmaking** | Extracts enchantments from items onto books |

### Imperfect Rituals

Single-use rituals activated by placing a block on an Imperfect Ritual Stone.

| Ritual | Trigger Block | Effect |
|--------|---------------|--------|
| **Ritual of Regression** | Bookshelf | Remove repair cost from a held item |
| **Ritual of Reduction** | Bookshelf | Downgrade enchantments on the held item |
| **Ritual of Enhancement** | Amethyst Block | Upgrade all enchantments on the held item by 1 level |
| **Ritual of Boundless Skies** | Ancient Debris | 15 minutes of creative-style flight |
| **Ritual of Hunger** | Bone Block | Drain your hunger instantly |
| **Ritual of Clear Skies** | Glowstone | Stop rain and thunder |
| **Ritual of Neptune's Blessing** | Prismarine | Water Breathing and Dolphin's Grace for 15 minutes |
| **Ritual of the Warden** | Sculk | Obsidian Cloak for 15 minutes |

Cross-mod imperfect rituals are listed under [Mod Compatibility](#mod-compatibility).

### Weapons and Tools

| Item | Description |
|------|-------------|
| **Iron Spear / Diamond Spear** | Throwable spears with retrievable mechanics |
| **Bound Spear** | Anima-bound spear; returns to you and strikes in an area |
| **Sentient Spear** | Scales with Spiritus; AoE effects vary by attuned aspect (Ruina, Nihilum, Vindicta, Invictus) |
| **Sentient Bow** | Consumes Spiritus per shot instead of arrows; arrows are spectral and vanish after impact |
| **Hellforged Bow** | EV-powered bow with extended-draw charging and an execute mechanic at full charge |
| **Sentient Shield** | Blocking grants buffs based on Spiritus aspect; +30% Spiritus gain while equipped |
| **Sanguine Diviner** | Inspect altars (tier, EV, capacity, rune bonuses), preview the next-tier multiblock as ghost blocks, auto-place upgrade blocks from your inventory, and inspect or dismantle rituals |
| **Codex Animus** | In-game Modonomicon-based guidebook covering every feature |

### Blocks and World Content

- **Crystallized Spiritus Blocks**, Animus's contribution to the `neovitae:altar/t6_capstones` tag; required for the top-tier (Transcendent) Ara Vitae.
- **Willful Stone**, decorative building blocks in 16 colors plus an uncolored base, fully unbreakable to anyone other than the owner.
- **Transcendent Orb of Vitae**, an EV-capacity Orb of Vitae for endgame storage.
- **Blood Trees**, sapling, logs, leaves, planks, and the full wood-family set (stairs, slabs, fences). Blood Apples drop from the leaves and restore hunger and EV when eaten.
- **Blood Core**, a functional block that emits corruption effects when activated by lightning and accelerates nearby Blood Tree growth.
- **AntiLife**, a corrupting fluid created when lightning strikes Essentia Vitae. Spreads and converts nearby blocks. Comes with a bucket.
- **Living Terra**, a land-enriching fluid that slowly solidifies into dirt. Comes with a bucket.
- **Fragment of Healing**, a permanent passive health-buff item; cannot be dropped or moved out of your inventory.

### Sanguine Diviner

Animus's signature inspection tool. Right-click an Ara Vitae for a full readout of current EV, capacity, tier, rune breakdown, and every altar bonus (speed, dislocation, sacrifice, self-sacrifice, orb capacity, capacity, efficiency, tick rate).

Hold the diviner and look at an altar to see a holographic preview of the next tier's required blocks. Sneak-right-click to auto-place upgrade blocks from your inventory, walking the altar up tier by tier until you run out of materials or hit the max.

The diviner reads the live NeoVitae datapack registry, so it correctly handles custom altar tiers added by other packs. It also works on master ritual stones (shows owner, ritual name, active or inactive; sneak-right-click to dismantle) and on Ars Nouveau Arcane Runes (Source level, speed multiplier, dislocation bonus).

---

## Mod Compatibility

Animus provides deep integration with several other mods. Each integration is fully soft-dependent; its features only load when the target mod is present, so Animus is safe to install standalone.

### Iron's Spells 'n Spellbooks

- **EV Spell Casting**, cast any Iron's Spells spell using Essentia Vitae instead of mana. Configurable hybrid mode lets you mix EV and mana.
- **Blood-Infused Spellbook**, crafted at the Ara Vitae. Reduces mana costs and passively regenerates EV.
- **Sanguine Scrolls**, reusable spell scrolls powered by EV. Five tiers based on Tabula slate progression (Rasa, Robur, Animata, Spiritus, Aetherea), each with increasing use counts.
- **Sigil of Crimson Will**, a sigil that boosts spell damage based on your stored Spiritus.
- **Ritual of Arcane Mastery**, searches nearby chests for Iron's Spells scrolls and upgrades their spell level; cost scales with the spell's rarity and current level.
- **Ritual of the Iron Heart** (imperfect, activated by an Arcane Anvil), Echoing Strikes III for 15 minutes.
- **Arcane Channeling**, a Sentient Armor upgrade tree that grants armor XP for spellcasting and unlocks spell-power bonuses.

### Ars Nouveau

- **Arcane Rune**, an altar rune block powered by Source from nearby Source Jars; contributes to altar speed and dislocation bonuses.
- **Ritual of Source Vitaeum**, converts Source from nearby Source Jars into EV.
- **Ritual of Ars Vitae**, the reverse trade: drains EV from the owner's network to fill a Source Jar above the ritual.
- **Ritual of the Magi** (imperfect, activated by a Source Gem Block), Mana Regeneration for 15 minutes.
- **Source Attunement**, a Sentient Armor upgrade tree that grants armor XP from glyph casting and unlocks spell-damage bonuses.

### Malum

- **Runic Sentient Scythe**, a hybrid weapon: 30% faster attacks, scales with Spiritus, and auto-harvests Malum spirits from kills.
- **Hand of Death**, an endgame soul reaper with lifesteal and execute mechanics.
- **Ritual of Soul-Stained Blood** (imperfect, activated by a Hallowed Gold block), Gaia's Bulwark for 15 minutes.
- **Spirit harvesting**, sentient weapons harvest Malum spirits when Malum is loaded, on top of their normal Spiritus drops.

### EvilCraft

- **Sanguine Rectifier**, a block that bridges NeoVitae EV and EvilCraft Blood. EV from a bound Orb of Vitae converts to Blood (for EvilCraft devices), and EvilCraft Blood feeds into the Ara Vitae. Transfer rates scale with altar speed runes.

---

## Installation

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1.
2. Install [NeoVitae](https://www.curseforge.com/minecraft/mc-mods/neovitae) 1.0.3 or later.
3. Download Animus from [CurseForge](https://minecraft.curseforge.com/projects/animus).
4. Place the jar in your `mods` folder.

### Optional Dependencies

- [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks), spell integration
- [Ars Nouveau](https://www.curseforge.com/minecraft/mc-mods/ars-nouveau), Source integration
- [Malum](https://www.curseforge.com/minecraft/mc-mods/malum), soul-harvesting integration
- [EvilCraft](https://www.curseforge.com/minecraft/mc-mods/evilcraft), blood-bridge integration
- [Curios](https://www.curseforge.com/minecraft/mc-mods/curios), lets sigils, the Key of Binding, and similar items live in dedicated slots
- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei), recipe lookup including dedicated categories for Hellfire Forge and Ara Vitae recipes
- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade), in-game block and entity info

---

## In-Game Guide

Animus ships the **Codex Animus** guidebook (Modonomicon-based). Craft it or pick one up from the creative tab to browse every sigil, ritual, weapon, and cross-mod feature with recipes and mechanics.

---

## Credits

- **Authors:** TeamDman and Saereth
- **Special Thanks:** WayOfTime for the original Blood Magic.

---

## Links

- [CurseForge](https://minecraft.curseforge.com/projects/animus), downloads and feature overview
- [GitHub](https://github.com/TeamDman/Animus), source code and issues (1.21.1 work on the `1.21.1-neovitae` branch)
- [NeoVitae](https://github.com/BreakInBlocks/NeoVitae/), base mod

---

## License

MIT License
