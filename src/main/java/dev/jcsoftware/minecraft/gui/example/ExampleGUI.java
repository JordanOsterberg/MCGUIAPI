package dev.jcsoftware.minecraft.gui.example;

import dev.jcsoftware.minecraft.gui.GUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

  // You don't need this in a separate method
  // I use one so that I can call it again and the amount of coal in the middle slot changes
  public void createInventory() {
    set(0, new ItemStack(Material.ARROW), (player, clickedItem) -> {
      clicks--;
      if (clicks <= 0) {
        clicks = 1;
      }

      createInventory();
      return ButtonAction.CANCEL;
    });

    set(4, new ItemStack(Material.COAL, clicks));

    set(8, new ItemStack(Material.ARROW), (player, item) -> {
      clicks++;
      if (clicks > 64) {
        clicks = 64;
      }

      createInventory();
      return ButtonAction.CANCEL;
    });
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
