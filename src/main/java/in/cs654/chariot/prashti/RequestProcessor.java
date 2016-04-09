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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// TODO javadoc
public class RequestProcessor {

    public static List<String> reservedFunctions = Arrays.asList("chariotSetup", "chariotFoo");

    public static BasicResponse process(BasicRequest request) {
        if (reservedFunctions.contains(request.getFunctionName())) {
            return PrashtiProcessor.process(request);
        } else {
            // TODO make RPC to ashva
            return new BasicResponse();
        }
    }

    public static BasicResponse error(BasicRequest request) {
        return BasicResponse.newBuilder()
                .setResponse(new HashMap<String, String>())
                .setFunctionName(request.getFunctionName())
                .setRequestId(request.getRequestId())
                .setStatus("ERROR")
                .build();
    }
}
