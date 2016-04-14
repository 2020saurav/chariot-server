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

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public static String serializeDeviceList(List<Device> devices) {
        String serialized = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(devices);
            oos.flush();
            oos.close();
            serialized = Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ignore) {
        }
        return serialized;
    }

    public static List<Device> deserializeDeviceListString(String serializedString) {
        List<Device> devices = new ArrayList<Device>();
        try {
            byte[] data = Base64.getDecoder().decode(serializedString);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            devices = (ArrayList) ois.readObject();
        } catch (Exception ignore) {
        }
        return devices;
    }
}
