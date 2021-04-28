package dev.jcsoftware.minecraft.gui.object;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class GUI<T extends JavaPlugin> implements InventoryHolder {

	@Getter
	private final T plugin;
	@Getter
	private final Inventory inventory;

	private final Map<Integer, GUIItem> itemPositionMap = new HashMap<>();

	/**
	 * Instantiate a new GUI implementing the bukkit {@link InventoryHolder}
	 *
	 * @param plugin the source plugin extending {@link JavaPlugin}
	 */
	public GUI(T plugin) {
		this.plugin = plugin;
		this.inventory = Bukkit.createInventory(this, getSize(), getTitle());
	}

	/**
	 * Generate all the items by setting them to their corresponding item slots
	 */
	protected void generate() {
		itemPositionMap.forEach((index, item) -> inventory.setItem(index, item.getItemStack()));
	}

	/**
	 * Set a new {@link GUIItem} into the GUI based on an item slot
	 *
	 * @param slot the slot to put the item in
	 * @param item the item to set into the GUI
	 */
	private void setItem(int slot, GUIItem item) {
		if (slot >= getSize()) {
			throw new IllegalArgumentException("Invalid slot " + slot + " for inventory of size " + getSize() + " [" + getClass().getName() + "]");
		}

		itemPositionMap.remove(slot);
		if (item != null && item.getItemStack() != null) {
			itemPositionMap.put(slot, item);
		}

		generate();
	}

	/**
	 * Set a new {@link ItemStack} into the GUI based on an item slot
	 *
	 * @param slot      the slot to put the item in
	 * @param itemStack the item to set into the GUI
	 * @see GUI#setItem(int, GUIItem)
	 */
	protected void setItem(int slot, ItemStack itemStack) {
		setItem(slot, new GUIItem(null, itemStack));
	}

	/**
	 * Set a new {@link ItemStack} with it's own {@link ButtonCompletion} action into the GUI based on an item slot
	 *
	 * @param slot             the slot to put the item in
	 * @param itemStack        the item to set into the GUI
	 * @param buttonCompletion the button completion object used for the click action
	 * @see GUI#setItem(int, GUIItem)
	 */
	protected void setItem(int slot, ItemStack itemStack, ButtonCompletion buttonCompletion) {
		setItem(slot, new GUIItem(buttonCompletion, itemStack));
	}

	/**
	 * Open the actual bukkit {@link Inventory} itself
	 *
	 * @param player the player opening the inventory
	 */
	public void open(Player player) {
		generate();
		player.openInventory(inventory);
	}

	/**
	 * Get an {@link GUIItem} by by it's slot position
	 *
	 * @param slot the slot of the GUIItem
	 * @return The optional GUIItem, if not present, it will return {@link Optional#empty()}
	 */
	public Optional<GUIItem> getGUIItem(int slot) {
		return Optional.ofNullable(itemPositionMap.get(slot));
	}

	/**
	 * @return the size of the inventory
	 */
	public abstract int getSize();

	/**
	 * @return the title of the inventory
	 */
	public abstract String getTitle();

	/**
	 * Check if the player is permitted to close the GUI
	 *
	 * @param player the player closing the GUI
	 * @return <code>true</code> if the player can close the GUI, <code>false</code> otherwise
	 */
	public boolean canClose(Player player) {
		return true;
	}

	/**
	 * Called when the player closes the GUI
	 *
	 * @param player the player closing the GUI
	 * @param event  the bukkit {@link InventoryCloseEvent} for more accessibility
	 */
	public void onClose(Player player, InventoryCloseEvent event) {
	}

	/**
	 * Called when the player opens the GUI
	 *
	 * @param player the player opening the GUI
	 * @param event  the bukkit {@link InventoryOpenEvent} for more accessibility
	 */
	public void onOpen(Player player, InventoryOpenEvent event) {
	}

	public enum ButtonAction {
		CLOSE_GUI,
		CANCEL
	}

	public interface ButtonCompletion {
		ButtonAction onClick(Player whoClicked, ItemStack clickedItem);
	}
}
