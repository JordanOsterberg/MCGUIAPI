# MCGUIAPI
MCGUIAPI is a Minecraft Spigot Plugin tool which simplifies the process of showing a player a GUI with clickable elements.

## How to Install
You can use JitPack to use this API via Maven or Gradle. 

You could also take all of the classes and add them to your project directly, but it's better practice to use the Maven dependency in the event of major changes.

[![](https://jitpack.io/v/JordanOsterberg/MCGUIAPI.svg)](https://jitpack.io/#JordanOsterberg/JScoreboards)

**Repository**:
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

**Dependency (type latest version in for `version`)**
```
<dependency>
    <groupId>com.github.JordanOsterberg</groupId>
    <artifactId>MCGUIAPI</artifactId>
    <version></version>
</dependency>
```

See [JitPack](https://jitpack.io/#JordanOsterberg/MCGUIAPI) for more information / the most up-to-date versioning if you're having trouble.

### Example
This example GUI has two arrows and a coal item. Clicking on the arrows will increase/decrease the amount of coal items in the inventory.
```java
// YourPlugin should be the name of your Main class
// This will allow you to access *your* plugin class directly from the GUI
public class ExampleGUI extends GUI<YourPlugin> {
  private int clicks = 1;

  public ExampleGUI(YourPlugin plugin) {
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
```

To use in your plugin, create a GUI class.
Then, create an instance of `GUIAPI`.

**Only initialize GUIAPI after your plugin has been enabled.**
```java
public class YourPlugin extends JavaPlugin {
  private GUIAPI<YourPlugin> guiAPI;

  public void onEnable() {
    guiAPI = new GUIAPI(this);
  }
}
```

Once you have a `GUIAPI`, you can open a GUI like so:
```java
guiAPI.openGUI(player, new ExampleGUI(pluginInstance));
```

## License

See [LICENSE.md](LICENSE.md) for license information.