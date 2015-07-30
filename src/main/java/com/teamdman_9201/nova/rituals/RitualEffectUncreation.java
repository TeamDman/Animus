package com.teamdman_9201.nova.rituals;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import WayofTime.alchemicalWizardry.api.rituals.IMasterRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.RitualComponent;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectUncreation extends RitualEffect {
    private int upkeep = NOVA.ritualData.get("upkeepUncreate");

    public int getCostPerRefresh() {
        return upkeep;
    }

    @Override
    public List<RitualComponent> getRitualComponentList() {
        ArrayList<RitualComponent> ritualBlocks = new ArrayList();
        ritualBlocks.add(new RitualComponent(-4, 0, -2, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-4, 0, 0, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-4, 0, 2, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-3, 0, -3, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-3, 0, -1, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-3, 0, 1, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-3, 0, 3, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 0, -4, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-2, 0, -2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 0, 0, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-2, 0, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 0, 4, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-1, 0, -3, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-1, 0, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 0, 0, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-1, 0, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 0, 3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(0, 0, -4, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(0, 0, -2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(0, 0, -1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(0, 0, 1, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(0, 0, 2, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(0, 0, 4, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(1, 0, -3, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(1, 0, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 0, 0, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(1, 0, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 0, 3, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(2, 0, -4, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(2, 0, -2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 0, 0, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(2, 0, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 0, 4, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(3, 0, -3, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(3, 0, -1, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(3, 0, 1, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(3, 0, 3, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(4, 0, -2, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(4, 0, 0, RitualComponent.WATER));
        ritualBlocks.add(new RitualComponent(4, 0, 2, RitualComponent.WATER));
        return ritualBlocks;
    }

    @Override
    public void performEffect(IMasterRitualStone ritualStone) {
        String owner          = ritualStone.getOwner();
        World  world          = ritualStone.getWorld();
        int    x              = ritualStone.getXCoord();
        int    y              = ritualStone.getYCoord();
        int    z              = ritualStone.getZCoord();
        int    currentEssence = SoulNetworkHandler.getCurrentEssence(owner);
        if (currentEssence < this.getCostPerRefresh()) {
            EntityPlayer entityOwner = SpellHelper.getPlayerForUsername(owner);
            if (entityOwner == null) {
                return;
            }
            SoulNetworkHandler.causeNauseaToPlayer(owner);
        } else {

            int d0 = 0;
            AxisAlignedBB region = AxisAlignedBB.getBoundingBox((double) x, (double) y + 1, (double) z, (double) (x + 1), (double) (y + 2), (double) (z + 1)).expand(d0, d0, d0);
            List list = world.getEntitiesWithinAABB(EntityItem.class, region);
            ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
            EntityItem books = null;
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                EntityItem ent = (EntityItem) iter.next();
                if (ent.getEntityItem().getItem() == Items.book) {
                    if (books == null) {
                        books = ent;
                    } else {
                        books.getEntityItem().stackSize += ent.getEntityItem().stackSize;
                        world.removeEntity(ent);
                    }
                }
            }
            iter = list.iterator();
            while (iter.hasNext() && books != null && books.getEntityItem() != null && books.getEntityItem().stackSize > 0) {
                ItemStack dropped = ((EntityItem) iter.next()).getEntityItem();
                if (dropped == null) {
                    continue;
                }
                if (dropped.getItem() == Items.enchanted_book) {
                    NBTTagList enchants = dropped.stackTagCompound.getTagList("StoredEnchantments", 10);
                    if (enchants != null) {
                        for (int i = enchants.tagCount() - 1; i >= 0; --i) {
                            if (books.getEntityItem().stackSize == 0)
                                break;
                            ItemStack newItem = new ItemStack(Items.enchanted_book);
                            NBTTagCompound data = enchants.getCompoundTagAt(i);
                            short enchID = data.getShort("id");
                            double enchLVL = ((double)data.getShort("lvl")) / 2;
                            System.out.println(enchLVL);
                            enchants.removeTag(i);

                            newItem.setTagCompound(new NBTTagCompound());
                            NBTTagList bookTags = new NBTTagList();
                            NBTTagCompound comp = new NBTTagCompound();
                            comp.setShort("id", enchID);
                            comp.setShort("lvl", (short) Math.floor(enchLVL));
                            bookTags.appendTag(comp);
                            newItem.stackTagCompound.setTag("StoredEnchantments", bookTags);
                            drops.add(newItem.copy());

                            newItem = new ItemStack(Items.enchanted_book);
                            newItem.setTagCompound(new NBTTagCompound());
                            bookTags = new NBTTagList();
                            comp = new NBTTagCompound();
                            comp.setShort("id", enchID);
                            comp.setShort("lvl", (short) Math.ceil(enchLVL));
                            bookTags.appendTag(comp);
                            newItem.stackTagCompound.setTag("StoredEnchantments", bookTags);
                            drops.add(newItem);

                            books.getEntityItem().stackSize--;
                        }
                        dropped.stackSize = 0;
                    }

                } else {
                    NBTTagList enchants = dropped.getEnchantmentTagList();
                    if (enchants != null) {
                        for (int i = enchants.tagCount() - 1; i >= 0; --i) {
                            if (books.getEntityItem().stackSize == 0)
                                break;
                            ItemStack newItem = new ItemStack(Items.enchanted_book);
                            NBTTagCompound data = enchants.getCompoundTagAt(i);
                            short enchID = data.getShort("id");
                            short enchLVL = data.getShort("lvl");
                            enchants.removeTag(i);
                            newItem.setTagCompound(new NBTTagCompound());
                            NBTTagList bookTags = new NBTTagList();
                            newItem.stackTagCompound.setTag("StoredEnchantments", bookTags);
                            NBTTagCompound nbttagcompound = new NBTTagCompound();
                            nbttagcompound.setShort("id", enchID);
                            nbttagcompound.setShort("lvl", enchLVL);
                            bookTags.appendTag(nbttagcompound);
                            drops.add(newItem);
                            books.getEntityItem().stackSize--;
                        }
                        if (dropped.getEnchantmentTagList().tagCount() == 0)
                            dropped.stackTagCompound.removeTag("ench");
                    }
                }
            }
            Iterator dropIter = drops.iterator();
            while (dropIter.hasNext()) {
                EntityItem dropEntity = new EntityItem(world, x, y + 1, z, ((ItemStack) dropIter.next()).copy());
                world.spawnEntityInWorld(dropEntity);
            }
            if (!list.isEmpty())
                ritualStone.setActive(false);
        }
        if (world.rand.nextInt(10) == 0) {
            SpellHelper.sendIndexedParticleToAllAround(world, x, y, z, 20, world.provider.dimensionId, 1, x, y, z);
        }
    }
}

