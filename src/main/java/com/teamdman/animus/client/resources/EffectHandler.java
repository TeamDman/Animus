package com.teamdman.animus.client.resources;


import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import com.teamdman.animus.client.resources.fx.EntityComplexFX;
import com.teamdman.animus.client.resources.fx.EntityFXFacingParticle;
import com.teamdman.animus.client.resources.fx.IComplexEffect;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class EffectHandler {
    public static final Random STATIC_EFFECT_RAND = new Random();

    private static int clientEffectTick = 0;

    public static final EffectHandler instance = new EffectHandler();

    public static final Map<IComplexEffect.RenderTarget, Map<Integer, List<IComplexEffect>>> complexEffects = new HashMap<>();
    public static final List<EntityFXFacingParticle> fastRenderParticles = new LinkedList<>();

    private EffectHandler() {}

    public static EffectHandler getInstance() {
        return instance;
    }

    public static int getDebugEffectCount() {
        int amt = 0;
        for (Map<Integer, List<IComplexEffect>> effects : complexEffects.values()) {
            for (List<IComplexEffect> eff : effects.values()) {
                amt += eff.size();
            }
        }
        amt += fastRenderParticles.size();
        return amt;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            synchronized (complexEffects) {
                Map<Integer, List<IComplexEffect>> layeredEffects = complexEffects.get(IComplexEffect.RenderTarget.OVERLAY_TEXT);
                for (int i = 0; i <= 2; i++) {
                    for (IComplexEffect effect : layeredEffects.get(i)) {
                        GL11.glPushMatrix();
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                        effect.render(event.getPartialTicks());
                        GL11.glPopAttrib();
                        GL11.glPopMatrix();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDebugText(RenderGameOverlayEvent.Text event) {
        if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            event.getLeft().add("");
            event.getLeft().add("[AstralSorcery] EffectHandler:");
            event.getLeft().add("[AstralSorcery] > Complex effects: " + getDebugEffectCount());
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        synchronized (complexEffects) {
            Map<Integer, List<IComplexEffect>> layeredEffects = complexEffects.get(IComplexEffect.RenderTarget.RENDERLOOP);
            EntityFXFacingParticle.renderFast(event.getPartialTicks(), fastRenderParticles);
            for (int i = 0; i <= 2; i++) {
                for (IComplexEffect effect : layeredEffects.get(i)) {
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    effect.render(event.getPartialTicks());
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tick();
    }

    public EntityComplexFX registerFX(EntityComplexFX entityComplexFX) {
        register(entityComplexFX);
        return entityComplexFX;
    }


    private void register(final IComplexEffect effect) {
        if(Assets.reloading || Minecraft.getMinecraft().isGamePaused()) return;

        new Thread(() -> {
            synchronized (complexEffects) {
                if(effect instanceof EntityFXFacingParticle) {
                    fastRenderParticles.add((EntityFXFacingParticle) effect);
                } else {
                    complexEffects.get(effect.getRenderTarget()).get(effect.getLayer()).add(effect);
                }
                effect.clearRemoveFlag();
            }
        }).start();
    }



    public void tick() {
        clientEffectTick++;

        synchronized (complexEffects) {
            for (IComplexEffect.RenderTarget target : complexEffects.keySet()) {
                Map<Integer, List<IComplexEffect>> layeredEffects = complexEffects.get(target);
                for (int i = 0; i <= 2; i++) {
                    Iterator<IComplexEffect> iterator = layeredEffects.get(i).iterator();
                    while (iterator.hasNext()) {
                        IComplexEffect effect = iterator.next();
                        effect.tick();
                        if(effect.canRemove()) {
                            effect.flagAsRemoved();
                            iterator.remove();
                        }
                    }
                }
            }
            Iterator<EntityFXFacingParticle> iterator = fastRenderParticles.iterator();
            while (iterator.hasNext()) {
                EntityFXFacingParticle effect = iterator.next();
                effect.tick();
                if(effect.canRemove()) {
                    effect.flagAsRemoved();
                    iterator.remove();
                }
            }
        }
    }

    public static int getClientEffectTick() {
        return clientEffectTick;
    }

    static {
        for (IComplexEffect.RenderTarget target : IComplexEffect.RenderTarget.values()) {
            Map<Integer, List<IComplexEffect>> layeredEffects = new HashMap<>();
            for (int i = 0; i <= 2; i++) {
                layeredEffects.put(i, new LinkedList<>());
            }
            complexEffects.put(target, layeredEffects);
        }
    }

    public static void cleanUp() {
        synchronized (complexEffects) {
            for (IComplexEffect.RenderTarget t : IComplexEffect.RenderTarget.values()) {
                for (int i = 0; i <= 2; i++) {
                    complexEffects.get(t).get(i).clear();
                }
            }
            fastRenderParticles.clear();
        }
    }
}
