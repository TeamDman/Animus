package com.teamdman.animus.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityVengefulSpirit extends EntityMob {

    public SoundEvent ambientSound;
    public SoundEvent deathSound;
    private static final DataParameter<Boolean> SCREAMING = EntityDataManager.<Boolean>createKey(EntityVengefulSpirit.class, DataSerializers.BOOLEAN);
    private int lastSound = 0;
    
    
	public EntityVengefulSpirit(World worldIn) {
		super(worldIn);
    	this.noClip = false;
        
        
        this.isAirBorne = true;
		this.experienceValue = 0;

		isImmuneToFire = true;
		deathSound = new SoundEvent(new ResourceLocation("animus:ghostly"));
		ambientSound = new SoundEvent(new ResourceLocation("animus:vengefulspiritambient"));
		
		
	}
	
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        
    }
	
    @Override
    protected void entityInit(){
    	super.entityInit();
          }
    
    public void scream()
    {
            if (!this.isSilent())
            {
                this.world.playSound(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, ambientSound, this.getSoundCategory(), 2.5F, 1.0F, false);
                lastSound = this.ticksExisted;
            }
    }

    @Override
    public void updateAITasks(){
    	super.updateAITasks();
    }
    
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);
    }

    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_GUARDIAN_AMBIENT_LAND;
    }
    
    public boolean isPlaying()
    {
        return ((Boolean)this.dataManager.get(SCREAMING)).booleanValue();
    }
    
    @Override
    public void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source){
    	//these abberations do not drop loot
    }
    
    @Override
    public void onUpdate(){
    	super.onUpdate();
/*    	if (this.getAttackTarget() == null)
    		this.setDead();

    	if (this.getAttackTarget().isDead)
    		this.setDead();
    	
    	if (this.ticksExisted > 300 )
    		this.setDead();*/
    }
    
    @Override
    public int getBrightnessForRender(float partialTicks){
    		return 16;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {//Spirits are short lived and immortal and do not care about other attackers attacking them
     //They are soley focused on their vengeance target
		return false;
    	
    }
    
    protected SoundEvent getDeathSound()
    {
        return deathSound;
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote)
        {
        	if (lastSound == 0)
        		this.scream();
        	
            for (int i = 0; i < 2; ++i)
            {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width,
                        this.posY + .5D + this.rand.nextDouble() * (double) this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width,
                        (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D, new int[0]);
            }
        }
    	super.onLivingUpdate();
    }

    
}
