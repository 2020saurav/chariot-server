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

package in.cs654.chariot.turagraksa;

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.utils.Heartbeat;
import in.cs654.chariot.utils.RequestFactory;
import in.cs654.chariot.utils.ZooKeeperClient;

public class ZooKeeper {
    public static final Long HB_TIME_THRESHOLD = 30000L; // milliseconds
    public static final Long HB_TIME_ASHVA = 15000L;
    private static ZooKeeperClient otherZooKeeperClient = null;

    static void notifyOtherZooKeeperServer(Heartbeat heartbeat) {
        if (otherZooKeeperClient != null) {
            final BasicRequest request = RequestFactory.getHeartbeatSyncRequest(heartbeat);
            otherZooKeeperClient.call(request);
        }
    }
}
