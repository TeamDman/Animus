package com.teamdman.animus.rituals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.teamdman.animus.Animus;
import com.teamdman.animus.registry.AnimusPotions;

import WayofTime.bloodmagic.api.ritual.AreaDescriptor;
import WayofTime.bloodmagic.api.ritual.EnumRuneType;
import WayofTime.bloodmagic.api.ritual.IMasterRitualStone;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.api.ritual.RitualComponent;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.soul.EnumDemonWillType;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class RitualVengefulSpirit extends Ritual {

	public static final String EFFECT_RANGE = "effect";
	public final int maxWill = 100;
	public double willBuffer = 0;
	public Random rand = new Random();
	
	public RitualVengefulSpirit() {
		
		super("RitualVengefulSpirit", 0, 20000, "ritual." + Animus.MODID + ".vengefulspirit");	
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-16, -16, -16), 32));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);

	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		if (network == null) {
			return;
		}

		
		World world = masterRitualStone.getWorldObj();
		
		EnumDemonWillType type = EnumDemonWillType.VENGEFUL;
		BlockPos pos = masterRitualStone.getBlockPos();
		double currentAmount = WorldDemonWillHandler.getCurrentWill(world, pos, type);


		AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
		AxisAlignedBB range = damageRange.getAABB(pos);

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, range);
		
		int entityCount = 0;
		//Mob drops we care about: Bones, Flesh, Spider Eyes, Skulls, 
		
		

			for (EntityLivingBase livingEntity : list) {
				if (!(livingEntity instanceof EntityPlayer) || livingEntity instanceof FakePlayer)
					continue;
				PotionEffect vPotion = ((EntityLivingBase) livingEntity).getActivePotionEffect(AnimusPotions.VENGEFULSPIRITS);
				
				if (vPotion == null)
				((EntityLivingBase) livingEntity).addPotionEffect(new PotionEffect(AnimusPotions.VENGEFULSPIRITS, 800));
<<<<<<< HEAD
				else{
					int pow = Math.min(5*((vPotion.getDuration()+60)/36000),4);
					((EntityLivingBase) livingEntity).addPotionEffect(new PotionEffect(AnimusPotions.VENGEFULSPIRITS, Math.min(((vPotion.getDuration() + 800)*2), 36000), pow, true, false));
				}
=======
				else
					((EntityLivingBase) livingEntity).addPotionEffect(new PotionEffect(AnimusPotions.VENGEFULSPIRITS, Math.min(((vPotion.getDuration() + 800)*2), 36000), Math.min(1+((5*vPotion.getDuration()+60)/36000),4), true, false));
				
>>>>>>> origin/master
				entityCount++;

			}
			network.syphon(getRefreshCost() * entityCount);
			double drainAmount = 2*Math.min((maxWill - currentAmount)+1, Math.min(entityCount/2, 10));

			
				double filled = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, drainAmount, maxWill, false);
				if (filled > 0)
					WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);
			
		

	}

	@Override
	public int getRefreshCost() {
		return 100;
	}
	
	@Override
    public int getRefreshTime()
    {
        return 600;
    }

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> ritualBlocks = new ArrayList<RitualComponent>();
		this.addRune(ritualBlocks, 1, 0, 1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, -1, 0, 1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, 1, 0, -1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, -1, 0, -1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, 0, -1, 0, EnumRuneType.AIR);
		this.addRune(ritualBlocks, 2, -1, 2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, 2, -1, -2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, -2, -1, 2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, -2, -1, -2, EnumRuneType.EARTH);
		return ritualBlocks;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		willBuffer = tag.getDouble("willBuffer");

	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setDouble("willBuffer", willBuffer);
	}

	
	@Override
	public Ritual getNewCopy() {
		return new RitualVengefulSpirit();
	}
	
	
}
