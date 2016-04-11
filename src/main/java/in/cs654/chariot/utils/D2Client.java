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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class D2Client {

    public static final String d2ServiceURL = "http://172.27.25.236:4567/prashtis";

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
                prashtiList.add(new Prashti(e.getAsJsonObject().get("ipAddr").getAsString()));
            }
        } catch (Exception ignore) {
        }
        return prashtiList;
    }
}
