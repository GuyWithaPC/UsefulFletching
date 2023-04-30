package io.github.guywithapc.usefulfletching.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.guywithapc.usefulfletching.UsefulFletching;
import io.github.guywithapc.usefulfletching.UsefulFletchingClient;
import io.github.guywithapc.usefulfletching.handler.FletchingScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FletchingScreen extends HandledScreen<FletchingScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(UsefulFletching.MOD_ID,"textures/gui/container/fletching.png");

    public FletchingScreen(FletchingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.titleX = 50;
        this.titleY = 15;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0,TEXTURE);
        RenderSystem.bindTexture(0);
        drawTexture(matrices,this.x,this.y,0,0,this.backgroundWidth,this.backgroundHeight);
        this.drawInvalidRecipeArrow(matrices,this.x,this.y);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices,mouseX,mouseY,delta);
        this.drawMouseoverTooltip(matrices,mouseX,mouseY);
    }

    private void drawInvalidRecipeArrow(MatrixStack matrices, int x, int y) {
        FletchingScreenHandler fletchingHandler = (FletchingScreenHandler)this.handler;
        if ((fletchingHandler.getSlot(1).hasStack()
                || fletchingHandler.getSlot(2).hasStack())
                && !fletchingHandler.getSlot(fletchingHandler.getCraftingResultSlotIndex()).hasStack()
        ) {
            drawTexture(matrices, x+99, y+45, this.backgroundWidth, 0, 28, 21);
        }
    }
}
