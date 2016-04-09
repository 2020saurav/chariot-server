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

package in.cs654.chariot.integtest;

import in.cs654.chariot.ashva.AshvaProcessor;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AshvaProcessorTest {
    public static void main(String[] args) {
        BasicRequest request = BasicRequest.newBuilder()
                .setRequestId(CommonUtils.randomString(32))
                .setFunctionName("testFunc")
                .setArguments(new ArrayList<String>())
                .setDeviceId("1")
                .setExtraData(new HashMap<String, String>())
                .build();
        BasicResponse response = AshvaProcessor.process(request);
        System.out.println(response.getResponse().get("answer"));
    }
}
