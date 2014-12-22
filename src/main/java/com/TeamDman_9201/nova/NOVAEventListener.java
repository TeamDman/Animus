package com.TeamDman_9201.nova;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class NOVAEventListener {

  @SubscribeEvent
  public void SomethingCrafted(ItemCraftedEvent event) {
  }
}

//
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.inventory.IInventory;
// import net.minecraft.item.ItemStack;
// import net.minecraftforge.event.entity.player.PlayerEvent;
// //import cpw.mods.fml.common.gameevent.PlayerEvent;
//
// public class NOVAEventListener extends PlayerEvent {
//
//
// public NOVAEventListener(EntityPlayer player) {
// super(player);
// // TODO Auto-generated constructor stub
// }
//
// public void ItemCraftedEvent(EntityPlayer player, ItemStack crafting,
// IInventory craftMatrix) {
// System.out.println("Event Fired");
//
// }
// // ItemCraftedEvent
// }
