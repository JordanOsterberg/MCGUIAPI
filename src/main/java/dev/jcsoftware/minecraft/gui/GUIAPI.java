package dev.jcsoftware.minecraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIAPI<T extends JavaPlugin> implements Listener {
  private final T plugin;
  private final Map<UUID, GUI<T>> playerToGUIMap = new HashMap<>();

  public GUIAPI(T plugin) {
    this.plugin = plugin;

    if (!plugin.isEnabled()) {
      throw new IllegalStateException("Your plugin must be initialized before instantiating an instance of GUIAPI.");
    }

    plugin.getServer()
        .getPluginManager()
        .registerEvents(this, plugin);
  }

  public void openGUI(Player player, GUI<T> gui) {
    gui.open(player);
    playerToGUIMap.put(player.getUniqueId(), gui);
  }

  public GUI<T> getOpenGUI(Player player) {
    return playerToGUIMap.get(player.getUniqueId());
  }

  @EventHandler
  private void onClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    GUI<T> open = getOpenGUI((Player) event.getWhoClicked());
    open.handleOnClick(event);
  }

  @EventHandler
  private void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player)) return;
    Player player = (Player) event.getPlayer();
    GUI<T> openGUI = getOpenGUI(player);
    if (openGUI == null) return;
    if (!openGUI.canClose(player)) {
      // Delay task to prevent overflow
      Bukkit.getServer()
          .getScheduler()
          .runTaskLater(plugin, () -> {
            openGUI.open(player);
          }, 10);
      return;
    }

    openGUI.onClose(player);
    playerToGUIMap.remove(player.getUniqueId());
  }
}
