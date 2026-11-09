package ichttt.mods.firstaid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Accessors for the protected vanilla helpers First Aid needs to call directly. */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("getHurtSound")
    SoundEvent firstaid$getHurtSound(DamageSource source);

    @Invoker("getSoundVolume")
    float firstaid$getSoundVolume();

    @Invoker("getDamageAfterMagicAbsorb")
    float firstaid$getDamageAfterMagicAbsorb(DamageSource source, float damage);
}
