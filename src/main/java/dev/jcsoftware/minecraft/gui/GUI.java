package dev.jcsoftware.minecraft.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class GUI<T extends JavaPlugin> implements InventoryHolder, Listener {
  @Getter private final T plugin;
  @Getter private final Inventory inventory;

  private final Map<Integer, GUIItem> itemPositionMap = new HashMap<>();

  public GUI(T plugin) {
    this.plugin = plugin;
    this.inventory = Bukkit.createInventory(this, getSize(), getTitle());

    plugin.getServer()
        .getPluginManager()
        .registerEvents(this, plugin);
  }

  public abstract int getSize();
  public abstract String getTitle();

  public void onClose(Player player) {}

  protected void generate() {
    itemPositionMap.forEach((index, item) -> {
      inventory.setItem(index, item.getItemStack());
    });
  }

  private void set(int index, GUIItem item) {
    if (index >= getSize()) {
      throw new IllegalArgumentException("Invalid index " + index + " for inventory of size " + getSize() + " [" + getClass().getName() + "]");
    }

    itemPositionMap.remove(index);
    itemPositionMap.put(index, item);
    generate();
  }

  protected void set(int index, ItemStack itemStack) {
    set(index, new GUIItem(null, itemStack));
  }

  protected void set(int index, ItemStack itemStack, ButtonCompletion onClick) {
    set(index, new GUIItem(onClick, itemStack));
  }

  protected void clear() {
    inventory.clear();
  }

  public interface ButtonCompletion {
    ButtonAction onClick();
  }

  public enum ButtonAction {
    CLOSE_GUI,
    CANCEL,
    ALLOW
  }

  public void open(Player player) {
    generate();
    player.openInventory(inventory);
  }

  @EventHandler
  private void onClick(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) return;
    if (!event.getClickedInventory().equals(inventory)) return;

    int index = event.getSlot();
    GUIItem item = itemPositionMap.get(index);

    if (item == null) return;
    if (item.getOnClick() == null) {
      event.setCancelled(true);
      return;
    }

    ButtonAction result = item.getOnClick().onClick();
    switch (result) {
      case CANCEL:
        event.setCancelled(true);
        break;
      case CLOSE_GUI:
        event.getWhoClicked().closeInventory();
        break;
    }
  }

  @EventHandler
  private void onInventoryClose(InventoryCloseEvent event) {
    if (!event.getInventory().equals(inventory)) return;
    if (!(event.getPlayer() instanceof Player)) return;
    onClose((Player) event.getPlayer());
  }
}
