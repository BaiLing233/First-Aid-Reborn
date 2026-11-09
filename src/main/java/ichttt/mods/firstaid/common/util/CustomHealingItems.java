package ichttt.mods.firstaid.common.util;

import java.util.Optional;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Lets packs use items from any mod as First Aid healing items, or as debuff suppressors
 * (morphine-like), without writing code.
 *
 * <p>Config format, one entry per line in the list:</p>
 * <pre>
 * healing:    "namespace:item,hearts,secondsPerHalfHeart,applyTimeMs,showTooltip"
 * suppressor: "namespace:item,seconds,showTooltip"
 * </pre>
 */
public final class CustomHealingItems {

    public record HealSpec(ResourceLocation id, int totalHeals, int ticksPerHeal, int applyTime, boolean showTooltip) {
        public int hearts() {
            return totalHeals / 2;
        }

        public double secondsPerHeal() {
            return ticksPerHeal / 20.0D;
        }
    }

    public record SuppressSpec(ResourceLocation id, int ticks, boolean showTooltip) {
        public double seconds() {
            return ticks / 20.0D;
        }
    }

    @Nullable
    public static HealSpec getHeal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String raw : FirstAidConfig.SERVER.customHealingItems.get()) {
            HealSpec spec = parseHeal(raw);
            if (spec != null && spec.id().equals(id)) {
                return spec;
            }
        }
        return null;
    }

    @Nullable
    public static SuppressSpec getSuppressor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String raw : FirstAidConfig.SERVER.customDebuffSuppressors.get()) {
            SuppressSpec spec = parseSuppressor(raw);
            if (spec != null && spec.id().equals(id)) {
                return spec;
            }
        }
        return null;
    }

    public static PartHealer createHealer(HealSpec spec, ItemStack stack) {
        return new PartHealer((IntSupplier) () -> spec.ticksPerHeal(), (IntSupplier) () -> spec.totalHeals(), stack);
    }

    @Nullable
    public static HealSpec parseHeal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length < 3) {
            warn(raw, "expected namespace:item,hearts,secondsPerHalfHeart[,applyTimeMs][,showTooltip]");
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null) {
            warn(raw, "invalid item id '" + parts[0] + "'");
            return null;
        }
        try {
            double hearts = Double.parseDouble(parts[1].trim());
            double secondsPerHalfHeart = Double.parseDouble(parts[2].trim());
            int applyTime = parts.length > 3 ? Integer.parseInt(parts[3].trim()) : 40;
            boolean showTooltip = parts.length <= 4 || Boolean.parseBoolean(parts[4].trim());
            int totalHeals = Math.max(1, (int) Math.round(hearts * 2));
            int ticksPerHeal = Math.max(1, (int) Math.round(secondsPerHalfHeart * 20));
            return new HealSpec(id, totalHeals, ticksPerHeal, Math.max(0, applyTime), showTooltip);
        } catch (NumberFormatException e) {
            warn(raw, "numbers could not be parsed");
            return null;
        }
    }

    @Nullable
    public static SuppressSpec parseSuppressor(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length < 2) {
            warn(raw, "expected namespace:item,seconds[,showTooltip]");
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null) {
            warn(raw, "invalid item id '" + parts[0] + "'");
            return null;
        }
        try {
            double seconds = Double.parseDouble(parts[1].trim());
            boolean showTooltip = parts.length <= 2 || Boolean.parseBoolean(parts[2].trim());
            return new SuppressSpec(id, Math.max(1, (int) Math.round(seconds * 20)), showTooltip);
        } catch (NumberFormatException e) {
            warn(raw, "duration could not be parsed");
            return null;
        }
    }

    public static Optional<Item> itemOf(ResourceLocation id) {
        return BuiltInRegistries.ITEM.containsKey(id) ? Optional.of(BuiltInRegistries.ITEM.get(id)) : Optional.empty();
    }

    private static void warn(String raw, String reason) {
        FirstAid.LOGGER.warn("Ignoring custom First Aid item entry '{}': {}", raw, reason);
    }

    private CustomHealingItems() {
    }
}
