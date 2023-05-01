package io.github.guywithapc.usefulfletching.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin implements CraftingRecipe {

    /**
     * This mixin adds the ability to parse the "data" tag as providing NBT data for the item
     * @author GuyWithaPC
     * @reason not sure why this was disallowed in the first place, it would make datapacks much easier.
     */
    @Overwrite
    public static ItemStack outputFromJson(JsonObject json) {
        Item item = ShapedRecipe.getItem(json);
        int i = JsonHelper.getInt(json,"count",1);
        if (i < 1) {
            throw new JsonSyntaxException("Invalid output count: " + i);
        } else {
            ItemStack itemStack = new ItemStack(item, i);
            if (json.has("data")) {
                try {
                    NbtCompound data = new StringNbtReader(new StringReader(
                            JsonHelper.getObject(json,"data").toString()
                    )).parseCompound();
                    for (String key : data.getKeys()) {
                        itemStack.setSubNbt(key, data.get(key));
                    }
                } catch (CommandSyntaxException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            return itemStack;
        }
    }
}
