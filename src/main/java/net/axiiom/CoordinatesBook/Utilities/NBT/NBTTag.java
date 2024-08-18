package net.axiiom.CoordinatesBook.Utilities.NBT;

public class NBTTag {
    public String key;
    public Object value;
    
    public NBTTag(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
