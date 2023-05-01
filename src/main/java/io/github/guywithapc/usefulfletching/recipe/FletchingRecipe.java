package io.github.guywithapc.usefulfletching.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.Predicate;

public class FletchingRecipe implements Recipe<Inventory> {

    private final Identifier id;
    private final ItemStack output;
    private final DefaultedList<Ingredient> items;
    public FletchingRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems) {
        this.id = id;
        this.output = output;
        this.items = recipeItems;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return items.get(0).test(inventory.getStack(0))
                && items.get(1).test(inventory.getStack(1))
                && items.get(2).test(inventory.getStack(2))
                && items.get(3).test(inventory.getStack(3));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FletchingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "fletching";
        private Type() {

        }
    }

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "fletching";
        private Serializer() {

        }
        @Override
        public FletchingRecipe read(Identifier id, JsonObject json) {
            FletchingRecipeJsonObject recipeObject = new Gson()
                    .fromJson(json, FletchingRecipeJsonObject.class)
                    .build();
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(4,Ingredient.EMPTY);
            for (int i = 0; i < recipeObject.ingredientList.size(); i++) {
                inputs.set(i,Ingredient.fromJson(recipeObject.ingredientList.get(i)));
            }
            ItemStack output = ShapedRecipe.outputFromJson(recipeObject.result);
            return new FletchingRecipe(id, output, inputs);
        }

        @Override
        public FletchingRecipe read(Identifier id, PacketByteBuf buf) {
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(buf.readInt(),Ingredient.EMPTY);
            for (int i = 0; i < 4; i++) {
                inputs.set(i,Ingredient.fromPacket(buf));
            }
            ItemStack output = buf.readItemStack();
            return new FletchingRecipe(id,output,inputs);
        }

        @Override
        public void write(PacketByteBuf buf, FletchingRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.write(buf);
            }
            buf.writeItemStack(recipe.getOutput(DynamicRegistryManager.EMPTY));
        }
    }
}
