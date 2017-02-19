package com.teamdman.animus.client.resources.fx;

public abstract class EntityComplexFX implements IComplexEffect {
	protected int age = 0;
	protected int maxAge = 40;

	private boolean flagRemoved = true;

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	@Override
	public boolean canRemove() {
		return age >= maxAge;
	}

	@Override
	public RenderTarget getRenderTarget() {
		return RenderTarget.RENDERLOOP;
	}

	@Override
	public void tick() {
		age++;
	}

	public boolean isRemoved() {
		return flagRemoved;
	}

	public void flagAsRemoved() {
		flagRemoved = true;
	}

	public void clearRemoveFlag() {
		flagRemoved = false;
	}

}
