package dev.jcsoftware.minecraft.gui;

import dev.jcsoftware.minecraft.gui.object.GUI;
import dev.jcsoftware.minecraft.gui.object.GUIItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * MCGUIAPI is a Minecraft Spigot Plugin tool which simplifies the process of showing a player a GUI with clickable elements.
 * <p>
 * <p>
 * GUIAPI initialization example:
 *
 * <pre>
 *     {@code
 *     public class YourPlugin extends JavaPlugin {
 *         private GUIAPI<YourPlugin> guiAPI;
 *
 * 	       public void onEnable() {
 *             guiAPI = new GUIAPI<>(this);
 *         }
 *     }
 *     }
 * </pre>
 * <p>
 * You can open a GUI like so:
 *
 * <pre>
 *     {@code
 *     GUIAPI#openGUI(Player, GUI<YourPlugin>);
 *     }
 * </pre>
 */
public class GUIAPI<T extends JavaPlugin> implements Listener {

	private final T plugin;
	private final Map<UUID, GUI<T>> playerToGUIMap = new HashMap<>();

	/**
	 * Instantiate a new GUI API instance for your plugin
	 * NOTE: Keep in mind to have one API instance running at the time...
	 *
	 * @param plugin the source plugin extending {@link JavaPlugin}
	 */
	public GUIAPI(T plugin) {
		this.plugin = plugin;

		if (!plugin.isEnabled()) {
			throw new IllegalStateException("Your plugin must be initialized before instantiating an instance of GUIAPI.");
		}

		plugin.getServer().getPluginManager().registerEvents(new GUIListener<>(this), plugin);
	}

	/**
	 * Open a specific GUI for a specific online bukkit {@link Player}
	 *
	 * @param player the player opening the gui
	 * @param gui    the gui to open
	 * @see GUI#open(Player)
	 */
	public void openGUI(Player player, GUI<T> gui) {
		// Put the player's unique id first so InventoryOpenEvent can detect it
		playerToGUIMap.put(player.getUniqueId(), gui);

		// Open the gui
		gui.open(player);
	}

	/**
	 * Get the current open GUI of a player, this might be null hence the {@link Optional}
	 *
	 * @param player the player of the open gui
	 * @return The optional open gui, if not present, it will return {@link Optional#empty()}
	 */
	public Optional<GUI<T>> getOpenGUI(Player player) {
		return Optional.ofNullable(playerToGUIMap.get(player.getUniqueId()));
	}

	/**
	 * Check if the specified player has an GUI opened, if true, accept the {@link Consumer}
	 *
	 * @param player        the player of the open gui
	 * @param openGUIAction the gui action executed
	 * @return <code>true</code> if the player has an GUI opened, <code>false</code> otherwise
	 *
	 * @see GUIAPI#getOpenGUI(Player)
	 */
	public boolean hasOpenGUI(Player player, Consumer<GUI<T>> openGUIAction) {
		AtomicBoolean hasOpenGUI = new AtomicBoolean(false);

		Optional<GUI<T>> openGUI = this.getOpenGUI(player);
		openGUI.ifPresent(gui -> {
			hasOpenGUI.set(true);
			openGUIAction.accept(gui);
		});

		return hasOpenGUI.get();
	}

	/**
	 * The GUIListener class is used to register clicks and handle opening/closing events
	 *
	 * @param <T> the source plugin extending {@link JavaPlugin}
	 */
	public static final class GUIListener<T extends JavaPlugin> implements Listener {

		private final GUIAPI<T> api;

		public GUIListener(GUIAPI<T> api) {
			this.api = api;
		}

		@EventHandler
		public void onClick(InventoryClickEvent event) {
			if (event.getClickedInventory() == null) return;
			if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof GUI)) return;
			if (event.getCurrentItem() == null) return;

			Player player = (Player) event.getWhoClicked();
			Optional<GUI<T>> openGUI = api.getOpenGUI(player);
			openGUI.ifPresent(gui -> {
				// Cancel the click
				event.setCancelled(true);

				// Check if there is a valid item, if present, continue click action
				Optional<GUIItem> optionalItem = gui.getGUIItem(event.getSlot());
				optionalItem.ifPresent(item -> {
					if (item.getOnClick() == null) return;

					// Execute the click action and check for a close gui action
					GUI.ButtonAction result = item.getOnClick().onClick(player, event.getCurrentItem());
					if (result == GUI.ButtonAction.CLOSE_GUI && gui.canClose(player)) {
						player.closeInventory();
					}
				});
			});
		}

		@EventHandler
		private void onInventoryOpen(InventoryOpenEvent event) {
			if (!(event.getInventory().getHolder() instanceof GUI)) return;
			Player player = (Player) event.getPlayer();

			Optional<GUI<T>> openGUI = api.getOpenGUI(player);
			openGUI.ifPresent(gui -> gui.onOpen(player, event));
		}

		@EventHandler
		private void onInventoryClose(InventoryCloseEvent event) {
			InventoryHolder inventoryHolder = event.getInventory().getHolder();

			if (!(inventoryHolder instanceof GUI)) return;
			Player player = (Player) event.getPlayer();

			Optional<GUI<T>> openGUI = api.getOpenGUI(player);
			openGUI.ifPresent(gui -> {
				if (!gui.canClose(player)) {
					Bukkit.getServer().getScheduler().runTaskLater(api.plugin, () -> gui.open(player), 1L);
					return;
				}

				gui.onClose(player, event);
				api.playerToGUIMap.remove(player.getUniqueId(), gui);
			});
		}
	}
}
