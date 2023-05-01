package io.github.guywithapc.usefulfletching.handler;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FletchingSlotsManager {

    private final List<FletchingSlot> inputSlots;
    private final FletchingSlot outputSlot;

    FletchingSlotsManager(List<FletchingSlot> inputSlots, FletchingSlot resultSlot) {
        if (!inputSlots.isEmpty() && !resultSlot.equals(FletchingSlot.DEFAULT)) {
            this.inputSlots = inputSlots;
            this.outputSlot = resultSlot;
        } else {
            throw new IllegalArgumentException("Need to define both input slots and result slot.");
        }
    }

    public static Builder create() {
        return new Builder();
    }

    public boolean hasSlotIndex(int index) {
        return this.inputSlots.size() >= index;
    }

    public FletchingSlot getInputSlot(int index) {
        return this.inputSlots.get(index);
    }

    public FletchingSlot getOutputSlot() {
        return this.outputSlot;
    }

    public List<FletchingSlot> getInputSlots() {
        return this.inputSlots;
    }

    public int getInputSlotCount() {
        return this.inputSlots.size();
    }

    public int getOutputSlotIndex() {
        return this.getInputSlotCount();
    }

    public List<Integer> getInputSlotIndices() {
        return this.inputSlots.stream().map(FletchingSlot::slotId).collect(Collectors.toList());
    }

    public static class Builder {
        private final List<FletchingSlot> inputSlots = new ArrayList<FletchingSlot>();
        private FletchingSlot outputSlot;

        public Builder() {
            this.outputSlot = FletchingSlot.DEFAULT;
        }

        public Builder input(int slotId, int x, int y, Predicate<ItemStack> predicate) {
            this.inputSlots.add(new FletchingSlot(slotId, x, y, predicate));
            return this;
        }

        public Builder output(int slotId, int x, int y) {
            this.outputSlot = new FletchingSlot(slotId, x, y, (stack) -> false);
            return this;
        }

        public FletchingSlotsManager build() {
            return new FletchingSlotsManager(this.inputSlots, this.outputSlot);
        }
    }

    public static record FletchingSlot(int slotId, int x, int y, Predicate<ItemStack> mayPlace) {

        static final FletchingSlot DEFAULT = new FletchingSlot(0, 0, 0, (stack) -> {
            return true;
        });

        public FletchingSlot(int slotId, int x, int y, Predicate<ItemStack> mayPlace) {
            this.slotId = slotId;
            this.x = x;
            this.y = y;
            this.mayPlace = mayPlace;
        }

        public int slotId() {
            return this.slotId;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Predicate<ItemStack> mayPlace() {
            return this.mayPlace;
        }
    }
}
