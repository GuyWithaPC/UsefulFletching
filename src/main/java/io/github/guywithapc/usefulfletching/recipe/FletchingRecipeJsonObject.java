package io.github.guywithapc.usefulfletching.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FletchingRecipeJsonObject {
    JsonObject ingredients;
    JsonObject result;
    List<JsonObject> ingredientList;
    public FletchingRecipeJsonObject build() {
        ingredientList = new ArrayList<JsonObject>();

        // process ingredients as a list of keys and values
        JsonArray pattern = JsonHelper.getArray(ingredients, "pattern");
        JsonObject legend = JsonHelper.getObject(ingredients, "key");
        for (int i = 0; i < pattern.size(); i++) {
            String key = pattern.get(i).getAsString();
            ingredientList.add(JsonHelper.getObject(legend,key));
        }

        return this;
    }
}
