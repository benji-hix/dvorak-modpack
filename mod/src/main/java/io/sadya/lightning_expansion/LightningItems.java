package io.sadya.lightning_expansion;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.spell_engine.api.spell.container.SpellContainers;
import net.spell_engine.rpg_series.config.ArmorSetConfig;
import net.spell_engine.rpg_series.config.AttributeModifier;
import net.spell_engine.rpg_series.config.WeaponConfig;
import net.spell_engine.rpg_series.item.Armor;
import net.spell_engine.rpg_series.item.Equipment;
import net.spell_engine.rpg_series.item.Weapon;
import net.spell_engine.rpg_series.item.Weapons;
import net.spell_power.api.SpellPowerMechanics;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Six items on Spell Engine's RPG Series equipment API, the same layer Wizards 3.1.1 uses.
 * Numbers copy Wizards' tier-2 sets (0.25 school spell power per robe piece, one secondary stat)
 * so lightning sits beside arcane, fire and frost as a parallel school, not a stronger one.
 */
public final class LightningItems {
	private LightningItems() { }

	private static final String NS = LightningExpansion.ID;
	public static final ResourceKey<CreativeModeTab> GROUP_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(NS, "general"));

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(NS, path);
	}

	// MARK: Weapons

	public static final ArrayList<Weapon.Entry> weapons = new ArrayList<>();
	private static Weapon.Entry weapon(Weapon.Entry entry) {
		weapons.add(entry);
		return entry;
	}

	public static final Weapon.Entry wand = weapon(Weapons.damageWand(
			NS, "wand_lightning",
			Equipment.Tier.TIER_2, () -> Ingredient.of(Items.COPPER_INGOT),
			List.of(SpellSchools.LIGHTNING.id))
			.spellContainer(SpellContainers.forMagicWeapon().withSpellId(id("spark_bolt")))
			.translatedName("Lightning Wand")
	);
	public static final Weapon.Entry staff = weapon(Weapons.damageStaff(
			NS, "staff_lightning",
			Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT),
			List.of(SpellSchools.LIGHTNING.id))
			.spellContainer(SpellContainers.forMagicWeapon().withSpellId(id("shock")))
			.translatedName("Lightning Staff")
	);

	// MARK: Robes

	/** Item class per set, as Wizards does; Armor.CustomItem carries the config-driven attributes. */
	public static class LightningRobe extends Armor.CustomItem {
		public LightningRobe(Holder<ArmorMaterial> material, ArmorItem.Type slot, Properties settings) {
			super(material, slot, settings);
		}
	}

	private static final ArrayList<Armor.Entry> armors = new ArrayList<>();

	// Layer id "lightning_robe" resolves to textures/models/armor/lightning_robe_layer_{1,2}.png:
	// vanilla armor rendering, no geo model, no armor_model_api (plan 4e).
	public static final Holder<ArmorMaterial> robeMaterial = Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL, id("lightning_robe"),
			new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 1,
							ArmorItem.Type.CHESTPLATE, 3,
							ArmorItem.Type.LEGGINGS, 2,
							ArmorItem.Type.BOOTS, 1),
					10,
					SoundEvents.ARMOR_EQUIP_LEATHER,
					() -> Ingredient.of(Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.CYAN_WOOL),
					List.of(new ArmorMaterial.Layer(id("lightning_robe"))),
					0, 0));

	private static final float SPELL_POWER_T2 = 0.25F;
	private static final float CRIT_CHANCE_T2 = 0.02F;

	private static ArmorSetConfig.Piece piece(int armor) {
		return new ArmorSetConfig.Piece(armor).addAll(List.of(
				AttributeModifier.multiply(SpellSchools.LIGHTNING.id, SPELL_POWER_T2),
				AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, CRIT_CHANCE_T2)
		));
	}

	public static final Armor.Set robeSet;
	static {
		var entry = Armor.Entry.create(
				robeMaterial,
				id("lightning_robe"),
				20,
				LightningRobe::new,
				ArmorSetConfig.with(piece(1), piece(3), piece(2), piece(1)),
				Equipment.LootProperties.of(2)
		).translatedName("Lightning Hat", "Lightning Robe Top", "Lightning Robe Bottom", "Lightning Boots");
		armors.add(entry);
		robeSet = entry.armorSet();
	}

	// MARK: Registration

	public static void register(Map<String, WeaponConfig> weaponConfigs, Map<String, ArmorSetConfig> armorConfigs) {
		Weapon.register(weaponConfigs, weapons, GROUP_KEY);
		Armor.register(armorConfigs, armors, GROUP_KEY);
	}
}
