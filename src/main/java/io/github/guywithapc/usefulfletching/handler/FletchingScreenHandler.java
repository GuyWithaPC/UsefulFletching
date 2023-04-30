package io.github.guywithapc.usefulfletching.handler;

import io.github.guywithapc.usefulfletching.UsefulFletching;
import io.github.guywithapc.usefulfletching.UsefulFletchingClient;
import io.github.guywithapc.usefulfletching.mixin.FletchingTableMixin;
import io.github.guywithapc.usefulfletching.recipe.FletchingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FletchingScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {
    private final CraftingInventory input;
    private final CraftingResultInventory result;
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final List<FletchingRecipe> recipes;

    @Nullable
    private FletchingRecipe currentRecipe;

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(UsefulFletching.FLETCHING_SCREEN_TYPE, syncId);
        this.input = new CraftingInventory(this,2,1);
        this.result = new CraftingResultInventory();
        this.context = context;
        this.player = playerInventory.player;

        // ingredient and result slots
        this.addSlot(new Slot(this.input, 0, 27, 47));
        this.addSlot(new Slot(this.input, 1, 76, 47));
        this.addSlot(new CraftingResultSlot(player, this.input, this.result, 2, 134, 47));

        int i;
        int j;

        // inventory slots
        for(i = 0; i < 3; ++i) {
            for(j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // hotbar slots
        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        // set up recipes
        this.recipes = this.player.world.getRecipeManager().listAllOfType(UsefulFletching.FLETCHING_RECIPE_TYPE);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    public void clearInputSlots() {
        this.input.clear();
    }

    @Override
    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(input,player.world);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == getCraftingResultSlotIndex()) {
            if (actionType == SlotActionType.PICKUP
                    || actionType == SlotActionType.THROW
            ) {
                System.out.println("pickup action");
                decrementStack(0);
                decrementStack(1);
            }
            if (actionType == SlotActionType.PICKUP_ALL
                    || actionType == SlotActionType.QUICK_CRAFT
                    || actionType == SlotActionType.QUICK_MOVE
            ) {
                System.out.println("pickup all action");
                clearInputSlots();
            }
        }
        super.onSlotClick(slotIndex,button,actionType,player);
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.input.getStack(slot);
        itemStack.decrement(1);
        this.input.setStack(slot, itemStack);
    }

    public void updateResult() {
        if (!player.world.isClient) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            ItemStack itemStack = ItemStack.EMPTY;
            Optional<FletchingRecipe> optional = this.player.world
                    .getRecipeManager()
                    .getFirstMatch(UsefulFletching.FLETCHING_RECIPE_TYPE, this.input, this.player.world);
            if (optional.isPresent()) {
                currentRecipe = optional.get();
                if (result.shouldCraftRecipe(serverPlayerEntity.world,serverPlayerEntity,currentRecipe)) {
                    ItemStack itemStack2 = currentRecipe.craft(input, serverPlayerEntity.world.getRegistryManager());
                    if (itemStack2.isItemEnabled(serverPlayerEntity.world.getEnabledFeatures())) {
                        itemStack = itemStack2;
                    }
                }
            }
            result.setLastRecipe(currentRecipe);
            result.setStack(2,itemStack);
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 2, itemStack));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world,pos) -> {
            updateResult();
        });
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.input);
        });
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 2;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 3;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return null;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 2) {
                this.context.run((world, pos) -> {
                    itemStack2.getItem().onCraft(itemStack2, world, player);
                });
                if (!this.insertItem(itemStack2, 3, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot < 2) {
                if (!this.insertItem(itemStack2, 3, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
            if (slot == 2) {
                player.dropItem(itemStack2, false);
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return context.get((world, pos) -> {
            return player.squaredDistanceTo((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }
}
