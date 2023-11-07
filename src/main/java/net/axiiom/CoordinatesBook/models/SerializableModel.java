package net.axiiom.CoordinatesBook.models;

import java.io.*;
import java.util.Base64;

public class SerializableModel implements Serializable {
    public Object deserialize(String serialized) throws IOException, ClassNotFoundException  {
        byte[] data = Base64.getDecoder().decode(serialized);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        ois.close();

        return ois.readObject();
    }

    public String serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(this);
        oos.close();

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
