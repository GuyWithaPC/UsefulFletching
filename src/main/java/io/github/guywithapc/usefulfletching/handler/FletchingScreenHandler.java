package io.github.guywithapc.usefulfletching.handler;

import io.github.guywithapc.usefulfletching.UsefulFletching;
import io.github.guywithapc.usefulfletching.recipe.FletchingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class FletchingScreenHandler extends ScreenHandler {
    private final Inventory input;
    private final CraftingResultInventory output = new CraftingResultInventory();
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final World world;
    private final FletchingSlotsManager fletchingSlotsManager;
    private final List<Integer> inputSlotIndices;
    private final int outputSlotIndex;

    @Nullable
    private FletchingRecipe currentRecipe;

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(UsefulFletching.FLETCHING_SCREEN_TYPE, syncId);
        this.context = context;
        this.player = playerInventory.player;
        this.world = player.world;

        // set up inventory
        this.fletchingSlotsManager = this.getFletchingSlotsManager();
        this.input = createInputInventory(fletchingSlotsManager.getInputSlotCount());
        this.createInputSlots(fletchingSlotsManager);
        this.inputSlotIndices = fletchingSlotsManager.getInputSlotIndices();
        this.createOutputSlot(fletchingSlotsManager);
        this.outputSlotIndex = fletchingSlotsManager.getOutputSlotIndex();
        this.addPlayerInventorySlots(player.getInventory());
    }

    private FletchingSlotsManager getFletchingSlotsManager() {
        // main row is at Y = 39
        // input 1 is at (51, 11) (arrows top)
        // input 2 is at (27, 35) (arrows middle)
        // input 3 is at (51, 60) (arrows bottom)
        // input 4 is at (76, 35) (ingredient)
        // output is at (134, 35)
        return FletchingSlotsManager.create()
                .input(0, 51, 11, (stack) -> {
                    return true;
                })
                .input(1, 27, 35, (stack) -> {
                    return true;
                })
                .input(2, 51, 60, (stack) -> {
                    return true;
                })
                .input(3, 76, 35, (stack) -> {
                    return true;
                })
                .output(4, 134, 35)
                .build();
    }

    private SimpleInventory createInputInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty() {
                super.markDirty();
                FletchingScreenHandler.this.onContentChanged(this);
            }
        };
    }

    private void createInputSlots(FletchingSlotsManager manager) {
        for (FletchingSlotsManager.FletchingSlot slot : manager.getInputSlots()) {
            this.addSlot(new Slot(this.input, slot.slotId(), slot.x(), slot.y()) {
                public boolean canInsert(ItemStack stack) {
                    return slot.mayPlace().test(stack);
                }
            });
        }
    }

    private void createOutputSlot(FletchingSlotsManager manager) {
        this.addSlot(new Slot(this.output, manager.getOutputSlot().slotId(), manager.getOutputSlot().x(), manager.getOutputSlot().y()) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            public boolean canTakeItems(PlayerEntity player) {
                return FletchingScreenHandler.this.canTakeOutput(player);
            }

            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                FletchingScreenHandler.this.onTakeOutput(player, stack);
            }
        });
    }

    private void addPlayerInventorySlots(PlayerInventory playerInventory) {
        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

    }

    public void updateResult() {
        Optional<FletchingRecipe> optional = world.getRecipeManager().getFirstMatch(
                UsefulFletching.FLETCHING_RECIPE_TYPE,
                this.input,
                this.world
        );
        if (optional.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
        } else {
            FletchingRecipe recipe = optional.get();
            ItemStack result = recipe.craft(this.input, world.getRegistryManager());
            this.currentRecipe = recipe;
            this.output.setLastRecipe(recipe);
            this.output.setStack(0,result);
        }
    }

    public boolean canTakeOutput(PlayerEntity player) {
        return this.currentRecipe != null && this.currentRecipe.matches(this.input, this.world);
    }

    public void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraft(world, player, stack.getCount());
        this.output.unlockLastRecipe(player);
        this.decrementStack(0);
        this.decrementStack(1);
        this.decrementStack(2);
        this.decrementStack(3);
        if (!world.isClient) {
            Random rng = new Random();
            this.context.run((world, pos) -> {
                world.playSound(
                        null,
                        pos,
                        SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f + rng.nextFloat(-0.1f,0.1f)
                );
            });
        }
    }

    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void decrementStack(int slot) {
        ItemStack stack = this.input.getStack(slot);
        stack.decrement(1);
        this.input.setStack(slot, stack);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.input);
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            int i = this.getPlayerInventoryStartIndex();
            int j = this.getPlayerHotbarEndIndex();
            if (slot == this.getOutputSlotIndex()) {
                if (!this.insertItem(itemStack2, i, j, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (this.inputSlotIndices.contains(slot)) {
                if (!this.insertItem(itemStack2, i, j, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.isValidIngredient(itemStack2) && slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerHotbarEndIndex()) {
                int k = this.getSlotFor(itemStack);
                if (!this.insertItem(itemStack2, k, this.getOutputSlotIndex(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerInventoryEndIndex()) {
                if (!this.insertItem(itemStack2, this.getPlayerHotbarStartIndex(), this.getPlayerHotbarEndIndex(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= this.getPlayerHotbarStartIndex() && slot < this.getPlayerHotbarEndIndex() && !this.insertItem(itemStack2, this.getPlayerInventoryStartIndex(), this.getPlayerInventoryEndIndex(), false)) {
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
        }

        return itemStack;
    }

    private boolean isValidIngredient(ItemStack item) {
        return true;
    }

    private int getSlotFor(ItemStack item) {
        return this.input.isEmpty() ? 0 : inputSlotIndices.get(0);
    }

    public int getOutputSlotIndex() {
        return this.outputSlotIndex;
    }

    private int getPlayerInventoryStartIndex() {
        return this.getOutputSlotIndex() + 1;
    }

    private int getPlayerInventoryEndIndex() {
        return this.getPlayerInventoryStartIndex() + 27;
    }

    private int getPlayerHotbarStartIndex() {
        return this.getPlayerInventoryEndIndex();
    }

    private int getPlayerHotbarEndIndex() {
        return this.getPlayerHotbarStartIndex() + 9;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return context.get((world, pos) -> {
            return player.squaredDistanceTo((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }
}
