package io.github.guywithapc.usefulfletching.handler;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;

public class FletchingScreenHandlerFactory implements ScreenHandlerType.Factory<FletchingScreenHandler> {

    @Override
    public FletchingScreenHandler create(int syncId, PlayerInventory playerInventory) {
        return new FletchingScreenHandler(syncId, playerInventory);
    }
}
