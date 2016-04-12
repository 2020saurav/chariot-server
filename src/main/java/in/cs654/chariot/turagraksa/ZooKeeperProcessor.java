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
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.Heartbeat;
import in.cs654.chariot.utils.Mongo;
import in.cs654.chariot.utils.ReservedFunctions;
import in.cs654.chariot.utils.ResponseFactory;

public class ZooKeeperProcessor {
    public static BasicResponse process(BasicRequest request) {
        if (request.getFunctionName().equals(ReservedFunctions.HEARTBEAT.toString())) {
            final String ipAddr = request.getExtraData().get("ipAddr");
            final String timeOfBeat = request.getExtraData().get("timeOfBeat");
            final String logs = request.getExtraData().get("logs");
            final Heartbeat heartbeat = new Heartbeat(ipAddr, timeOfBeat, logs);
            Mongo.updateHeartbeat(heartbeat);
            return ResponseFactory.getEmptyResponse(request);
        } else {
            return ResponseFactory.getErrorResponse(request);
        }
    }
}
