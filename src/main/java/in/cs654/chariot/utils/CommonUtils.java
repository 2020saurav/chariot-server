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

import java.net.*;
import java.util.Enumeration;
import java.util.Random;

/**
 * Class for common utility functions for chariot-server.
 */
public class CommonUtils {

    private final static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Function to generate random string of given length
     * @param length of random string as required
     * @return random string
     */
    public static String randomString(int length) {
        final Random rand = new Random();
        final StringBuilder buf = new StringBuilder();
        for (int i=0; i<length; i++) {
            buf.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return buf.toString();
    }

    /**
     * This function is used to get IP Address of the host machine. For chariot-server,
     * it is very likely (only possibility in dev) that it is connected by eth0 or by wlan0.
     * In that order, if an IP Address is found, it is returned. Not very certain of it's reliability.
     * @return ipadress of the host machine
     */
    public static String getIPAddress() {
        try {
            final NetworkInterface niEth0 = NetworkInterface.getByName("eth0");
            final NetworkInterface niWlan0 = NetworkInterface.getByName("wlan0");
            if (niEth0 != null) {
                for (Enumeration en = niEth0.getInetAddresses(); en.hasMoreElements();) {
                    final InetAddress addr = (InetAddress) en.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
            if (niWlan0 != null) {
                for (Enumeration en = niWlan0.getInetAddresses(); en.hasMoreElements();) {
                    final InetAddress addr = (InetAddress) en.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignore) {
        }
        return "";
    }

    /**
     * Function to get a random integer in range [min, max].
     * @param min minimum of range
     * @param max maximum of range
     * @return random integer in the range
     */
    public static int randInt(int min, int max) {
        final Random rn = new Random();
        final int range = max - min + 1;
        return rn.nextInt(range) + min;
    }
}
