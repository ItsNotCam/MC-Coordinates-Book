package net.axiiom.CoordinatesBook.Listeners;

import net.axiiom.CoordinatesBook.Main.CoordinatesBookPlugin;
import net.axiiom.CoordinatesBook.Utilities.NBT.NBTWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
// import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

public class BookCloseListener implements Listener {
	private final CoordinatesBookPlugin plugin;

	public BookCloseListener(CoordinatesBookPlugin _plugin) {
		this.plugin = _plugin;
	}

//	@EventHandler
//	public void onSwapAwayFromBook(PlayerChangedMainHandEvent event) {
//		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
//		if(!item.getType().equals(Material.WRITABLE_BOOK)) {
//			return;
//		}
//
//		String coordinateUUID = NBTWrapper.getNBTTag("coordinateUUID", item);
//		if(coordinateUUID == null) {
//			return;
//		}
//
//		// tell the user that the book has been deleted
//		event.getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.ITALIC + "Book was deleted.");
//		event.getPlayer().getInventory().remove(item);
//	}

	@EventHandler
	public void onDropBook(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();
		if(!item.getType().equals(Material.WRITABLE_BOOK)) {
			return;
		}

		String coordinateUUID = NBTWrapper.getNBTTag("coordinateUUID", item);
		if(coordinateUUID == null) {
			return;
		}

		// tell the user that the book has been deleted
		event.getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.ITALIC + "Book was deleted.");
		event.getItemDrop().remove();
	}

	@EventHandler
	public void onBookClose(PlayerEditBookEvent event) {
		if (!event.isSigning()) {
			return;
		}

		ItemStack book = event.getPlayer().getInventory().getItemInMainHand();
		if (!book.getType().equals(Material.WRITABLE_BOOK)) {
			return;
		}

		String coordinateUUID = NBTWrapper.getNBTTag("coordinateUUID", book);
		if (coordinateUUID == null) {
			return;
		}

		BookMeta meta = event.getNewBookMeta();
		String title = meta.getTitle();

		boolean success = plugin.getCoordinateManager().renameCoordinate(event.getPlayer(), coordinateUUID, title);
		if (success) {
			event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully renamed coordinate");
		} else {
			event.getPlayer().sendMessage(ChatColor.RED + "Failed to rename coordinate");
		}

		removeBookLater(event, coordinateUUID);
	}

	private void removeBookLater(PlayerEditBookEvent event, String coordinateUUID) {
		Bukkit.getScheduler().runTask(plugin, () -> {
			// remove all written books with an NBT tag of "uuid" that are the same as the uid of the book that was just signed
			Arrays.stream(event.getPlayer().getInventory().getContents())
				.filter(item ->
					item != null
						&& (item.getType().equals(Material.WRITABLE_BOOK) || item.getType().equals(Material.WRITTEN_BOOK))
						&& NBTWrapper.getNBTTag("coordinateUUID", item).equals(coordinateUUID)
				)
				.forEach(item -> event.getPlayer().getInventory().remove(item));
		});
	}
}