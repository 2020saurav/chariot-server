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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MongoTest {
    @Test
    public void testMongoDevice() {
        String deviceId = CommonUtils.randomString(6);
        String dockerImage = CommonUtils.randomString(10);
        Mongo.addDevice(new Device(deviceId, dockerImage));
        boolean assertion = Mongo.getDockerImage(deviceId).equals(dockerImage);
        Mongo.deleteDeviceById(deviceId);
        assertTrue(assertion);
    }

    @Test
    public void testMongoHeartbeat() {
        String ipAddr = "0.0.0.0";
        String time = ((Long) System.currentTimeMillis()).toString();
        String logs = "";
        Heartbeat heartbeat = new Heartbeat(ipAddr, time, logs);
        Mongo.updateHeartbeat(heartbeat);
        boolean assertion = Mongo.getLastBeatTime(ipAddr)==Long.parseLong(time);
        Mongo.deleteHeartbeatByIP(ipAddr);
        assertTrue(assertion);
    }
}
