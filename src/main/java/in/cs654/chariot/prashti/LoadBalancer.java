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

import in.cs654.chariot.utils.Ashva;
import in.cs654.chariot.utils.CommonUtils;
import in.cs654.chariot.utils.Mongo;

import java.util.List;
import java.util.logging.Logger;

// TODO javadoc
public class LoadBalancer {
    private static final Logger LOGGER = Logger.getLogger("Load Balancer");
    public static Ashva getAshva() {
        final List<Ashva> ashvaList = Mongo.getAliveAshvaList();
        final int randIndex = CommonUtils.randInt(0, ashvaList.size()-1);
        final Ashva ashva = ashvaList.get(randIndex);
        LOGGER.info("Returning Ashva ["+ashva.getIpAddr()+"]");
        return ashva;
    }
}
