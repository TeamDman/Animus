package com.teamdman.animus.client.resources.fx;

import com.teamdman.animus.client.resources.SpriteLibrary;
import com.teamdman.animus.client.resources.SpriteSheetResource;

public class EntityFXBurst extends EntityFXFacingSprite {

    public EntityFXBurst(int burstId, double x, double y, double z) {
        super(getSprite(burstId), x, y, z);
    }

    public EntityFXBurst(int burstId, double x, double y, double z, float scale) {
        super(getSprite(burstId), x, y, z, scale);
    }

    private static SpriteSheetResource getSprite(int burstId) {
        switch (burstId) {
            case 0:
                return SpriteLibrary.spriteCulling;
            case 1:
            	return SpriteLibrary.spriteNaturesLeech;
        }
        return SpriteLibrary.spriteCulling;
    }
}
