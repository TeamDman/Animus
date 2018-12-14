# Animus [![](http://cf.way2muchnoise.eu/full_animus_downloads.svg)](https://minecraft.curseforge.com/projects/animus)
This is an addon for the mod [Blood Magic](https://github.com/WayofTime/BloodMagic/)

A feature overview can be seen on the [curseforge link](https://minecraft.curseforge.com/projects/animus)

Here's an example CraftTweaker script for modifying altar components
```zenscript
import crafttweaker.block.IBlockState;
import mods.animus.AnimusPlugin;

print("-------------------------modify altar ----------------------------");
AnimusPlugin.removeComponentMapping(<blockstate:minecraft:glowstone>,"GLOWSTONE");
AnimusPlugin.addComponentMapping(<blockstate:minecraft:dirt>, "GLOWSTONE");
```

Excerpt from BloodMagic API:
```
Valid component types:
GLOWSTONE
BLOODSTONE
BEACON
BLOODRUNE
CRYSTAL
NOTAIR
```