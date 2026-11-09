package ichttt.mods.firstaid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ichttt.mods.firstaid.common.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Records which projectile hit which player, so the damage can be routed to the limb the projectile
 * actually struck instead of a random one.
 */
@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Inject(method = "onHit", at = @At("HEAD"))
    private void firstaid$onHit(HitResult result, CallbackInfo ci) {
        if (result.getType() != HitResult.Type.ENTITY) {
            return;
        }
        Entity entity = ((EntityHitResult) result).getEntity();
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return;
        }
        EventHandler.hitList.put(player, Pair.of((Projectile) (Object) this, result));
    }
}
