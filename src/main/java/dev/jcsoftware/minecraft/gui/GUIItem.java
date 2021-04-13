package dev.jcsoftware.minecraft.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Getter
public class GUIItem {
  private final GUI.ButtonCompletion onClick;
  private final ItemStack itemStack;
}
