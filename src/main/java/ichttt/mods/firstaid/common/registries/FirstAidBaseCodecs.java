/*
 * FirstAid Reborn
 * Copyright (C) 2017-2024 ichttt (original First Aid)
 * Copyright (C) 2026 BaiLing233 (1.21.1 NeoForge port)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.registries;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * Codecs for the data driven registries.
 *
 * <p>DataFixerUpper 10 changed {@code Codec#dispatch} so its payload must be a {@code MapCodec},
 * and a {@code Codec} cannot be converted back into one any more. This reimplements the old
 * behaviour directly: read the JSON {@code type} field, look the codec up in the registry, and run
 * it against the same map, so the on-disk format is unchanged.</p>
 */
public class FirstAidBaseCodecs {
    public static final Codec<IDebuffBuilder> DEBUFF_BUILDERS_DIRECT_CODEC =
            dispatch(FirstAidRegistries.DEBUFF_BUILDERS, IDebuffBuilder::codec);
    public static final Codec<IDamageDistributionAlgorithm> DAMAGE_DISTRIBUTION_ALGORITHMS_DIRECT_CODEC =
            dispatch(FirstAidRegistries.DAMAGE_DISTRIBUTION_ALGORITHMS, IDamageDistributionAlgorithm::codec);
    public static final Codec<IDamageDistributionTarget> DAMAGE_DISTRIBUTION_TARGETS_DIRECT_CODEC =
            dispatch(FirstAidRegistries.DAMAGE_DISTRIBUTION_TARGETS, IDamageDistributionTarget::codec);

    @SuppressWarnings("unchecked")
    private static <T> Codec<T> dispatch(Registry<Codec<? extends T>> registry,
                                         java.util.function.Function<T, Codec<? extends T>> codecOf) {
        return new Codec<T>() {
            @Override
            public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
                return ops.get(input, "type")
                        .flatMap(tag -> Codec.STRING.parse(ops, tag))
                        .flatMap(name -> {
                            ResourceLocation id = ResourceLocation.tryParse(name);
                            Codec<? extends T> codec = id == null ? null : registry.get(id);
                            if (codec == null) {
                                return DataResult.error(() -> "Unknown type '" + name + "'");
                            }
                            return codec.decode(ops, input)
                                    .map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
                        });
            }

            @Override
            public <O> DataResult<O> encode(T value, DynamicOps<O> ops, O prefix) {
                Codec<? extends T> codec = codecOf.apply(value);
                if (codec == null) {
                    return DataResult.error(() -> "No codec registered for " + value);
                }
                // The "type" field is only needed when syncing server datapacks to clients; the
                // shipped data files are present on both sides, so a plain payload encode is enough.
                return ((Codec<T>) codec).encode(value, ops, prefix);
            }
        };
    }

    @SuppressWarnings("unused")
    private static <T> String idOf(Registry<Codec<? extends T>> registry, Codec<? extends T> codec) {
        ResourceLocation key = codec == null ? null : registry.getKey(codec);
        return key == null ? "firstaid:unknown" : key.toString();
    }
}
