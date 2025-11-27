# Animus Mod - Comprehensive Testing Plan
## Blood Magic Addon for Minecraft 1.20.1

> **Note**: Pilum testing covered in separate `PILUM_TESTING_PLAN.md`

---

## PREREQUISITES

### Required Setup:
1. Install Blood Magic mod (dependency)
2. Have a Blood Altar built (Tier 1 minimum for basic tests)
3. Have LP (Life Points) available in soul network
4. Creative mode recommended for initial testing

### Quick LP Setup:
```
/bloodmagic network add @s 1000000
```

### Quick Item Commands:
```
# Sigils
/give @s animus:sigil_builder
/give @s animus:sigil_chains
/give @s animus:sigil_consumption
/give @s animus:sigil_leech
/give @s animus:sigil_storm
/give @s animus:sigil_transposition

# Items
/give @s animus:blood_apple
/give @s animus:fragment_healing
/give @s animus:altar_diviner
/give @s animus:key_binding
/give @s animus:mob_soul

# Blocks
/give @s animus:blood_wood
/give @s animus:blood_wood_stripped
/give @s animus:blood_wood_planks
/give @s animus:blood_sapling
/give @s animus:blood_leaves
/give @s animus:blood_core

# Fluids
/give @s animus:antimatter_bucket
/give @s animus:dirt_bucket

# Blood Magic Components (for binding)
/give @s bloodmagic:activationcrystal
/give @s bloodmagic:daggerofsacrifice
```

---

# 1. SIGILS TESTING

## 1.1 Sigil of the Phantom Builder

### Basic Functionality:
- [X] **Obtain sigil**: `/give @s animus:sigil_builder`
- [X] **Bind to player**: Right-click with sigil
- [X] **Verify bound status**: Check item tooltip shows owner

### Toggle Mechanics:
- [X] Hold sigil in main hand
- [X] **Shift + right-click**: Should toggle active/inactive
- [X] **Check texture change**: Active vs inactive state
- [X] Verify chat message confirms state change

### Single Block Placement:
- [ ] Put blocks in offhand (cobblestone, dirt, etc.)
- [ ] Activate sigil (shift + right-click)
- [ ] **Right-click in air**: Block should place 2 blocks ahead
- [ ] Check LP consumption: 100 LP per block
- [ ] **Test without LP**: Should fail with message
- [ ] **Test without blocks in offhand**: Should fail

### Area Fill Mode:
- [ ] Place a block on ground
- [ ] **Sneak + right-click on block face**: Should fill area
- [ ] Verify multiple blocks placed
- [ ] Check LP consumption: 100 LP × blocks placed
- [ ] Test on different faces (top, sides)
- [ ] Test config range setting

### Fast Building:
- [ ] With sigil active, rapid right-click
- [ ] **Verify no click delay**: Should place very fast
- [ ] Compare to vanilla (has delay)

### Edge Cases:
- [ ] Test with different block types (glass, wood, stone)
- [ ] Test when inventory/offhand empty
- [ ] Test when LP depleted
- [ ] Test binding to different players
- [ ] Deactivate and verify normal behavior returns

---

## 1.2 Sigil of Chains

### Basic Capture:
- [ ] **Obtain and bind sigil**: `/give @s animus:sigil_chains`
- [ ] Spawn test mob: `/summon minecraft:pig ~ ~ ~ {CustomName:'{"text":"TestPig"}'}`
- [ ] **Right-click mob**: Should capture it
- [ ] **Verify mob removed**: Mob should disappear
- [ ] **Verify mob soul created**: Check inventory for `mob_soul`
- [ ] Check LP consumption: 500 LP

### Mob Soul Inspection:
- [ ] Hover over mob soul item
- [ ] **Verify tooltip shows**: Entity type (e.g., "Pig")
- [ ] **If named mob**: Should show custom name
- [ ] Right-click to see full NBT data

### Capture Various Mobs:
- [ ] **Passive mobs**: Cow, sheep, chicken, pig
- [ ] **Hostile mobs**: Zombie, skeleton, creeper
- [ ] **Flying mobs**: Bat, parrot
- [ ] **Water mobs**: Fish, dolphin, squid
- [ ] **Nether mobs**: Piglin, hoglin
- [ ] **Custom named mobs**: Verify name preserved

### Boss Testing:
- [ ] Spawn Wither: `/summon minecraft:wither`
- [ ] Attempt capture
- [ ] **Expected**: Should capture (or check if blacklisted)
- [ ] Spawn Ender Dragon (End dimension)
- [ ] Attempt capture

### Edge Cases:
- [ ] **Full inventory**: Mob soul should drop on ground
- [ ] **Insufficient LP**: Should fail with message
- [ ] **Unbound sigil**: Should fail
- [ ] **Click on player**: Should not capture
- [ ] **Click on armor stand**: Test entity filtering

---

## 1.3 Sigil of Consumption

### Basic Antimatter Creation:
- [ ] **Obtain and bind sigil**: `/give @s animus:sigil_consumption`
- [ ] Place test blocks (cobblestone works well)
- [ ] Look at block within 5 blocks
- [ ] **Right-click**: Block should convert to antimatter
- [ ] **Verify antimatter texture**: Should look different
- [ ] Check LP consumption: 200 LP initial

### Antimatter Spreading:
- [ ] Create large area of same block type (9x9x9 cobblestone)
- [ ] Convert center block to antimatter
- [ ] **Observe spreading**: Should spread to adjacent matching blocks
- [ ] **Verify 3x3x3 spread range** per tick
- [ ] Watch LP drain as it spreads
- [ ] **Verify only spreads to matching blocks**

### Different Block Types:
- [ ] Test on stone, dirt, wood, ores
- [ ] Verify each creates spreading antimatter
- [ ] Test mixing block types (should only spread to same)

### Antimatter Decay:
- [ ] Break an antimatter block
- [ ] **Should enter "decay" state**
- [ ] Watch decay spread to adjacent antimatter
- [ ] Verify all antimatter eventually decays

### Restrictions:
- [ ] **Test on bedrock**: Should fail
- [ ] **Test on tile entities** (chests, furnaces): Should fail or handle safely
- [ ] **Test outside range** (> 5 blocks): Should not target
- [ ] Test with insufficient LP

---

## 1.4 Sigil of Nature's Leech

### Setup:
- [ ] **Obtain and bind sigil**: `/give @s animus:sigil_leech`
- [ ] **Toggle active**: Shift + right-click
- [ ] Verify active state message

### Hunger Mechanics:
- [ ] Reduce player hunger: `/effect give @s minecraft:hunger 30 10`
- [ ] Keep sigil in inventory (doesn't need to be held)
- [ ] **Add plants to inventory** (wheat, carrots, potatoes)
- [ ] Wait for hunger to drop
- [ ] **Verify auto-feeding**: Should consume plants and restore hunger

### World Consumption:
- [ ] Place consumable blocks nearby (grass, flowers, crops)
- [ ] Create hungry state
- [ ] **Should consume from world** (10x10x10 area, 5 block radius)
- [ ] Verify blocks disappear
- [ ] Check hunger restored

### Consumable Block Types:
- [ ] **Logs**: Oak, spruce, birch, etc.
- [ ] **Flowers**: Dandelion, poppy, etc.
- [ ] **Crops**: Wheat, carrots, potatoes
- [ ] **Saplings**: All types
- [ ] **Grass**: Tall grass, ferns
- [ ] **Kelp, vines, moss**
- [ ] **Leaves**: All types

### Configuration:
- [ ] Check config for blacklist
- [ ] Add block to blacklist
- [ ] Verify blacklisted block not consumed

### LP Consumption:
- [ ] Monitor LP during feeding
- [ ] **Verify 5 LP per cycle**
- [ ] Test with insufficient LP (should deactivate)

### Hunger Restoration:
- [ ] Track hunger before/after
- [ ] **Should restore 1-3 food points**
- [ ] **Should restore 2.0 saturation**

---

## 1.5 Sigil of the Storm

### Basic Lightning:
- [ ] **Obtain and bind sigil**: `/give @s animus:sigil_storm`
- [ ] Look at ground nearby
- [ ] **Right-click**: Lightning should strike
- [ ] **Verify visual effect**: Lightning bolt
- [ ] **Verify damage**: Lightning causes fire/damage
- [ ] Check LP consumption: 500 LP

### Range Testing:
- [ ] Test at various distances
- [ ] **Max range**: 64 blocks
- [ ] **Beyond range**: Should fail or nothing happens
- [ ] Test vertical targeting (up/down)

### Water Fish Spawning:
- [ ] Create water source/pool
- [ ] Target water with sigil
- [ ] **Right-click**: Should spawn 1-3 fish
- [ ] Verify fish spawn in water
- [ ] Verify fish types (cod, salmon, etc.)

### Rain AOE Damage:
- [ ] **Start rain**: `/weather rain`
- [ ] Spawn mobs in 5 block radius
- [ ] Use sigil during rain
- [ ] **Verify AOE damage**: Nearby mobs take damage
- [ ] **Damage amount**: 4.0 hearts (8 damage)
- [ ] **Range**: 5 block radius
- [ ] Test requires skylight visibility

### Edge Cases:
- [ ] Test in different dimensions (Nether, End)
- [ ] Test underground (no skylight)
- [ ] Test on non-solid blocks (air)
- [ ] Test insufficient LP
- [ ] Test targeting entities vs blocks

---

## 1.6 Sigil of Transposition

### Two-Step Movement:
- [ ] **Obtain and bind sigil**: `/give @s animus:sigil_transposition`
- [ ] Place test block (chest with items)
- [ ] **Step 1 - Select**: Right-click block to select
- [ ] **Verify message**: "Block selected" or similar
- [ ] **Step 2 - Move**: Right-click destination
- [ ] **Verify block moved**: Block appears at new location
- [ ] **Verify tile entity preserved**: Chest inventory intact

### Clear Selection:
- [ ] Select a block
- [ ] **Right-click air**: Should clear selection
- [ ] Verify message confirms cleared

### Tile Entity Testing:
- [ ] **Chest with items**: Move and verify inventory preserved
- [ ] **Furnace with items/fuel**: Move and verify contents
- [ ] **Blood Altar**: Move and verify LP/tier preserved
- [ ] **Other tile entities**: Test hopper, dispenser, etc.

### LP Consumption:
- [ ] Monitor LP before/after move
- [ ] **Verify cost**: 5000 LP per move
- [ ] Test with insufficient LP

### Restrictions:
- [ ] **Test bedrock**: Should fail (unbreakable)
- [ ] **Test air**: Should fail (no block)
- [ ] **Test destination not air**: Should fail
- [ ] Check config for unbreakable block list

### Antimatter Special Case:
- [ ] Move antimatter block
- [ ] **Verify decay spread**: Should spread to new neighbors
- [ ] Test antimatter seeking behavior updates

### Edge Cases:
- [ ] Move block to same location
- [ ] Select and log out (state persistence)
- [ ] Rapid selection changes
- [ ] Move across chunk boundaries

---

# 2. RITUALS TESTING

## General Ritual Setup

### Building Rituals:
1. Craft Master Ritual Stone (Blood Magic)
2. Craft required Ritual Stones (runes)
3. Build ritual structure (see each ritual)
4. Use Ritual Diviner to preview structure
5. Activate with Activation Crystal

### Activation Crystal:
```
/give @s bloodmagic:activationcrystal
```

---

## 2.1 Ritual of Luna (Lux Lunae) - Light Harvester

### Ritual Structure:
```
Runes Required: 12 Dusk Runes
Layout: 3 layers of 4 corners each
Spacing: Check with Ritual Diviner
```

### Building:
- [ ] Place Master Ritual Stone (MRS)
- [ ] Build 3-layer Dusk rune pattern (4 corners per layer)
- [ ] **Place chest 1 block above MRS**
- [ ] Verify with Ritual Diviner

### Activation:
- [ ] Hold Activation Crystal
- [ ] Right-click MRS
- [ ] **Check LP cost**: 1000 LP
- [ ] Verify ritual activates (particle effects)

### Light Block Detection:
- [ ] Place light sources in range (torches, glowstone, lanterns)
- [ ] **Default range**: 32 blocks radius (configurable)
- [ ] **Scan volume**: 65×65×65
- [ ] Wait for ritual tick (5 ticks refresh)
- [ ] **Verify harvested**: Light blocks disappear
- [ ] **Check chest**: Should contain harvested items

### LP Consumption:
- [ ] Monitor LP per block harvested
- [ ] **Cost**: 1 LP per block
- [ ] Test with many blocks (drain test)

### No Chest Behavior:
- [ ] Remove chest
- [ ] Trigger harvest
- [ ] **Verify drops on ground**: Items drop at MRS

### Full Chest Behavior:
- [ ] Fill chest completely
- [ ] **Expected**: Ritual should stop or drop items

### Light Source Types:
- [ ] **Torches**: All types
- [ ] **Glowstone, sea lanterns**
- [ ] **Lanterns, jack-o-lanterns**
- [ ] **Redstone lamps** (powered)
- [ ] **End rods, conduits**
- [ ] **Custom light blocks** (from other mods)

### Config Testing:
- [ ] Adjust harvest range in config
- [ ] Reload and verify new range applies

---

## 2.2 Ritual of Sol (Lux Solis) - Light Placer

### Ritual Structure:
```
Runes Required: 12 Air Runes
Layout: 3 layers of 4 corners each
```

### Building:
- [ ] Place Master Ritual Stone
- [ ] Build Air rune pattern
- [ ] **Place chest with light sources above MRS**
- [ ] Verify structure

### Activation:
- [ ] Activate with crystal
- [ ] **LP cost**: 1000 LP
- [ ] Verify activation

### Dark Area Detection:
- [ ] Create dark areas (light level < 8)
- [ ] **Range**: 32 blocks (configurable)
- [ ] Ritual should detect dark spots
- [ ] Wait for 5-tick refresh

### Light Placement:
- [ ] **Should place lights** from chest inventory
- [ ] **Only on solid surfaces**
- [ ] **Only in dark areas** (light < 8)
- [ ] Verify light appears in correct spots

### Blood Light Sigil Special:
- [ ] Give Blood Magic's Sigil of Blood Light
- [ ] Put in chest
- [ ] **Should place without consuming**: Infinite use
- [ ] Verify sigil stays in chest

### LP Consumption:
- [ ] **Cost**: 1 LP per block placed
- [ ] Monitor during placement

### Empty Chest:
- [ ] Remove all light sources
- [ ] **Expected**: Ritual can't place, no errors

### Solid Surface Requirement:
- [ ] Create areas with air below
- [ ] **Verify no placement in air**
- [ ] Only places where solid block exists

### Different Light Types:
- [ ] Test torches, glowstone, lanterns
- [ ] Verify all work correctly

---

## 2.3 Ritual of Peace (Pax Aeterna) - Peaceful Mob Spawner

### Ritual Structure:
```
Runes Required: 12 Earth/Water Runes mix
Layout: Check Ritual Diviner
```

### Building & Activation:
- [ ] Build ritual structure
- [ ] Activate with crystal
- [ ] **LP cost**: 5000 LP
- [ ] **Refresh interval**: 400 ticks (20 seconds)

### Mob Spawning:
- [ ] Wait for first spawn cycle (400 ticks)
- [ ] **Verify peaceful mobs spawn**: Animals, not monsters
- [ ] **Spawn radius**: 8 blocks from MRS
- [ ] Verify spawn position validation (solid ground, light level, etc.)

### Mob Type Filtering:
- [ ] **Should spawn**: Cows, pigs, sheep, chickens, etc.
- [ ] **Should NOT spawn**: Zombies, skeletons, creepers
- [ ] Verify creature categories: CREATURE, AMBIENT, WATER_CREATURE, WATER_AMBIENT

### Spawn Position:
- [ ] Clear area around MRS
- [ ] Verify spawns within 8 block radius
- [ ] Check spawns on valid surfaces only

### LP Refresh:
- [ ] Monitor LP consumption
- [ ] **Cost per spawn**: Configurable (check config)
- [ ] Test with insufficient LP (should stop)

### Config Options:
- [ ] Check config for cost adjustment
- [ ] Modify and reload
- [ ] Verify changes apply

### Long-term Test:
- [ ] Let ritual run for 5+ minutes
- [ ] Count total mobs spawned
- [ ] Verify consistent spawn rate (every 400 ticks)

---

## 2.4 Ritual of Entropy (Dissolutio) - Item to Cobblestone

### Ritual Structure:
```
Runes Required: 12 Earth Runes
Layout: 3 layers of 4 corners
```

### Building:
- [ ] Build ritual with Earth runes
- [ ] **Place chest above MRS**
- [ ] Fill chest with unwanted items

### Activation:
- [ ] Activate with crystal
- [ ] **LP cost**: 1000 LP
- [ ] **Refresh**: 1 tick (very fast!)

### Conversion Testing:
- [ ] Add various items to chest
- [ ] **Verify 1:1 conversion**: 1 item → 1 cobblestone
- [ ] Check processing speed (1 tick = very fast)
- [ ] Verify chest fills with cobblestone

### Cobblestone Skip:
- [ ] Put cobblestone in chest
- [ ] **Verify skipped**: Cobblestone not converted
- [ ] Only non-cobblestone items process

### LP Consumption:
- [ ] **Cost**: 1 LP per item
- [ ] Process large batch and monitor LP drain

### Different Item Types:
- [ ] **Tools**: Diamond pick, iron sword
- [ ] **Food**: Bread, apples
- [ ] **Blocks**: Dirt, stone, wood
- [ ] **Ores**: Iron ore, gold ore
- [ ] Verify all convert to cobblestone

### Speed Test:
- [ ] Fill chest with 1000+ items
- [ ] Time full conversion
- [ ] Verify consistent 1 tick processing

---

## 2.5 Ritual of Unmaking (Solutio Fascinationis) - Enchant Extraction

### Ritual Structure:
```
Runes Required: 27 total (Fire, Dusk, Air, Earth, Water)
Layout: Complex multi-layer (use Ritual Diviner!)
```

### Building:
- [ ] Build large ritual structure
- [ ] Verify all 27 runes placed correctly
- [ ] Clear 5×5×5 area around MRS

### Activation:
- [ ] **LP cost**: 3000 LP
- [ ] **Refresh**: 20 ticks
- [ ] **One-shot**: Deactivates after processing

### Enchanted Item Extraction:
- [ ] Enchant item (diamond sword with Sharpness V)
- [ ] Add books to ritual area (need 1 book per enchant)
- [ ] Drop enchanted item near MRS (within 5×5×5)
- [ ] **Trigger ritual**
- [ ] **Verify**: Enchantment extracted to book
- [ ] **Verify**: Original item loses enchantment
- [ ] **Verify**: 1 book consumed per enchant

### Enchanted Book Splitting:
- [ ] Create enchanted book (Sharpness V)
- [ ] Add 2 regular books to area
- [ ] Drop enchanted book near MRS
- [ ] **Trigger ritual**
- [ ] **Verify**: Creates 2 books with Sharpness IV (level - 1)
- [ ] **Verify**: Original book consumed
- [ ] **Verify**: Level reduced but min 1

### Multiple Enchantments:
- [ ] Enchant sword with 3 enchants (Sharpness, Knockback, Fire Aspect)
- [ ] Add 3 books
- [ ] Drop sword
- [ ] **Verify**: All enchants extracted separately
- [ ] **Verify**: 3 books consumed, 3 enchanted books created

### Book Requirement:
- [ ] Drop enchanted item without books
- [ ] **Expected**: Nothing happens or partial extraction
- [ ] Test with insufficient books (2 books, 3 enchants)

### Ritual Deactivation:
- [ ] **Verify one-shot**: Ritual stops after processing
- [ ] Must reactivate for next use

### Edge Cases:
- [ ] **Curse enchantments**: Test Curse of Binding, Vanishing
- [ ] **Max level enchants**: Sharpness V → IV → III (verify)
- [ ] **Level I enchants**: Should stay level I
- [ ] **Treasure enchantments**: Mending, Frost Walker

---

## 2.6 Ritual of the Steadfast Heart (Cor Constans) - Absorption Buff

### Ritual Structure:
```
Runes Required: Earth, Water, Air runes mix
Layout: Check Ritual Diviner
```

### Building & Activation:
- [ ] Build ritual structure
- [ ] **Activation cost**: 20,000 LP (expensive!)
- [ ] **Refresh interval**: 600 ticks (30 seconds)
- [ ] **Range**: 32 block radius

### Absorption Application:
- [ ] Stand within 32 blocks of MRS
- [ ] Wait for refresh (600 ticks)
- [ ] **Verify Absorption effect applied**
- [ ] Check hearts UI (yellow/gold hearts)

### Stacking Mechanics:
- [ ] Let ritual apply multiple times
- [ ] **Verify amplifier increases**: Max Level 4
- [ ] **Verify duration increases**: Max 30,000 ticks
- [ ] Test formula: New duration = current + 600 ticks (capped)

### Multiple Players:
- [ ] Have 2+ players in range
- [ ] **Verify all players affected**
- [ ] **LP cost**: 100 LP per player per refresh
- [ ] Test with many players (LP drain)

### Demon Will Generation:
- [ ] Check for Steadfast demon will generation
- [ ] Verify will type and amount
- [ ] Test will accumulation over time

### Range Testing:
- [ ] Stand at 32 blocks: Should receive
- [ ] Stand at 33 blocks: Should not receive
- [ ] Test vertical range (up/down)

### Fake Player Exclusion:
- [ ] Test with mod that creates fake players
- [ ] **Verify excluded**: No effect on fake players

### LP Drain Test:
- [ ] Start with limited LP
- [ ] Let ritual run
- [ ] Verify stops when LP depleted

---

## 2.7 Ritual of Nature's Leech (Sanguisugio Naturae) - Plant to LP

### Ritual Structure:
```
Runes Required: Water, Air, Earth runes
Layout: Medium complexity
```

### Altar Setup:
- [ ] Build Blood Altar within range
- [ ] **Altar range**: 11×21×11 from MRS
- [ ] Verify altar detected

### Plant Area Setup:
- [ ] Plant crops in 24×24×24 area
- [ ] Add grass, flowers, saplings
- [ ] Create diverse plant types

### Activation:
- [ ] **LP cost**: 3000 LP
- [ ] **Refresh cost**: 10 LP
- [ ] **Base refresh**: 80 ticks (modified by demon will)

### Plant Consumption:
- [ ] Activate ritual
- [ ] **Verify plants consumed**: Blocks disappear
- [ ] **Verify altar fills**: +50 LP per block
- [ ] Watch particles and sound effects

### Consumable Block Types:
- [ ] **Logs**: All wood types
- [ ] **Flowers**: All flower types
- [ ] **Crops**: Wheat, carrots, potatoes, beetroot
- [ ] **Saplings**: All types
- [ ] **Grass, tall grass, ferns**
- [ ] **Kelp, seagrass**
- [ ] **Vines, moss**
- [ ] **Leaves**: All types (if configured)

### Demon Will Mechanics:
- [ ] **Verify corrosive will generation**
- [ ] Test refresh rate variation
- [ ] **More will = slower refresh** (counter-intuitive!)
- [ ] Monitor will accumulation

### Configuration:
- [ ] Check blacklist in config
- [ ] Add block to blacklist (e.g., oak_log)
- [ ] Verify blacklisted block not consumed

### Range Testing:
- [ ] **Effect range**: 24×24×24 detection
- [ ] **Altar range**: 11×21×11 search
- [ ] Place plants outside range: Not consumed
- [ ] Place altar outside range: Ritual fails

### No Altar Test:
- [ ] Remove altar
- [ ] **Expected**: Ritual fails or stops

### LP Generation Calculation:
- [ ] Consume 10 blocks
- [ ] **Expected altar LP**: +500 (10 × 50)
- [ ] Verify exact amount added

---

## 2.8 Ritual of Culling (Cruor Vastatio) - Entity Killer

### ⚠️ **WARNING**: Extremely powerful and expensive ritual!

### Ritual Structure:
```
Runes Required: Fire and Dusk runes (large structure)
Layout: Complex - use Ritual Diviner!
```

### Initial Setup:
- [ ] Build large ritual structure
- [ ] Place Blood Altar within range (11×21×11)
- [ ] **Activation cost**: 50,000 LP (very expensive!)
- [ ] **Refresh cost**: 75 LP per entity
- [ ] **Refresh interval**: 25 ticks (fast!)

### Basic Entity Killing:
- [ ] Spawn test mobs in 21×21×21 range
- [ ] Activate ritual
- [ ] **Verify entities die**: Should kill rapidly
- [ ] **Verify altar fills**: +200 LP per kill
- [ ] Watch kill rate (every 25 ticks)

### Player Protection:
- [ ] Enter ritual range with > 4 health
- [ ] **Verify not killed**: Players with HP > 4 are safe
- [ ] Reduce player health to < 4
- [ ] **Expected**: Player may be vulnerable (test carefully!)

### Boss Killing:
- [ ] **Requires**: 100 demon will (destructive type)
- [ ] Generate 100 destructive will
- [ ] Spawn Wither
- [ ] **Verify kills boss** with sufficient will
- [ ] **Check extra LP cost** (configurable)

### Wither Special Case:
- [ ] Check config: `rituals.culling.killWither`
- [ ] If enabled: Spawn Wither
- [ ] **Verify kills**: Should work with will
- [ ] **Check cost**: `rituals.culling.witherCost`

### TNT Destruction:
- [ ] Check config: `rituals.culling.cullingKillsTnT`
- [ ] If enabled: Place primed TNT in range
- [ ] **Verify TNT destroyed**: Prevents explosion

### Demon Will Generation:
- [ ] Monitor destructive will generation
- [ ] **Chance**: 3% per kill
- [ ] Run ritual with many kills
- [ ] Verify will accumulates

### Potion Effect Check:
- [ ] Give entity resistance/protection effects
- [ ] Test if ritual respects/ignores effects
- [ ] Check debug logs for potion handling

### Range Testing:
- [ ] **Kill range**: 21×21×21 cube
- [ ] **Altar range**: 11×21×11
- [ ] Place entity at exactly 21 blocks
- [ ] Place entity at 22 blocks: Should not kill

### LP Economics:
- [ ] Cost per kill: 75 LP
- [ ] Gain per kill: 200 LP to altar
- [ ] Net gain: +125 LP per kill
- [ ] Test with many entities (profit check)

### Performance Test:
- [ ] Spawn 100+ entities
- [ ] Activate ritual
- [ ] Monitor TPS/lag
- [ ] Verify handles large numbers

### Debug Logging:
- [ ] Enable debug mode if available
- [ ] Check logs for:
  - Entity detection
  - Kill events
  - LP transfers
  - Will generation

---

# 3. BLOCKS TESTING

## 3.1 Blood Wood System

### Blood Wood Log:
- [ ] **Obtain**: `/give @s animus:blood_wood`
- [ ] Place in all orientations (X, Y, Z axis)
- [ ] Verify rotation with right-click placement
- [ ] **Test with axe**: Strip to get stripped log
- [ ] Verify sound/hardness matches wood

### Stripped Blood Wood:
- [ ] Strip normal blood wood with axe
- [ ] Verify texture change
- [ ] Place in all orientations
- [ ] Use in crafting recipes

### Blood Wood Planks:
- [ ] Craft from blood wood (1 log → 4 planks)
- [ ] Craft from stripped blood wood (1 log → 4 planks)
- [ ] Verify plank texture
- [ ] Use in crafting (sticks, fences, etc.)

### Crafting Integration:
- [ ] Craft planks from logs
- [ ] Craft sticks from planks
- [ ] Test in vanilla recipes (chests, doors, etc.)
- [ ] Verify fuel value (if burnable)

---

## 3.2 Blood Sapling & Tree Growth

### Sapling Planting:
- [ ] **Obtain sapling**: `/give @s animus:blood_sapling`
- [ ] **Valid surfaces**: Grass, dirt, coarse dirt
- [ ] **Invalid surfaces**: Stone, sand (should fail)
- [ ] Place sapling on grass
- [ ] Verify placement

### Natural Growth:
- [ ] Wait for natural growth (random tick)
- [ ] **Growth time**: Varies (like vanilla saplings)
- [ ] May need to wait several minutes
- [ ] Check for tree generation

### Bonemeal Growth:
- [ ] Use bonemeal on sapling
- [ ] **Should instantly grow** into tree
- [ ] Verify tree structure

### Tree Structure:
- [ ] **Height**: 4-6 blocks tall
- [ ] **Trunk**: Blood wood logs
- [ ] **Top**: Blood core replaces top log
- [ ] **Canopy**: Blood leaves in pattern
- [ ] Verify all parts present

### Space Requirements:
- [ ] **Minimum height**: 7 blocks clear above
- [ ] Test with low ceiling: Should fail
- [ ] Test with adequate space: Should succeed

### Multiple Saplings:
- [ ] Plant forest of saplings
- [ ] Verify each grows independently
- [ ] Check for interference between trees

---

## 3.3 Blood Leaves

### Appearance:
- [ ] Verify leaf texture/color
- [ ] Check transparency rendering
- [ ] Test with fancy/fast graphics

### Decay Mechanics:
- [ ] Break all logs from grown tree
- [ ] **Leaves should decay** naturally
- [ ] Time decay process

### Persistent State:
- [ ] Leaves from grown sapling: Persistent (don't decay)
- [ ] Player-placed leaves: May decay
- [ ] Verify with NBT data

### Drops:
- [ ] **Break without silk touch**: Should drop saplings (check drop rate)
- [ ] **Break with silk touch**: Should drop leaves block
- [ ] Test drop rates (may be configured)

### Shears:
- [ ] Break with shears
- [ ] **Should drop leaves block**
- [ ] Verify tool behavior

---

## 3.4 Blood Core

### Obtaining:
- [ ] Grow blood tree (sapling → tree)
- [ ] Blood core appears at top
- [ ] Break to collect
- [ ] Or: `/give @s animus:blood_core`

### Placement:
- [ ] Place blood core manually
- [ ] Verify texture shows inactive state

### Toggle Spreading:
- [ ] **Right-click blood core**
- [ ] **Verify state change**: Message confirms
- [ ] **Texture change**: ACTIVE state appears
- [ ] Right-click again: Toggles back to inactive

### Tree Spreading (Active):
- [ ] Activate blood core (right-click)
- [ ] **Requires corrosive demon will** in chunk/area
- [ ] Generate corrosive will (via rituals)
- [ ] Wait for spread timer (default 1200 ticks = 1 minute)
- [ ] **Verify new trees spawn**: Blood trees appear in radius

### Spread Configuration:
- [ ] Check config: `bloodcore.treeSpreadRadius`
- [ ] Adjust radius value
- [ ] Test new spread range
- [ ] **Default**: Configurable (check value)

### Leaf Regrowth:
- [ ] Break some blood leaves from tree
- [ ] Blood core should regrow missing leaves
- [ ] **Regrowth speed**: 100 ticks default (configurable)
- [ ] Verify leaves restored

### Demon Will Interaction:
- [ ] **Will type**: Corrosive
- [ ] **Consumption**: 5.0 per tree spawned
- [ ] **Timer modification**: More will = slower spread
- [ ] Test with varying will amounts

### Spread Location Requirements:
- [ ] New trees spawn on grass/dirt only
- [ ] Verify space requirements (7 blocks height)
- [ ] Test on stone: Should not spawn
- [ ] Test in crowded area: May not spawn

### NBT Persistence:
- [ ] Activate blood core
- [ ] Log out and back in
- [ ] **Verify state saved**: Still active
- [ ] Break and replace: State resets

---

## 3.5 Antimatter Block

### Creation (via Sigil):
- [ ] Use Sigil of Consumption on block
- [ ] **Verify antimatter created**
- [ ] Observe texture/appearance

### Spreading Behavior:
- [ ] Place target blocks in 3×3×3 area
- [ ] Create antimatter in center
- [ ] **Verify spreads to matching blocks**
- [ ] **Range**: 3×3×3 per tick
- [ ] **Only matching types**: Won't spread to different blocks

### LP Consumption During Spread:
- [ ] Monitor player LP
- [ ] **Cost**: Configured per spread
- [ ] Check config: `antimatterConsumption`
- [ ] Test with limited LP: Should stop spreading

### Tile Entity (Seeking Block):
- [ ] Antimatter stores "seeking block type"
- [ ] Should only spread to that block type
- [ ] Break antimatter: Check tile entity data
- [ ] Verify seeking block saved

### Decay State:
- [ ] **Break antimatter block**
- [ ] **Enters decay mode**
- [ ] **Decay spreads to adjacent antimatter**
- [ ] Watch chain reaction decay
- [ ] All connected antimatter should decay

### Particle Effects:
- [ ] Watch for particles during spread
- [ ] Check decay particles
- [ ] Verify visual feedback

### Sound Effects:
- [ ] Listen for spreading sound
- [ ] Listen for decay sound
- [ ] Verify audio cues present

### Performance:
- [ ] Create large antimatter spread (100+ blocks)
- [ ] Monitor TPS/FPS
- [ ] Test server performance
- [ ] Check for lag spikes

---

## 3.6 & 3.7 Fluid Testing (Antimatter & Dirt)

### Antimatter Fluid:

#### Bucket Interaction:
- [ ] **Obtain bucket**: `/give @s animus:antimatter_bucket`
- [ ] Place antimatter source
- [ ] Verify fluid appears
- [ ] Pick up with empty bucket

#### Flowing Behavior:
- [ ] Place source block
- [ ] **Verify flows like water**
- [ ] Check flow distance
- [ ] Test on slopes

#### Block Conversion:
- [ ] Place blocks adjacent to antimatter fluid
- [ ] **Verify conversion**: Blocks → antimatter blocks
- [ ] Test different block types

#### Life Essence Interaction:
- [ ] Get Blood Magic's life essence fluid
- [ ] Place near antimatter fluid
- [ ] **Verify special interaction**: Spreads or reacts
- [ ] Document behavior

#### Random Tick Spreading:
- [ ] Place antimatter fluid
- [ ] Wait for random ticks
- [ ] Observe spreading behavior
- [ ] May be slow (random tick dependent)

### Dirt Fluid:

#### Bucket Interaction:
- [ ] **Obtain bucket**: `/give @s animus:dirt_bucket`
- [ ] Place dirt fluid source
- [ ] Verify fluid appearance

#### Flowing Behavior:
- [ ] Place source and observe flow
- [ ] **Check flow rate**
- [ ] Test on various surfaces

#### Solidification (High Level):
- [ ] Create deep pool (> 6/8 full)
- [ ] **Verify solidifies**: Becomes dirt block
- [ ] Test level threshold (exactly 6/8)

#### Solidification (Touching Dirt):
- [ ] Place dirt block
- [ ] Place dirt fluid adjacent
- [ ] **Verify solidifies**: Fluid → dirt block
- [ ] Test all faces (top, bottom, sides)

#### Fluid Level Mechanics:
- [ ] Test different fill levels (1/8 to 8/8)
- [ ] Document solidification threshold
- [ ] Verify level display

#### Interaction with Other Fluids:
- [ ] Place water nearby: Document behavior
- [ ] Place lava nearby: Document behavior
- [ ] Test with Blood Magic fluids

---

# 4. ITEMS TESTING

## 4.1 Blood Apple

### Basic Consumption:
- [ ] **Obtain**: `/give @s animus:blood_apple`
- [ ] Check hunger/saturation stats
- [ ] **Food value**: 3 hunger, 0.3 saturation
- [ ] **Always edible**: Even when full

### Eating Effects:
- [ ] Eat blood apple
- [ ] **Nausea check**: 75% chance
- [ ] **Duration**: 40 ticks (2 seconds)
- [ ] Eat multiple apples: Test probability

### Altar Detection:
- [ ] Build Blood Altar within range
- [ ] **Detection range**: 11×21×11
- [ ] Place altar in range
- [ ] Eat apple near altar

### LP to Altar:
- [ ] Note altar LP before eating
- [ ] Eat apple near altar
- [ ] **Expected**: LP added to altar (2× configured amount)
- [ ] Check config: `bloodPerApple`
- [ ] Verify doubling (altar gets 2× base)

### LP to Soul Network (No Altar):
- [ ] Eat apple away from altar
- [ ] **Expected**: LP added to player's soul network
- [ ] Check network LP before/after
- [ ] **Amount**: 1× configured value (not doubled)

### Config Testing:
- [ ] Check config: `bloodPerApple`
- [ ] Adjust value
- [ ] Reload and test
- [ ] Verify new amount applies

### Edge Cases:
- [ ] Multiple altars in range: Which gets LP?
- [ ] Eat while unbound: Does it work?
- [ ] Eat in different dimensions

---

## 4.2 Fragment of Healing

### Basic Properties:
- [ ] **Obtain**: `/give @s animus:fragment_healing`
- [ ] Check stack size: Max 1
- [ ] Verify tooltip/lore

### Drop Prevention:
- [ ] **Survival mode**: Try to drop (Q key)
- [ ] **Expected**: Cannot drop
- [ ] **Creative mode**: Try to drop
- [ ] **Expected**: CAN drop in creative

### Block Breaking Prevention:
- [ ] Hold fragment
- [ ] Try to break blocks
- [ ] **Expected**: Cannot break blocks
- [ ] Switch to other item: Can break blocks again

### Crafting Component:
- [ ] Check recipes using fragment
- [ ] Test crafting (if recipes exist)
- [ ] Verify fragment consumed properly

### Quest/Reward Use:
- [ ] Document intended use (quest system?)
- [ ] Test with quest mods if applicable

---

## 4.3 Altar Diviner

### Basic Usage:
- [ ] **Obtain**: `/give @s animus:altar_diviner`
- [ ] Build Blood Altar (any tier)
- [ ] Right-click altar with diviner

### Information Display:
- [ ] **Verify displays**:
  - Current blood (LP)
  - Max capacity
  - Altar tier
- [ ] Check chat message format
- [ ] Verify values accurate

### Tier Recalculation:
- [ ] Add runes to upgrade altar tier
- [ ] Use diviner
- [ ] **Verify tier updates**: Should recalculate
- [ ] Remove runes
- [ ] Use diviner: Tier should downgrade

### Sound Effect:
- [ ] Listen for sound on use
- [ ] Verify audio feedback present

### Non-Altar Usage:
- [ ] Right-click on random block
- [ ] **Expected**: Nothing happens or error message

### Different Altar Tiers:
- [ ] Test on Tier 1 altar
- [ ] Test on Tier 2 altar
- [ ] Test on Tier 3+ altars
- [ ] Verify displays correctly for each

---

## 4.4 Key of Binding

### Obtaining:
- [ ] **Get key**: `/give @s animus:key_binding`
- [ ] Check stack size: Max 1
- [ ] Verify appearance

### Binding Mechanic:
- [ ] Bind key to player (Blood Magic binding)
- [ ] Right-click to bind (if applicable)
- [ ] **Verify bound**: Tooltip shows owner

### Tooltip Display:
- [ ] **Bound key**: Shows owner name
- [ ] **Unbound key**: Shows unbound state
- [ ] Trade to another player: Verify still shows original owner

### Crafting Component:
- [ ] Check sigil recipes
- [ ] **Verify required** for sigil crafting
- [ ] Craft sigil with bound key
- [ ] **Expected**: Sigil inherits binding

### Unbinding:
- [ ] Test if unbinding possible
- [ ] Check Blood Magic unbinding mechanics
- [ ] Document process

### Multiple Keys:
- [ ] Can player have multiple keys?
- [ ] Different bindings per key?
- [ ] Test key uniqueness

---

## 4.5 Mob Soul

### Creation:
- [ ] Created via Sigil of Chains (tested above)
- [ ] Verify item appearance
- [ ] Check tooltip

### Entity Data Display:
- [ ] **Tooltip shows**:
  - Entity type (e.g., "Zombie")
  - Custom name (if any)
  - Additional NBT data
- [ ] Hover over for details

### NBT Inspection:
- [ ] Use NBT viewer (F3+H advanced tooltips)
- [ ] **Verify stores**:
  - Entity ID
  - Position data
  - Custom name
  - Health, equipment
  - All entity NBT
- [ ] Document stored data

### Stack Behavior:
- [ ] **Stack size**: 1 (unstackable)
- [ ] Different mob types: Don't stack
- [ ] Same mob type: Still don't stack (unique data)

### Future Use:
- [ ] Check for release/spawn mechanic
- [ ] Right-click to spawn entity?
- [ ] Used in rituals/crafting?
- [ ] Document intended purpose

---

# 5. INTEGRATION & INTERACTION TESTING

## 5.1 Blood Magic Integration

### LP System:
- [ ] All sigils consume LP correctly
- [ ] Rituals consume LP as specified
- [ ] LP transfers to altars work
- [ ] Soul network integration works

### Altar Compatibility:
- [ ] All altar-related features detect altars
- [ ] Tier requirements respected
- [ ] LP limits and capacities work
- [ ] Altar upgrades recognized

### Binding System:
- [ ] Sigils bind properly
- [ ] Keys bind properly
- [ ] Bound items work only for owner
- [ ] Unbinding works if supported

### Demon Will:
- [ ] Rituals generate correct will types
- [ ] Will consumption works
- [ ] Will affects timers/behavior
- [ ] Will types: Corrosive, Destructive, Steadfast

### Ritual System:
- [ ] Master Ritual Stones work
- [ ] Ritual runes recognized
- [ ] Activation crystals work
- [ ] Ritual persistence (logout/login)

---

## 5.2 Cross-Feature Interactions

### Sigil of Consumption + Antimatter:
- [ ] Create antimatter with sigil
- [ ] Watch spreading behavior
- [ ] Test LP drain from both sources
- [ ] Verify seeking block stored

### Blood Core + Ritual of Nature's Leech:
- [ ] Activate blood core spreading
- [ ] Run Nature's Leech ritual
- [ ] **Verify**: Ritual consumes new trees
- [ ] **Verify**: LP generated from spread trees

### Bound Pilum + Ritual of Culling:
- [ ] Run Culling ritual
- [ ] Kill entities with Bound Pilum melee
- [ ] **Both fill altar**: LP from both sources
- [ ] Compare LP rates

### Blood Apple + Multiple Altars:
- [ ] Place 2+ altars in range
- [ ] Eat blood apple
- [ ] **Document**: Which altar gets LP?
- [ ] Test priority/selection

### Antimatter Spread + Transposition:
- [ ] Create spreading antimatter
- [ ] Use Transposition sigil to move antimatter
- [ ] **Verify**: Spread continues at new location
- [ ] **Verify**: Seeks new adjacent blocks

---

## 5.3 Multi-Ritual Testing

### Two Rituals Same Time:
- [ ] Activate Luna and Sol simultaneously
- [ ] Verify both function
- [ ] Check for conflicts/interference

### Resource Competition:
- [ ] Run multiple LP-consuming rituals
- [ ] Verify LP drain from all
- [ ] Test with limited LP
- [ ] Which ritual gets priority?

### Overlapping Ranges:
- [ ] Place two rituals with overlapping effect ranges
- [ ] **Nature's Leech + Culling**: Both affect same area
- [ ] Verify both function correctly

### Performance:
- [ ] Run 5+ rituals simultaneously
- [ ] Monitor TPS/lag
- [ ] Check for performance issues
- [ ] Test on server

---

# 6. CONFIGURATION TESTING

## Config File Location:
```
config/animus-common.toml
```

### All Config Options:
- [ ] `bloodPerApple` - Blood apple LP amount
- [ ] `antimatterConsumption` - LP per antimatter spread
- [ ] `bloodcore.treeSpreadRadius` - Blood core spread radius
- [ ] `bloodcore.treeSpreadInterval` - Spread timer
- [ ] `bloodcore.leafRegrowSpeed` - Leaf regrowth rate
- [ ] `builder.builderRange` - Builder sigil range
- [ ] `rituals.culling.killWither` - Can kill Wither
- [ ] `rituals.culling.witherCost` - Extra LP for Wither
- [ ] `rituals.culling.cullingKillsTnT` - TNT destruction

### Config Change Testing:
- [ ] Modify each config value
- [ ] Reload world/server
- [ ] **Verify changes apply**
- [ ] Test affected features
- [ ] Reset to defaults

### Invalid Values:
- [ ] Test negative values
- [ ] Test extremely large values
- [ ] Test zero values
- [ ] Verify safe handling

---

# 7. PERFORMANCE & STABILITY

## Performance Tests:

### Antimatter Large Scale:
- [ ] Create 1000+ block antimatter spread
- [ ] Monitor FPS/TPS
- [ ] Check memory usage
- [ ] Time full spread completion

### Ritual Spam:
- [ ] Activate 10+ rituals
- [ ] Run for 30 minutes
- [ ] Monitor performance
- [ ] Check for memory leaks

### Blood Core Forest:
- [ ] Activate many blood cores
- [ ] Let spread for extended period
- [ ] Check tree generation performance
- [ ] Monitor entity count

### Entity Culling Stress:
- [ ] Spawn 500+ entities
- [ ] Run Culling ritual
- [ ] Monitor kill rate
- [ ] Check for lag spikes

## Stability Tests:

### Save/Load:
- [ ] Activate features
- [ ] Save and quit
- [ ] Reload world
- [ ] **Verify all states preserved**

### Chunk Loading:
- [ ] Activate ritual
- [ ] Leave area (unload chunks)
- [ ] Return (reload chunks)
- [ ] Verify ritual still active

### Dimension Travel:
- [ ] Test features in Overworld
- [ ] Test in Nether
- [ ] Test in End
- [ ] Test dimension transitions

### Server Testing:
- [ ] Test on dedicated server
- [ ] Multiple players using features
- [ ] Client-server sync
- [ ] Packet handling

---

# 8. ERROR HANDLING & EDGE CASES

## Insufficient Resources:

### Out of LP:
- [ ] Every feature with 0 LP
- [ ] Verify error messages
- [ ] Verify safe failure

### Out of Items:
- [ ] Builder with no blocks
- [ ] Sol ritual with no lights
- [ ] Entropy with empty chest
- [ ] Verify graceful handling

## Invalid Targets:

### Sigils on Invalid Blocks:
- [ ] Consumption on bedrock
- [ ] Transposition on air
- [ ] Storm on void
- [ ] Verify error messages

### Rituals Without Requirements:
- [ ] Nature's Leech without altar
- [ ] Culling without altar
- [ ] Sol without chest
- [ ] Verify safe failure

## Unbound Items:
- [ ] Use unbound sigils
- [ ] Use unbound keys
- [ ] Verify error messages
- [ ] Verify no crashes

## Chunk Boundaries:
- [ ] Ritual spanning chunks
- [ ] Antimatter spread across chunks
- [ ] Blood core spread across chunks
- [ ] Verify proper handling

## World Corruption:
- [ ] Break tile entity while active
- [ ] Remove altar mid-ritual
- [ ] Interrupt antimatter spread
- [ ] Verify recovery

---

# 9. COMPATIBILITY TESTING

## Other Mods:

### Common Mod Interactions:
- [ ] JEI: Recipe viewing
- [ ] The One Probe: Block info
- [ ] Waila/Hwyla: Block tooltips
- [ ] Create: Mechanical interactions
- [ ] Botania: Cross-mod features

### Dimension Mods:
- [ ] Test in Twilight Forest
- [ ] Test in Aether
- [ ] Test in custom dimensions

### Storage Mods:
- [ ] Test with Applied Energistics
- [ ] Test with Refined Storage
- [ ] Test ritual chest interactions

---

# 10. DOCUMENTATION VERIFICATION

## Recipe Checking:
- [ ] All items craftable
- [ ] Recipes show in JEI
- [ ] Ritual structures documented
- [ ] Verify recipe balance

## Localization:
- [ ] All items have names
- [ ] All tooltips present
- [ ] Messages properly formatted
- [ ] Check en_us.json completeness

## Textures & Models:
- [ ] All items have textures
- [ ] All blocks have textures
- [ ] Models load correctly
- [ ] No missing textures (purple/black)

---

# TESTING COMPLETION CHECKLIST

## Major Categories:
- [ ] ✅ Pilums (all 3 variants) - See `PILUM_TESTING_PLAN.md`
- [ ] All 6 Sigils
- [ ] All 8 Rituals
- [ ] All Blocks (wood, sapling, core, antimatter)
- [ ] All Items (apple, fragment, diviner, key, mob soul)
- [ ] Both Fluids
- [ ] Blood Magic Integration
- [ ] Cross-Feature Interactions
- [ ] Configuration
- [ ] Performance & Stability
- [ ] Error Handling
- [ ] Compatibility

## Test Results Summary:
```
Total Features Tested: ___
Features Passed: ___
Features Failed: ___
Bugs Found: ___
Performance Issues: ___
```

---

## BUG REPORT TEMPLATE

When you find bugs, document as follows:

**Feature**: [Sigil/Ritual/Block/Item name]
**Bug**: [Brief description]
**Severity**: Critical / Major / Minor
**Steps to Reproduce**:
1.
2.
3.

**Expected**: [What should happen]
**Actual**: [What actually happens]
**LP Cost**: [If relevant]
**Config**: [Default or modified?]
**Other Mods**: [List if relevant]
**Crash Log**: [Paste or attach if crashed]
**Screenshots**: [Attach if helpful]

---

**Happy Testing!** 🧪🔬

Remember to test systematically and document everything. This is a complex mod with many interconnected features - thorough testing will ensure a quality experience for users!
