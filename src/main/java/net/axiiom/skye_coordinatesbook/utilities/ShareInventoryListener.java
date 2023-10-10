package net.axiiom.skye_coordinatesbook.utilities;

import net.axiiom.skye_coordinatesbook.Main.CoordinatesBookPlugin;
import net.axiiom.skye_coordinatesbook.features.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;

/*
    Class that listens to player input on the inventory - used to share coordinates
 */
public class ShareInventoryListener implements Listener {
    private ArrayList<Integer> userIndexes;
    private CoordinatesBookPlugin plugin;

    public ShareInventoryListener(CoordinatesBookPlugin _plugin) {
        this.plugin = _plugin;
        this.userIndexes = new ArrayList(Arrays.asList(new Integer[]{0,1,2,3,9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39,45,46,47,48}));
    }

    // Runs when an item is clicked on in the player's inventory. Only runs functionality when the clicked inventory
    // is generated by the plugin
    @EventHandler
    public void onClickInventory(InventoryClickEvent _event) {
        if(_event.getView().getTitle().equalsIgnoreCase("Share Your Coordinate") && _event.getCurrentItem() != null) {
            // stops the default action
            _event.setCancelled(true);

            ItemStack item = _event.getCurrentItem();
            Inventory inventory = _event.getClickedInventory();
            Player player = (Player) _event.getWhoClicked();

            if(!inventory.getType().equals(InventoryType.PLAYER))
            {
                if(item.getType().equals(Material.PLAYER_HEAD)) {
                    int openSlot = (userIndexes.contains(_event.getSlot())) ?
                            getFirstOpenSlot(inventory, false) :
                            getFirstOpenSlot(inventory, true);

                    if(openSlot != -1) {
                        inventory.setItem(_event.getSlot(), new ItemStack(Material.AIR));
                        inventory.setItem(openSlot, item);
                    }
                }

                if(item.getType().equals(Material.LIME_CONCRETE)) {

                    //creates coordinate from NBT data found in item | see CommandExecutor
                    Coordinate coordinate = getCoordFromItem(inventory.getItem(49));

                    for(int i : userIndexes) {
                        ItemStack thisItem = inventory.getItem(i);
                        if(thisItem != null && thisItem.getType().equals(Material.PLAYER_HEAD)) {
                            SkullMeta skullMeta = (SkullMeta) thisItem.getItemMeta();
                            String targetPlayerName = skullMeta.getOwningPlayer().getName();

                            if(checkPlayerExists(player, targetPlayerName)) {
                                //send message to player to share
                                shareCoordinate(coordinate, Bukkit.getPlayer(targetPlayerName), player);
                            }
                        }
                    }

                    player.closeInventory();
                }
            }


        }
    }

    /* Shares coordinate with a player in the message format.
        '<player_name> has sent you their coordinate:
        <coordinate_name> : (<x> <y> <z>)
        Add to your coordinate book? yes / no'

       The player simply clicks on "yes" or "no" to accept or deny the request
    */
    private boolean shareCoordinate(Coordinate _coordinate, Player _targetPlayer, Player _sender) {
        plugin.commandExecutor.awaitingShareResponse.put(_targetPlayer.getUniqueId(), _coordinate);
        Location location = _coordinate.getLocation();
        String coords = String.format("%s %s %s", location.getBlockX(), location.getBlockY(), location.getBlockZ());


        // i generated this from a website
        String command = "tellraw " + _targetPlayer.getName() + " [\"\",{\"text\":\"" + _sender.getName() +
                "\",\"color\":\"aqua\"},{\"text\":\" has sent you their coordinate: \",\"color\":\"none\"" +
                ",\"bold\":false},{\"text\":\"" + coords + "\",\"color\":\"dark_aqua\"},{\"text\":\"\\nAdd" +
                " to your coordinate book? \",\"color\":\"none\"},{\"text\":\"yes\",\"color\":\"green\"," +
                "\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/receiveCoordinate\"}" +
                ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" +
                "Add coordinate to your book?\",\"color\":\"aqua\"}]}}},{\"text\":\" / \",\"color\":\"none\"," +
                "\"bold\":false},{\"text\":\"no\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"" +
                "run_command\",\"value\":\"/denyCoordinate\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\"" +
                ":{\"text\":\"\",\"extra\":[{\"text\":\"Ignore this request\"}]}}}]";

        Bukkit.dispatchCommand(_sender.getServer().getConsoleSender(), command);
        return true;
    }

    // Gets the coordinate from the NBT Tags assigned to the item
    private Coordinate getCoordFromItem(ItemStack _book){
        String[] coordsArray= NBTWrapper.getNBTTag("coords", _book).split(" ");

        ArrayList<Integer> coords = new ArrayList<>();
        for (String s : coordsArray)
            coords.add(Integer.parseInt(s));

        int x = coords.get(0);
        int y = coords.get(1);
        int z = coords.get(2);
        String worldName = NBTWrapper.getNBTTag("worldName",_book);

        return new Coordinate(x,y,z,worldName,"_received_");
    }

    // Gets the first open slot in the player's currently open inventory
    private int getFirstOpenSlot(Inventory _inventory, boolean _userIndexes) {

        if(_userIndexes) {
            for(int index : userIndexes) {
                ItemStack thisItem = _inventory.getItem(index);
                if(thisItem == null || thisItem.getType().equals(Material.AIR)) {
                    return index;
                }
            }
        } else {
            for(int i = 0; i < 54; i++) {
                ItemStack thisItem = _inventory.getItem(i);
                if(!userIndexes.contains(i) && (thisItem == null || thisItem.getType().equals(Material.AIR)) ) {
                    return i;
                }
            }
        }

        return -1;
    }

    // Validates a player with the given name exists
    private boolean checkPlayerExists(Player _player, String _target) {
        for(Player p :_player.getServer().getOnlinePlayers()) {
            if(p.getName().equalsIgnoreCase(_target)) {
                return true;
            }
        }

        return false;
    }
}
