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

package in.cs654.chariot.prashti;

import in.cs654.chariot.ashva.AshvaServer;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class has function process to handle requests sent to prashti server.
 */
class PrashtiProcessor {
    private static final Logger LOGGER = Logger.getLogger("Prashti Processor");

    /**
     * This function matches the request against the following:
     * DEVICE_SETUP : This request is sent by the devices. The request is handled by requesting all alive
     * ashvas to install the device.
     * PING : Simple Ping-Echo
     * @param request received by the prashti server
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {
        if (request.getFunctionName().equals(ReservedFunctions.DEVICE_SETUP.toString())) {
            final String dockerImage = request.getExtraData().get("dockerImage");
            final Device device = new Device(request.getDeviceId(), dockerImage);
            LOGGER.info("Device setup request by device " + device.getId() + " (" + dockerImage + ")");
            final BasicRequest installRequest = RequestFactory.getInstallRequest(device);
            final List<Ashva> ashvaList = Mongo.getAliveAshvaList();
            for (Ashva ashva : ashvaList) {
                final AshvaClient client = new AshvaClient(ashva.getIpAddr());
                client.call(installRequest);
            }
            return ResponseFactory.getEmptyResponse(request);
        } else if (request.getFunctionName().equals(ReservedFunctions.PING.toString())) {
            return ResponseFactory.getEmptyResponse(request);
        } else {
            return ResponseFactory.getErrorResponse(request);
        }
    }
}
