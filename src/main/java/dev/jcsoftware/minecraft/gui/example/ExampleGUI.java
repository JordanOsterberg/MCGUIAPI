package dev.jcsoftware.minecraft.gui.example;

import dev.jcsoftware.minecraft.gui.object.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

// JavaPlugin should be the name of your Main class
// This will allow you to access *your* plugin class directly from the GUI
public class ExampleGUI extends GUI<JavaPlugin> {

	private int clicks = 1;

	public ExampleGUI(JavaPlugin plugin) {
		super(plugin);

		createInventory();
	}

	public void createInventory() {
		setItem(0, new ItemStack(Material.ARROW), (player, clickedItem) -> {
			clicks--;
			if (clicks <= 0) {
				clicks = 1;
			}

			createInventory();
			return ButtonAction.CANCEL;
		});


		setItem(4, new ItemStack(Material.COAL, clicks), (player, item) -> ButtonAction.CLOSE_GUI);

		setItem(8, new ItemStack(Material.ARROW), (player, item) -> {
			clicks++;
			if (clicks > 64) {
				clicks = 64;
			}

			createInventory();
			return ButtonAction.CANCEL;
		});
	}

	@Override
	public void onClose(Player player, InventoryCloseEvent event) {
		player.sendMessage(ChatColor.RED + "You just closed the example inventory: " + this.getTitle());
	}

	@Override
	public void onOpen(Player player, InventoryOpenEvent event) {
		player.sendMessage(ChatColor.AQUA + "You just opened the example inventory: " + this.getTitle());
	}

	@Override
	public int getSize() {
		return 9;
	}

	@Override
	public String getTitle() {
		return "Test Inventory";
	}

	// While false, the inventory will not be allowed to close
	@Override
	public boolean canClose(Player player) {
		return true;
	}
}
