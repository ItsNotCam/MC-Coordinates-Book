package net.axiiom.CoordinatesBook.Main;

import net.axiiom.CoordinatesBook.Coordinate;
import net.axiiom.CoordinatesBook.Utilities.NBT.NBTTag;
import net.axiiom.CoordinatesBook.Utilities.NBT.NBTWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Commands {

	/*
			Renames the coordinate of the player.
			Takes in the old description and the new description and replaces it
	 */
	public static boolean renameCoordinate(CommandExecutor exec, Player _player, String[] _args) {
		exec.getPlugin().getLogger().info("Renaming coordinate " + _args[0]);
		String coordinateUUID = _args[0];
		Commands.giveWritableBook(exec, _player, coordinateUUID);
		return true;
	}

	/*
			This function is run when a player accepts a coordinate share request.

			When a player sends a coordinate to another player, they are presented with a dialog in which they can
			accept the coordinate or deny it. This handles the accept case.
	 */
	public static boolean receiveCoordinate(CommandExecutor exec, Player _player) {
		if(exec.awaitingShareResponse.containsKey(_player.getUniqueId())) {
			Coordinate coord = exec.awaitingShareResponse.get(_player.getUniqueId());
			exec.getPlugin().getCoordinateManager().addCoordinate(_player.getUniqueId(), new Coordinate(
				coord.getLocation(), coord.getName()
			));
			exec.awaitingShareResponse.remove(_player.getUniqueId());

			_player.sendMessage(ChatColor.GREEN + "Saved Coordinate as: " + coord.getName());
			return true;
		} else {
			_player.sendMessage("No coordinate to receive");
		}

		return false;
	}

	/*
			This function is run when a player denies a coordinate share request.

			When a player sends a coordinate to another player, they are presented with a dialog in which they can
			accept the coordinate or deny it. This handles the denial case.
	 */
	public static boolean denyCoordinate(CommandExecutor exec, Player _player) {
		if(exec.awaitingShareResponse.containsKey(_player.getUniqueId())) {
			exec.awaitingShareResponse.remove(_player.getUniqueId());
			_player.sendMessage(ChatColor.RED + "Coordinate denied");
			return true;
		} else {
			_player.sendMessage("No coordinate available");
		}

		return false;
	}


	/*
			This function is run when a player wishes to share a coordinate with another player

			Takes in 5 arguments:
			* UUID to validate the share request
			* The dimension that the coordinate is in (Overworld, Nether, The End)
			* X coordinate component
			* Y coordinate component
			* Z coordinate component
	 */
	public static boolean shareCoordinate(CommandExecutor exec, Player _player, String[] _args) {
		if(_args.length == 1) {
			String coordUUid = _args[0];

			// this is the "large chest" inventory size
			int size = 54;
			Inventory shareInventory = Bukkit.createInventory(null, size, "Share Your Coordinate");

			//fill spacer slots
			ArrayList<Integer> spacerSlots = new ArrayList<>();
			for(int i = 4; i+9 <= size; i+=9) {
				shareInventory.setItem(i, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE));
				spacerSlots.add(i);
			}

			/*
					Fill empty spaces with player heads.
					When a player head is clicked, the coordinate is sent
			 */
			Object[] players = exec.getPlugin().getServer().getOnlinePlayers().toArray();
			ArrayList<Integer> userIndexes = new ArrayList<>(Arrays.asList(0,1,2,3,9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39,45,46,47,48));
			for(int i = 0, playersIndex = 0; i < size && playersIndex < players.length; i++) {
				if(!userIndexes.contains(i) && !spacerSlots.contains(i)) {
					Player player = (Player) players[playersIndex];
					ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

					SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
					if(skullMeta == null) { continue; }
					skullMeta.setOwningPlayer(Bukkit.getPlayer(player.getUniqueId()));
					playerHead.setItemMeta(skullMeta);

					shareInventory.setItem(i,playerHead);
					playersIndex++;
				}
			}

			// Add item containing the coordinate and assign it to inventory slot 49
			ItemStack coordItem = new ItemStack(Material.BOOK);
			{
				NBTWrapper.setNBTTag(new NBTTag("uuid", coordUUid), coordItem);
				ItemMeta meta = coordItem.getItemMeta();
				if(meta != null) {
					String name = "Coordinate";
					try {
						name = exec.getPlugin().getDatabase().getCoordinateFromUUID(_player, coordUUid).getName();
					} catch (SQLException e) {
						_player.sendMessage(ChatColor.RED + "Error: Could not find coordinate");
					}
					meta.setDisplayName(ChatColor.GRAY + name);
					coordItem.setItemMeta(meta);
				}
			}
			shareInventory.setItem(49, coordItem);

			// fill remaining slots with nothing
			for(int i = 0; i < 54; i++) {
				if(shareInventory.getItem(i) == null) {
					shareInventory.setItem(i, new ItemStack(Material.AIR));
				}
			}

			ItemStack send = new ItemStack(Material.LIME_CONCRETE);
			{
				ItemMeta meta = send.getItemMeta();
				if(meta != null) {
					meta.setDisplayName(ChatColor.GREEN + "Confirm");
					send.setItemMeta(meta);
				}
			}

			shareInventory.setItem(22, send);
			_player.openInventory(shareInventory);
		}

		return false;
	}


	/*
			Removes a coordinate from a player's coordinates list

			Takes in 5 arguments:
			* UUID to validate the share request
			* The dimension that the coordinate is in (Overworld, Nether, The End)
			* X coordinate component
			* Y coordinate component
			* Z coordinate component

			Returns a boolean indicating if the book was successfully removed
	 */
	public static boolean removeCoordinate(CommandExecutor exec, Player _player, String[] _args)
	{
		if(_args.length == 1)
		{
			String uuid = _args[0];
			exec.getPlugin().getLogger().info(uuid);
			exec.getPlugin().getCoordinateManager().removeCoordinate(_player, uuid);
//            this.plugin.getCoordinateManager().openBook(_player);
			return true;
		}

		return false;
	}

	/*
			Set the player's compass to point at the coordinate's location
	 */
	public static boolean setCompassTarget(Player _player, String[] _args)
	{
		if(_args.length == 4)
		{
			boolean sameWorld = _player.getWorld().getName().equals(_args[3]);
			if(sameWorld) {
				int x = Integer.parseInt(_args[0]);
				int y = Integer.parseInt(_args[1]);
				int z = Integer.parseInt(_args[2]);
				_player.setCompassTarget(new Location(_player.getWorld(),x,y,z));
				return true;
			}

			_player.sendMessage(ChatColor.RED + "You must be in the same world as that coordinate in order "
				+ "to set your compass target to it.");
		}

		return false;
	}

	/*
			Takes in the current world, location, and inputted description.
			Creates a new coordinate in the database.

			Takes in 1 or more arguments that are used as the name of the coordinate
	 */
	public static boolean createCoordinate(CommandExecutor exec, Player _player, String[] _args)
	{
		if(_args.length < 1) {
			_player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC
				+ "Usage: " + ChatColor.GOLD + "/savecoord <name>"
			);
			return false;
		}

		String name = String.join(" ", _args);

		// Creates a new coordinate based on the player's current location and description
		Coordinate coordinate = new Coordinate(_player.getLocation(), name);
		boolean successful = exec.getPlugin().getCoordinateManager().createCoordinate(_player.getUniqueId(), coordinate);

		if(successful) {
			_player.sendMessage(ChatColor.GREEN + "Saved new coordinate!\n"
				+ ChatColor.GRAY + "" + ChatColor.ITALIC + "Use: " + ChatColor.AQUA + "/coords"
				+ ChatColor.GRAY + "" + ChatColor.ITALIC + " to view your saved coordinates");
		} else {
			_player.sendMessage(ChatColor.RED + "Failed to create new coordinate");
		}

		return successful;
	}

	/*
			Teleports the player to the coordinate's location

			Takes in 5 arguments:
			* UUID to validate the share request
			* The dimension that the coordinate is in (Overworld, Nether, The End)
			* X coordinate component
			* Y coordinate component
			* Z coordinate component
	 */
	public static boolean fastTravel(CommandExecutor exec, Player _player, String[] _args)
	{
		if(_args.length != 4) {
			return false;
		}

		World world = exec.getPlugin().getServer().getWorld(_args[3]);
		int x = Integer.parseInt(_args[0]);
		int y = Integer.parseInt(_args[1]);
		int z = Integer.parseInt(_args[2]);
		Location tpLocation = new Location(world, x, y, z);

		_player.teleport(tpLocation);
		return true;
	}

	/*
			Generates a book, fills it with the coordinates that of the player, and forecfully opens
			it on the client side
	 */
	public static boolean openBook(CommandExecutor exec, Player _player) {
		final boolean opened = exec.getPlugin().getCoordinateManager().openBook(_player);
		if(!opened) {
			_player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "First save a " +
				"location with" + ChatColor.GOLD + " /savecoord ");
		}

		return opened;
	}

	public static void giveWritableBook(CommandExecutor exec, Player _player, String coordinateUUID) {
		String coordinateName = exec.getPlugin().getCoordinateManager().getCoordinateByUUID(_player, coordinateUUID).getName();

		// Create a writable book item stack
		ItemStack book = new ItemStack(Material.WRITABLE_BOOK);

		// Set NBT data or other necessary tags here
		NBTWrapper.setNBTTag(new NBTTag("coordinateUUID", coordinateUUID), book);
		NBTWrapper.setNBTTag(new NBTTag("signingPlayerUUID", coordinateUUID), book);
		NBTWrapper.setNBTTag(new NBTTag("coordinateName", coordinateName), book);

		// Get the BookMeta and add pages
		BookMeta meta = (BookMeta) book.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(ChatColor.ITALIC + "" + ChatColor.BLUE + "Rename coordinate '" + coordinateName + "'");
			meta.addPage("" +
				"You are renaming '" + ChatColor.BLUE + coordinateName + ChatColor.BLACK + "'.\n\n" +
				"Set its name by signing the title of this book.\n" +
				"1. Click " + ChatColor.BLUE + "'Sign'" + ChatColor.BLACK + "\n" +
				"2. Type a new name.\n" +
				"3. Click " + ChatColor.BLUE + "'Sign and Close'"
			);
			book.setItemMeta(meta);
		}

		// Get the player's inventory and the currently held item
		Inventory inventory = _player.getInventory();
		int slot = _player.getInventory().getHeldItemSlot();
		ItemStack heldItem = inventory.getItem(slot);

		// Check if the player's inventory has room for the currently held item
		if (heldItem != null && !heldItem.getType().equals(Material.AIR)) {
			int emptySlot = inventory.firstEmpty();
			if (emptySlot == -1) {
				// Inventory is full, notify the player
				_player.sendMessage("Please free up an inventory slot and try again.");
				return;
			} else {
				// Move the currently held item to the empty slot
				inventory.setItem(emptySlot, heldItem);
			}
		}

		// Place the writable book in the player's hand
		inventory.setItem(slot, book);

		// Notify the player
		_player.sendMessage("You have received a writable book.\nPlease sign it with a new title to rename your coordinate.\n"
			+ ChatColor.BLUE + ChatColor.ITALIC + "This book will be deleted in 1 minute or when dropped.");

		Bukkit.getScheduler().runTaskLater(exec.getPlugin(), () -> {
			// remove all books with the 'coordinateUUID' NBT tag from the player's inventory
			for (ItemStack item : inventory.getContents()) {
				if (item != null && item.getType() == Material.WRITABLE_BOOK) {
					// remove only if the item has the 'coordinateUUID' NBT tag and it is the same as the one we just created
					String itemCoordinateUUID = NBTWrapper.getNBTTag("coordinateUUID", item);
					if (itemCoordinateUUID != null && itemCoordinateUUID.equals(coordinateUUID)) {
						_player.sendMessage(ChatColor.BLUE + "" + ChatColor.ITALIC + "Book was deleted.");
						inventory.remove(item);
					}
				}
			}
		}, 20L * 60L);
	}
}
