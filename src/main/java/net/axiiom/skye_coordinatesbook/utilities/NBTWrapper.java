package net.axiiom.skye_coordinatesbook.utilities;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

/*
    Wraps the NBT Tag functionality of objects.
    This allows me to store and retrieve metadata from within an object that can be placed into a player's inventory
 */
public class NBTWrapper
{
    public static ItemStack setNBTTag(String _tagName, String _value, ItemStack _itemStack)
    {
        NBTItem item = new NBTItem(_itemStack);
        item.setString(_tagName, _value);
        return item.getItem();
    }

    public static String getNBTTag(String _key, ItemStack _itemStack)
    {
        NBTItem item = new NBTItem(_itemStack);
        return item.getString(_key);
    }
}
