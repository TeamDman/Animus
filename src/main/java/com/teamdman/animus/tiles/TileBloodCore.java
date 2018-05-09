package com.teamdman.animus.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileBloodCore extends TileEntity implements ITickable  {

    private int delayCounter = 0; 
        private boolean spreading = false;
	
    public boolean isSpreading() {
        return spreading;
    }
    
	@Override
	public void update() {
        updateCounter();
		
	}

	private void updateCounter() {

        delayCounter--;
        if (delayCounter <= 0) {
        	
            world.markBlockRangeForRenderUpdate(getPos(), getPos());
            //TODO: Autoplant/grow trees on update within range
            //creative corrosive will
            //slow timer by a ratio of corrosive will in the chunk
            delayCounter = 1200; //attempt to grow a new tree once per minute
        }
		
	}

}
