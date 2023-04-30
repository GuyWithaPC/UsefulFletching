package io.github.guywithapc.usefulfletching;

import io.github.guywithapc.usefulfletching.handler.FletchingScreenHandler;
import io.github.guywithapc.usefulfletching.handler.FletchingScreenHandlerFactory;
import io.github.guywithapc.usefulfletching.recipe.FletchingRecipe;
import io.github.guywithapc.usefulfletching.screen.FletchingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class UsefulFletchingClient implements ClientModInitializer {


    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {

    }
}
