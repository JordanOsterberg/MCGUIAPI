package dev.jcsoftware.minecraft.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class GUI<T extends JavaPlugin> implements InventoryHolder {
  @Getter private final T plugin;
  @Getter private final Inventory inventory;

  private final Map<Integer, GUIItem> itemPositionMap = new HashMap<>();

  public GUI(T plugin) {
    this.plugin = plugin;
    this.inventory = Bukkit.createInventory(this, getSize(), getTitle());
  }

  public abstract int getSize();
  public abstract String getTitle();
  public abstract boolean canClose(Player player);

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
    if (item != null && item.getItemStack() != null) {
      itemPositionMap.put(index, item);
    }

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
    ButtonAction onClick(Player whoClicked, ItemStack clickedItem);
  }

  public enum ButtonAction {
    CLOSE_GUI {
      @Override
      public void run(GUI<?> gui, InventoryClickEvent event) {
        if (gui.canClose((Player) event.getWhoClicked())) event.getWhoClicked().closeInventory();
      }
    },
    CANCEL {
      @Override
      public void run(GUI<?> gui, InventoryClickEvent event) {
        event.setCancelled(true);
      }
    },
    NONE {
      @Override public void run(GUI<?> gui, InventoryClickEvent event) {}
    };

    public abstract void run(GUI<?> gui, InventoryClickEvent event);
  }

  public void open(Player player) {
    generate();
    player.openInventory(inventory);
  }

  public void handleOnClick(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) return;
    if (!event.getClickedInventory().equals(inventory)) return;
    if (!(event.getWhoClicked() instanceof Player)) return;

    int index = event.getSlot();
    GUIItem item = itemPositionMap.get(index);

    if (item == null) return;
    if (item.getOnClick() == null) {
      event.setCancelled(true);
      return;
    }

    Player player = (Player) event.getWhoClicked();

    ButtonAction result = item.getOnClick().onClick(player, event.getCurrentItem());
    result.run(this, event);
  }
}
