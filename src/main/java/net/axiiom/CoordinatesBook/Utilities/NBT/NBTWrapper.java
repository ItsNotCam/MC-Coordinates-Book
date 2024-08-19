package net.axiiom.CoordinatesBook.Utilities.NBT;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.inventory.ItemStack;

/*
    Wraps the NBT Tag functionality of objects.
    This allows me to store and retrieve metadata from within an object that can be placed into a player's inventory
 */
public class NBTWrapper
{
    public static void setNBTTag(String _key, String _value, ItemStack _itemStack)
    {
        NBT.modifyComponents(_itemStack, nbt -> {
            nbt.setString(_key, _value);
        });
    }

    public static void setNBTTag(NBTTag tag, ItemStack _itemStack)
    {
        System.out.println(tag.toString());
        NBT.modifyComponents(_itemStack, nbt -> {
            nbt.setString(tag.getKey(), (String) tag.getValue());
        });
    }

    public static void setNBTTags(NBTTag[] tags, ItemStack _itemsStack)
    {
        for(NBTTag tag : tags) {
            setNBTTag(tag, _itemsStack);
        }
    }

    public static String getNBTTag(String _key, ItemStack _itemStack)
    {
        return NBT.get(_itemStack, nbt -> {
            return nbt.getString(_key);
        });
    }
}
