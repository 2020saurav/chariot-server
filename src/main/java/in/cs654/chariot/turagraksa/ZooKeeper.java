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

/**
 * TODO complete this
 * ZK will run only on Prashti.
 * ZK will run it's own process, having its own rabbitmq server
 * jobs of Zookeeper:
 * - receive heartbeat from all servers and update db : will be used by Load Balancer and ZK itself
 * - new prashti selection
 * - syncing data with new servers
 * - syncing data with other prashti (and ping echo)
 */
public class ZooKeeper {
    public static Long HB_TIME_THRESHOLD = 10000L; // milliseconds
}
