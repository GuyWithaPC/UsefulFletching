package io.github.guywithapc.usefulfletching;

import io.github.guywithapc.usefulfletching.handler.FletchingScreenHandler;
import io.github.guywithapc.usefulfletching.handler.FletchingScreenHandlerFactory;
import io.github.guywithapc.usefulfletching.recipe.FletchingRecipe;
import io.github.guywithapc.usefulfletching.screen.FletchingScreen;
import net.fabricmc.api.ModInitializer;
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

public class UsefulFletching implements ModInitializer {

    public static final String MOD_ID = "usefulfletching";
    public static Identifier INTERACT_WITH_FLETCHING_TABLE = new Identifier(MOD_ID,"interact_with_fletching_table");
    public static ScreenHandlerType<FletchingScreenHandler> FLETCHING_SCREEN_TYPE = new ScreenHandlerType<FletchingScreenHandler>(
            new FletchingScreenHandlerFactory(),
            FeatureSet.empty()
    );

    public static final RecipeManager FLETCHING_RECIPES = new RecipeManager();
    public static final RecipeSerializer<FletchingRecipe> FLETCHING_RECIPE_SERIALIZER = FletchingRecipe.Serializer.INSTANCE;
    public static final RecipeType<FletchingRecipe> FLETCHING_RECIPE_TYPE = FletchingRecipe.Type.INSTANCE;

    static {
        Registry.register(
                Registries.SCREEN_HANDLER,
                "fletching_screen_handler_type",
                FLETCHING_SCREEN_TYPE
        );
        Registry.register(
                Registries.CUSTOM_STAT,
                "interact_with_fletching_table",
                INTERACT_WITH_FLETCHING_TABLE
        );
        Registry.register(
                Registries.RECIPE_SERIALIZER,
                new Identifier(MOD_ID,FletchingRecipe.Serializer.ID),
                FLETCHING_RECIPE_SERIALIZER
        );
        Registry.register(
                Registries.RECIPE_TYPE,
                new Identifier(MOD_ID,FletchingRecipe.Type.ID),
                FLETCHING_RECIPE_TYPE
        );
    }

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        Stats.CUSTOM.getOrCreateStat(INTERACT_WITH_FLETCHING_TABLE, StatFormatter.DEFAULT);
        HandledScreens.register(FLETCHING_SCREEN_TYPE, FletchingScreen::new);
    }
}
