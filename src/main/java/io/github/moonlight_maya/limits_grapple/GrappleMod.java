package io.github.moonlight_maya.limits_grapple;

import io.github.moonlight_maya.limits_grapple.item.GrappleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;

public class GrappleMod implements ModInitializer {

	public static final String MODID = "limits_grapple";

	public static final Item GRAPPLE_ITEM = new GrappleItem(new Item.Settings().maxCount(1).maxDamage(768));
	public static final ComponentType<Unit> ACTIVE = ComponentType.<Unit>builder()
		.codec(Unit.CODEC)
		.packetCodec(PacketCodec.unit(Unit.INSTANCE))
		.build();

	public static final ComponentType<Unit> HIT = ComponentType.<Unit>builder()
		.codec(Unit.CODEC)
		.packetCodec(PacketCodec.unit(Unit.INSTANCE))
		.build();

	public static final PacketCodec<PacketByteBuf, Vec3d> VEC_3D_PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.DOUBLE, Vec3d::getX,
		PacketCodecs.DOUBLE, Vec3d::getY,
		PacketCodecs.DOUBLE, Vec3d::getZ,
		Vec3d::new
	);

	public static final ComponentType<Vec3d> ANCHOR = ComponentType.<Vec3d>builder()
		.codec(Vec3d.CODEC)
		.packetCodec(VEC_3D_PACKET_CODEC)
		.build();

	public static final TagKey<Block> NO_GRAPPLE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MODID, "no_grapple"));

	public static final RegistryKey<Enchantment> RANGE_ENCHANTMENT = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MODID, "range"));
	public static final RegistryKey<Enchantment> ACCELERATION_ENCHANTMENT = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MODID, "acceleration"));
	public static final RegistryKey<Enchantment> MAX_SPEED_ENCHANTMENT = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MODID, "max_speed"));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, Identifier.of(MODID, "grappling_hook"), GRAPPLE_ITEM);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MODID, "active"), ACTIVE);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MODID, "hit"), HIT);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MODID, "anchor"), ANCHOR);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(GRAPPLE_ITEM);
		});
	}

}
