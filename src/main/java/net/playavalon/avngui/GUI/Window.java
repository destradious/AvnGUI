package net.playavalon.avngui.GUI;

import net.playavalon.avngui.GUI.Buttons.Button;
import net.playavalon.avngui.Utility.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

import static net.playavalon.avngui.AvnAPI.plugin;

public class Window implements Listener {

    private final String namespace;
    private final int size;
    private String display;
    private boolean cancelClick = true;

    private WindowGroup group;

    private final HashMap<Player, GUIInventory> inventories;
    private final HashMap<Integer, Button> buttons;

    /**
     * Create a GUI window belonging to a window group
     * @param namespace The namespaced ID of this GUI window
     * @param size How many slots this window has (multiple of 9)
     * @param displayname The label that appears on the top of the window
     */
    public Window(String namespace, int size, String displayname) {
        this.namespace = namespace;
        this.size = size;
        this.display = StringUtils.fullColor(displayname);

        inventories = new HashMap<>();
        buttons = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        WindowManager.put(this);
    }

    /**
     * Create a GUI window belonging to a window group
     * @param namespace The namespaced ID of this GUI window
     * @param size How many slots this window has (multiple of 9)
     * @param displayname The label that appears on the top of the window
     * @param group The group this window belongs to
     */
    public Window(String namespace, int size, String displayname, WindowGroup group) {
        this.namespace = namespace;
        this.size = size;
        this.display = StringUtils.fullColor(displayname);

        inventories = new HashMap<>();
        buttons = new HashMap<>();
        this.group = group;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        WindowManager.put(this);
    }


    /**
     * Sets whether or not to cancel inventory clicks when the player clicks
     * @param cancel "true" by default, false to allow inventory clicks
     */
    public final void setCancelClick(boolean cancel) {
        this.cancelClick = cancel;
    }


    /**
     * Add a button to this GUI window
     * @param slot The inventory slot (starting from 0) to place this button
     * @param button The button to place
     */
    public final void addButton(int slot, Button button) {
        if (slot >= size) {
            // TODO Error message of slot being higher than inventory slots available
            return;
        }

        buttons.put(slot, button);
    }

    /**
     * Remove a button from this GUI window
     * @param slot The inventory slot location of the button we're removing
     */
    public final void removeButton(int slot) {
        buttons.remove(slot);
    }

    /**
     * Set the label at the top of the GUI window
     * @param label The label of this GUI window. Works with colour codes.
     */
    public final void setLabel(String label) {
        display = StringUtils.fullColor(label);
    }

    /**
     * Get the label of this GUI window
     * @return The coloured label of the GUI window
     */
    public final String getLabel() {
        return display;
    }


    /**
     * Get the namespaced ID of this GUI window
     * @return The namespaced ID of this GUI window
     */
    public final String getName() {
        return namespace;
    }

    /**
     * Get the inventory size of this GUI window
     * @return The GUI window size
     */
    public final int getSize() {
        return size;
    }


    /**
     * Opens this GUI window for a player
     * @param player The player to open this GUI window for
     */
    public final void open(Player player) {
        GUIInventory gui;
        if (inventories.containsKey(player)) {
            gui = inventories.get(player);
        } else {
            gui = new GUIInventory(Bukkit.createInventory(player, size, display));
            inventories.put(player, gui);
        }

        Inventory inv = gui.getInv();

        for (Map.Entry<Integer, Button> button : buttons.entrySet()) {
            inv.setItem(button.getKey(), button.getValue().getItem());
        }

        player.openInventory(inv);
    }

    /**
     * Move to the next GUI window if this window is part of a group
     * @param player The player we are switching windows of
     */
    public final void next(Player player) {
        if (group == null) return;
        group.next(player);
    }

    /**
     * Move to the previous GUI window if this window is part of a group
     * @param player The player we are switching windows of
     */
    public final void previous(Player player) {
        if (group == null) return;
        group.previous(player);
    }


    @EventHandler
    protected final void onClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        GUIInventory gui = inventories.get(player);
        if (gui == null) return;
        if (event.getClickedInventory() != gui.getInv()) return;

        if (cancelClick) event.setCancelled(true);

        int slot = event.getRawSlot();
        Button button = buttons.get(slot);
        if (button == null) return;

        button.click(event, this);

    }

}