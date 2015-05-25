package com.TeamDman.nova.Tiles;

import com.TeamDman.nova.Blocks.BlockBrickFurnace;
import com.TeamDman.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileBrickFurnace extends TileEntity implements ISidedInventory {

  private static final int[] slotsTop = new int[]{0};
  private static final int[] slotsBottom = new int[]{2, 1};
  private static final int[] slotsSides = new int[]{1};

  /**
   * The ItemStacks that hold the items currently being used in the furnace
   */
  private ItemStack[] furnaceItemStacks = new ItemStack[3];
  /**
   * The number of ticks that the furnace will keep burning
   */
  public int furnaceBurnTime;
  /**
   * The number of ticks that a fresh copy of the currently-burning item would keep the furnace
   * burning for
   */
  public int currentItemBurnTime;
  /**
   * The number of ticks that the current item has been cooking for
   */
  public int furnaceCookTime;
  /**
   * How hot the furnace is in Celsius (range 0-200)
   */
  public int furnaceHeat = 21;
  /**
   * How hot the furnace is allowed to get default; melting point of tungsten
   */
  public int furnaceMaxHeat = 3422;
  /**
   * How fast the furnace smelts items
   */
  public int furnaceSmeltSpeed = 200;

  private String containerName = "Brick Furnace";
  private static final String __OBFID = "CL_00000357";

  /**
   * Returns the number of slots in the inventory.
   */
  public int getSizeInventory() {
    return this.furnaceItemStacks.length;
  }

  /**
   * Returns the stack in slot i
   */
  public ItemStack getStackInSlot(int par1) {
    return this.furnaceItemStacks[par1];
  }

  /**
   * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and
   * returns them in a new stack.
   */
  public ItemStack decrStackSize(int par1, int par2) {
    if (this.furnaceItemStacks[par1] != null) {
      ItemStack itemstack;

      if (this.furnaceItemStacks[par1].stackSize <= par2) {
        itemstack = this.furnaceItemStacks[par1];
        this.furnaceItemStacks[par1] = null;
        return itemstack;
      } else {
        itemstack = this.furnaceItemStacks[par1].splitStack(par2);

        if (this.furnaceItemStacks[par1].stackSize == 0) {
          this.furnaceItemStacks[par1] = null;
        }

        return itemstack;
      }
    } else {
      return null;
    }
  }

  /**
   * When some containers are closed they call this on each slot, then drop whatever it returns as
   * an EntityItem - like when you close a workbench GUI.
   */
  public ItemStack getStackInSlotOnClosing(int par1) {
    if (this.furnaceItemStacks[par1] != null) {
      ItemStack itemstack = this.furnaceItemStacks[par1];
      this.furnaceItemStacks[par1] = null;
      return itemstack;
    } else {
      return null;
    }
  }

  /**
   * Sets the given item stack to the specified slot in the inventory (can be crafting or armor
   * sections).
   */
  public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
    this.furnaceItemStacks[par1] = par2ItemStack;

    if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit()) {
      par2ItemStack.stackSize = this.getInventoryStackLimit();
    }
  }

  /**
   * Returns the name of the inventory
   */
  public String getInventoryName() {
    return this.hasCustomInventoryName() ? this.containerName : "container.furnace";
  }

  /**
   * Returns if the inventory is named
   */
  public boolean hasCustomInventoryName() {
    return this.containerName != null && this.containerName.length() > 0;
  }

  public void setName(String p_145951_1_) {
    this.containerName = p_145951_1_;
  }

  public void readFromNBT(NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);
    NBTTagList nbttaglist = tagCompound.getTagList("Items", 10);
    this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
      NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
      byte b0 = nbttagcompound1.getByte("Slot");

      if (b0 >= 0 && b0 < this.furnaceItemStacks.length) {
        this.furnaceItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
      }
    }

    this.furnaceBurnTime = tagCompound.getShort("BurnTime");
    this.furnaceCookTime = tagCompound.getShort("CookTime");
    this.furnaceHeat = tagCompound.getShort("Heat");
    this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);

    if (tagCompound.hasKey("CustomName", 8)) {
      this.containerName = tagCompound.getString("CustomName");
    }
  }

  public void writeToNBT(NBTTagCompound tagCompound) {
    super.writeToNBT(tagCompound);
    tagCompound.setShort("BurnTime", (short) this.furnaceBurnTime);
    tagCompound.setShort("CookTime", (short) this.furnaceCookTime);
    tagCompound.setShort("Heat", (short) this.furnaceHeat);
    NBTTagList nbttaglist = new NBTTagList();

    for (int i = 0; i < this.furnaceItemStacks.length; ++i) {
      if (this.furnaceItemStacks[i] != null) {
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setByte("Slot", (byte) i);
        this.furnaceItemStacks[i].writeToNBT(nbttagcompound1);
        nbttaglist.appendTag(nbttagcompound1);
      }
    }

    tagCompound.setTag("Items", nbttaglist);

    if (this.hasCustomInventoryName()) {
      tagCompound.setString("CustomName", this.containerName);
    }
  }

  /**
   * Returns the maximum stack size for a inventory slot.
   */
  public int getInventoryStackLimit() {
    return 64;
  }

  /**
   * Returns an integer between 0 and the passed value representing how close the current item is to
   * being completely cooked
   */
  @SideOnly(Side.CLIENT)
  public int getCookProgressScaled(int progress) {
    return this.furnaceCookTime * progress / furnaceSmeltSpeed;//200;
  }

  /**
   * Returns an integer between 0 and the passed value representing how much burn time is left on
   * the current fuel item, where 0 means that the item is exhausted and the passed value means that
   * the item is fresh
   */
  @SideOnly(Side.CLIENT)
  public int getBurnTimeRemainingScaled(int progress) {
    if (this.currentItemBurnTime == 0) {
      this.currentItemBurnTime = furnaceSmeltSpeed;//200;
    }

    return this.furnaceBurnTime * progress / this.currentItemBurnTime;
  }

  public void increaseFurnaceHeat(int toAdd) {
    this.furnaceHeat += toAdd;
    if (this.furnaceHeat > this.furnaceMaxHeat) {
      this.furnaceHeat = this.furnaceMaxHeat;
    }
    this.furnaceSmeltSpeed = 200 - (200 / 2422 * this.furnaceHeat);
  }

  /**
   * Furnace isBurning
   */
  public boolean isBurning() {
    return this.furnaceBurnTime > 0;
  }

  public void updateEntity() {
    boolean flag = this.furnaceBurnTime > 0;
    boolean flag1 = false;

    if (this.furnaceBurnTime > 0) {
      --this.furnaceBurnTime;
      increaseFurnaceHeat(1);//if (this.furnaceHeat<2423) ++this.furnaceHeat;
    }

    if (!this.worldObj.isRemote) {
      if (this.furnaceBurnTime == 0 && this.canSmelt()) {
        this.currentItemBurnTime =
        this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);

        if (this.furnaceBurnTime > 0) {
          flag1 = true;

          if (this.furnaceItemStacks[1] != null) {
            if (this.furnaceItemStacks[1].isItemEqual(new ItemStack(NOVA.itemSuperCoal, 1))) {
              increaseFurnaceHeat(1000);//this.furnaceHeat+=1000;
            }
            if (this.furnaceHeat > this.furnaceMaxHeat) {
              this.furnaceHeat = this.furnaceMaxHeat;
            }
            --this.furnaceItemStacks[1].stackSize;

            if (this.furnaceItemStacks[1].stackSize == 0) {
              this.furnaceItemStacks[1] =
                  furnaceItemStacks[1].getItem().getContainerItem(furnaceItemStacks[1]);
            }
          }
        }
      }

      if (this.isBurning() && this.canSmelt()) {
        ++this.furnaceCookTime;

        if (this.furnaceCookTime == furnaceSmeltSpeed)//200)
        {
          this.furnaceCookTime = 0;
          this.smeltItem();
          flag1 = true;
        }
      } else {
        this.furnaceCookTime = 0;
      }

      if (flag != this.furnaceBurnTime > 0) {
        flag1 = true;
        BlockBrickFurnace
            .updateFurnaceBlockState(this.furnaceBurnTime > 0, this.worldObj, this.xCoord,
                                     this.yCoord, this.zCoord);
      }
    }

    if (flag1) {
      this.markDirty();
    }
  }

  /**
   * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't
   * full, etc.
   */
  private boolean canSmelt() {
    if (this.furnaceItemStacks[0] == null) {
      return false;
    } else {
      ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);
      if (itemstack == null) {
        return false;
      }
      if (this.furnaceItemStacks[2] == null) {
        return true;
      }
      if (!this.furnaceItemStacks[2].isItemEqual(itemstack)) {
        return false;
      }
      int result = furnaceItemStacks[2].stackSize + itemstack.stackSize;
      return result <= getInventoryStackLimit() && result <= this.furnaceItemStacks[2]
          .getMaxStackSize(); //Forge BugFix: Make it respect stack sizes properly.
    }
  }  /**
   * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if
   * the item isn't fuel
   */
  public static int getItemBurnTime(ItemStack p_145952_0_) {
    if (p_145952_0_ == null) {
      return 0;
    } else {
      Item item = p_145952_0_.getItem();

      if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air) {
        Block block = Block.getBlockFromItem(item);

        if (block == Blocks.wooden_slab) {
          return 150;
        }

        if (block.getMaterial() == Material.wood) {
          return 300;
        }

        if (block == Blocks.coal_block) {
          return 16000;
        }
      }

      if (item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("WOOD")) {
        return 200;
      }
      if (item instanceof ItemSword && ((ItemSword) item).getToolMaterialName().equals("WOOD")) {
        return 200;
      }
      if (item instanceof ItemHoe && ((ItemHoe) item).getToolMaterialName().equals("WOOD")) {
        return 200;
      }
      if (item == Items.stick) {
        return 100;
      }
      if (item == Items.coal) {
        return 1600;
      }
      if (item == Items.lava_bucket) {
        return 20000;
      }
      if (item == Item.getItemFromBlock(Blocks.sapling)) {
        return 100;
      }
      if (item == Items.blaze_rod) {
        return 2400;
      }
      return GameRegistry.getFuelValue(p_145952_0_);
    }
  }

  /**
   * Turn one item from the furnace source stack into the appropriate smelted item in the furnace
   * result stack
   */
  public void smeltItem() {
    if (this.canSmelt()) {
      ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);

      if (this.furnaceItemStacks[2] == null) {
        this.furnaceItemStacks[2] = itemstack.copy();
      } else if (this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
        this.furnaceItemStacks[2].stackSize +=
            itemstack.stackSize; // Forge BugFix: Results may have multiple items
      }

      --this.furnaceItemStacks[0].stackSize;

      if (this.furnaceItemStacks[0].stackSize <= 0) {
        this.furnaceItemStacks[0] = null;
      }
    }
  }  public static boolean isItemFuel(ItemStack p_145954_0_) {
    /**
     * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
     * fuel
     */
    return getItemBurnTime(p_145954_0_) > 0;
  }

  /**
   * Do not make give this method the name canInteractWith because it clashes with Container
   */
  public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) {
    return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false :
           par1EntityPlayer.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D,
                                          (double) this.zCoord + 0.5D) <= 64.0D;
  }

  public void openInventory() {
  }

  public void closeInventory() {
  }

  /**
   * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the
   * given slot.
   */
  public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
    return par1 == 2 ? false : (par1 == 1 ? isItemFuel(par2ItemStack) : true);
  }

  /**
   * Returns an array containing the indices of the slots that can be accessed by automation on the
   * given side of this block.
   */
  public int[] getAccessibleSlotsFromSide(int par1) {
    return par1 == 0 ? slotsBottom : (par1 == 1 ? slotsTop : slotsSides);
  }

  /**
   * Returns true if automation can insert the given item in the given slot from the given side.
   * Args: Slot, item, side
   */
  public boolean canInsertItem(int par1, ItemStack par2ItemStack, int par3) {
    return this.isItemValidForSlot(par1, par2ItemStack);
  }

  /**
   * Returns true if automation can extract the given item in the given slot from the given side.
   * Args: Slot, item, side
   */
  public boolean canExtractItem(int par1, ItemStack par2ItemStack, int par3) {
    return par3 != 0 || par1 != 1 || par2ItemStack.getItem() == Items.bucket;
  }
}