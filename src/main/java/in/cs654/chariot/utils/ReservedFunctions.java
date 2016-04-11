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

public enum ReservedFunctions {
    DEVICE_SETUP ("chariotDeviceSetup"),
    ASHVA_JOIN ("chariotAshvaJoin"),
    DEVICE_INSTALL ("chariotDeviceInstall");

    private final String name;

    private ReservedFunctions(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

    public static boolean contains(String functionName) {
        for (ReservedFunctions r : ReservedFunctions.values()) {
            if (r.name.equals(functionName)) {
                return true;
            }
        }
        return false;
    }
}
