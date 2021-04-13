package dev.jcsoftware.minecraft.gui.example;

import dev.jcsoftware.minecraft.gui.GUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleGUI extends GUI<JavaPlugin> {
  public ExampleGUI(JavaPlugin plugin) {
    super(plugin);

    set(5, new ItemStack(Material.REDSTONE), () -> {
      System.out.println("Redstone clicked!");
      return ButtonAction.CANCEL;
    });

    set(2, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
  }

  @Override
  public int getSize() {
    return 27;
  }

  @Override
  public String getTitle() {
    return "Example GUI";
  }
}
