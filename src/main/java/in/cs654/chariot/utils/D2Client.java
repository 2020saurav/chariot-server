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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class D2Client {

    public static final String d2ServiceURL = "http://172.27.25.236:4567/prashtis";
    public static final long D2_PING_TIMEOUT = 2000L; // milliseconds
    public static List<Prashti> getOnlinePrashtiServers() {
        List<Prashti> prashtiList = new ArrayList<Prashti>();
        try {
            URL url = new URL(d2ServiceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(in.readLine());
            JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement e : jsonArray) {
                final String ipAddr = e.getAsJsonObject().get("ipAddr").getAsString();
                if (!ipAddr.equals("") && isActive(ipAddr)) {
                    prashtiList.add(new Prashti(ipAddr));
                }
            }
        } catch (Exception ignore) {
        }
        return prashtiList;
    }

    public static List<Prashti> getPrashtiServers() {
        List<Prashti> prashtiList = new ArrayList<Prashti>();
        try {
            URL url = new URL(d2ServiceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(in.readLine());
            JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement e : jsonArray) {
                final String ipAddr = e.getAsJsonObject().get("ipAddr").getAsString();
                if (!ipAddr.equals("")) {
                    prashtiList.add(new Prashti(ipAddr));
                }
            }
        } catch (Exception ignore) {
        }
        return prashtiList;
    }

    private static boolean isActive(final String ipAddr) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Callable<BasicResponse> task = new Callable<BasicResponse>() {
            @Override
            public BasicResponse call() throws Exception {
                PrashtiClient client = new PrashtiClient(ipAddr);
                BasicRequest request = BasicRequest.newBuilder()
                        .setRequestId(CommonUtils.randomString(32))
                        .setArguments(new ArrayList<String>())
                        .setDeviceId("D2")
                        .setExtraData(new HashMap<String, String>())
                        .setFunctionName(ReservedFunctions.PING.toString())
                        .build();
                return client.call(request);
            }
        };
        Future<BasicResponse> future = executorService.submit(task);
        try {
            BasicResponse ignore = future.get(D2_PING_TIMEOUT, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static void setPrashtiServers(List<Prashti> prashtiList) {
        try {
            URL url = new URL(d2ServiceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String params = "";
            if (prashtiList.size() == 1) {
                params = "ip1=" + prashtiList.get(0).getIpAddr();
                params += "&ip2=";
            } else if (prashtiList.size() >= 2) {
                params = "ip1=" + prashtiList.get(0).getIpAddr();
                params += "&ip2=" + prashtiList.get(1).getIpAddr();
            }
            connection.setDoOutput(true);
            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
            stream.writeBytes(params);
            stream.flush();
            stream.close();
            connection.getResponseCode();
        } catch (Exception ignore) {
        }
    }
}
