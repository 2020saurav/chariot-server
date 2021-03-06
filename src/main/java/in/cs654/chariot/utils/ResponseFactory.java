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

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;

import java.util.HashMap;

/**
 * This class contains functions which help in building common responses.
 */
public class ResponseFactory {

    /**
     * Method to build response for EMPTY case
     * @param request request object
     * @return response with status EMPTY
     */
    public static BasicResponse getEmptyResponse (BasicRequest request) {
        return BasicResponse.newBuilder()
                .setResponse(new HashMap<String, String>())
                .setFunctionName(request.getFunctionName())
                .setRequestId(request.getRequestId())
                .setStatus("EMPTY")
                .build();
    }

    /**
     * Method to build response for ERROR case
     * @param request request object
     * @return response with status ERROR
     */
    public static BasicResponse getErrorResponse (BasicRequest request) {
        return BasicResponse.newBuilder()
                .setResponse(new HashMap<String, String>())
                .setFunctionName(request.getFunctionName())
                .setRequestId(request.getRequestId())
                .setStatus("ERROR")
                .build();
    }

    /**
     * Method to build response for TIMEOUT case
     * @param request request object
     * @return response with status TIMEOUT
     */
    public static BasicResponse getTimeoutErrorResponse (BasicRequest request) {
        return BasicResponse.newBuilder()
                .setResponse(new HashMap<String, String>())
                .setFunctionName(request.getFunctionName())
                .setRequestId(request.getRequestId())
                .setStatus("TIMEOUT")
                .build();
    }
}
