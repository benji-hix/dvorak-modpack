package io.sadya.lightning_expansion;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.tiny_config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightningExpansion implements ModInitializer {
	public static final String ID = "lightning_expansion";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	// Same mechanism wizards uses: item stats live in config/lightning_expansion/equipment_v2.json,
	// seeded from the defaults registered in code on first run.
	public static final ConfigManager<ConfigFile.Equipment> equipmentConfig = new ConfigManager<>
			("equipment_v2", new ConfigFile.Equipment())
			.builder()
			.setDirectory(ID)
			.sanitize(true)
			.build();

	@Override
	public void onInitialize() {
		equipmentConfig.refresh();

		var group = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
				.icon(() -> new ItemStack(LightningItems.staff.item()))
				.title(Component.translatable("itemGroup.lightning_expansion.general"))
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LightningItems.GROUP_KEY, group);

		LightningItems.register(equipmentConfig.value.weapons, equipmentConfig.value.armor_sets);
		equipmentConfig.save();
		LOGGER.info("Lightning Expansion: {} weapons, {} armor pieces registered",
				LightningItems.weapons.size(), LightningItems.robeSet.pieces().size());
	}
}
