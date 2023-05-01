package io.github.guywithapc.usefulfletching.mixin;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TippedArrowItem.class)
public class TippedArrowItemMixin extends ArrowItem {

    public TippedArrowItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        ArrowEntity arrowEntity = new ArrowEntity(world, shooter);
        arrowEntity.initFromStack(stack);
        if (stack.hasNbt() && stack.getNbt().contains("Flaming") && stack.getNbt().getBoolean("Flaming")) {
            arrowEntity.setOnFireFor(10);
        }
        return arrowEntity;
    }
}
