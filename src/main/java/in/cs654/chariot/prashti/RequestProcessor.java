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
import in.cs654.chariot.utils.AshvaClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class contains method to process requests to generate responses, and to generate error responses.
 * Based on the list of reservedFunctions (function names which will not be allowed at Ashva level), this method
 * will run the request on Prashti (for calls like setup new device, heartbeat etc) or forward to Ashva for execution
 */
public class RequestProcessor {

    public static List<String> reservedFunctions = Arrays.asList("chariotSetup", "chariotFoo");

    /**
     * This method checks if the request is to be handled at Prashti ot be forwarded to Ashva
     * For forwarding the request, TODO get the IP address using device_id
     * @param request request object
     * @return response of the request
     */
    public static BasicResponse process(BasicRequest request) {
        if (reservedFunctions.contains(request.getFunctionName())) {
            return PrashtiProcessor.process(request);
        } else try {
            // TODO load balancing strategy to find IP addr
            final AshvaClient client = new AshvaClient("172.24.1.62");
            final BasicResponse response = client.call(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return error(request);
        }
    }

    /**
     * Method to build response for ERROR case
     * @param request request object
     * @return response with status ERROR
     */
    public static BasicResponse error(BasicRequest request) {
        return BasicResponse.newBuilder()
                .setResponse(new HashMap<String, String>())
                .setFunctionName(request.getFunctionName())
                .setRequestId(request.getRequestId())
                .setStatus("ERROR")
                .build();
    }
}