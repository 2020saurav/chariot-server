/*
 * Copyright (c) 2016 Abhilash Kumar and Saurav Kumar.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package in.cs654.chariot.utils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a device having an id and its corresponding docker image.
 */
public class Device implements Serializable {

    private String id;
    private String dockerImage;

    public Device(String id, String dockerImage) {
        this.id = id;
        this.dockerImage = dockerImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String getDockerImage() {
        return dockerImage;
    }

    /**
     * This function serializes a list of devices into a byte string.
     * @param devices list of device
     * @return byte string representation of the list
     */
    static String serializeDeviceList(List<Device> devices) {
        String serialized = "";
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(devices);
            oos.flush();
            oos.close();
            serialized = DatatypeConverter.printBase64Binary(baos.toByteArray());
        } catch (IOException ignore) {
        }
        return serialized;
    }

    /**
     * This function de-serializes the byte string into list of device
     * @param serializedString byte string representing the list of device
     * @return list of device
     */
    public static List<Device> deserializeDeviceListString(String serializedString) {
        List<Device> devices = new ArrayList<Device>();
        try {
            final byte[] data = DatatypeConverter.parseBase64Binary(serializedString);
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            devices = (ArrayList) ois.readObject();
        } catch (Exception ignore) {
        }
        return devices;
    }
}
