package com.teamdman.animus.client.resources.fx;

public interface IComplexEffect {

	public boolean canRemove();

	public boolean isRemoved();

	public void flagAsRemoved();

	public void clearRemoveFlag();

	public RenderTarget getRenderTarget();

	public void render(float pTicks);

	public void tick();

	//Valid layers: 0, 1, 2
	//Lower layers are rendered first.
	default public int getLayer() {
		return 0;
	}

	public static enum RenderTarget {

		OVERLAY_TEXT,
		RENDERLOOP

	}

}
