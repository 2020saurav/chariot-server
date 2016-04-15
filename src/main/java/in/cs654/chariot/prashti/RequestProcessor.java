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

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.Ashva;
import in.cs654.chariot.utils.AshvaClient;
import in.cs654.chariot.utils.ReservedFunctions;
import in.cs654.chariot.utils.ResponseFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class contains method to process requests to generate responses.
 * Based on the list of reservedFunctions, this method will run the request on Prashti
 * (for calls like setup new device, heartbeat etc) or forward to Ashva for execution
 */
public class RequestProcessor {
    private static final Logger LOGGER = Logger.getLogger("Prashti Request Processor");

    /**
     * This method checks if the request is to be handled at Prashti or be forwarded to Ashva
     * For forwarding the request, get the ashva from LoadBalancer and make RPC call to it
     * @param request request object
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {
        if (ReservedFunctions.contains(request.getFunctionName())) {
            return PrashtiProcessor.process(request);
        } else try {
            final Ashva ashva = LoadBalancer.getAshva();
            final AshvaClient client = new AshvaClient(ashva.getIpAddr());
            return client.call(request);
        } catch (Exception ignore) {
            LOGGER.severe("Error in sending request to Ashva");
            return ResponseFactory.getErrorResponse(request);
        }
    }
}
