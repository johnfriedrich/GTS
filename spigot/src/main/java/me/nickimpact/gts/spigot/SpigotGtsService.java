package me.nickimpact.gts.spigot;

import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.AllArgsConstructor;
import lombok.Setter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.text.TextService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiFunction;

@Setter
public class SpigotGtsService implements GtsService<CommandSender, String> {

	private final IGTSPlugin plugin;

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;
	private SpigotTextService textService;

	public SpigotGtsService(IGTSPlugin plugin) {
		this.plugin = plugin;
		this.textService = new SpigotTextService();
	}

	@Override
	public ListingManager getListingManager() {
		return this.manager;
	}

	@Override
	public IGtsStorage getStorage() {
		return this.storage;
	}

	@Override
	public EntryRegistry getEntryRegistry() {
		return this.registry;
	}

	@Override
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<CommandSender, String[], CommandResults> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			this.registry.getClassifications().add(new SpigotEntryClassification(entry, identifier, rep, ui, cmd));

			plugin.getPluginLogger().info("Loaded element type: " + entry.getSimpleName());
		} catch (Exception e) {
			plugin.getPluginLogger().info("Failed to register type (" + entry.getSimpleName() + ") with reason: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public BuilderRegistry getBuilderRegistry() {
		return this.builders;
	}

	@Override
	public SpigotTextService getTextService() {
		return this.textService;
	}

	public static class SpigotEntryClassification extends EntryClassification<CommandSender> {
		SpigotEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, BiFunction<CommandSender, String[], CommandResults> cmdHandler) {
			super(classification, identifers, itemRep, ui, cmdHandler);
		}
	}

	public static class SpigotTextService implements TextService<String> {

		@Override
		public String getPrefix() {
			return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "\u00bb ";
		}

		@Override
		public String getErrorPrefix() {
			return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "(" + ChatColor.RED + "Error" + ChatColor.GRAY + ") ";
		}

	}
}
