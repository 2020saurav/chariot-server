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

/**
 * This class is used to build object which will run a 'Process' which needs to
 * finish within a given timeout. This is used by Ashva Processor to encapsulate
 * the `docker run` process.
 */
public class TimeoutProcess extends Thread {
    private final Process process;
    public Integer exit;
    public TimeoutProcess(Process process) {
        this.process = process;
    }
    public void run() {
        try {
            exit = process.waitFor();
        } catch (InterruptedException ignore) {
        }
    }
}
