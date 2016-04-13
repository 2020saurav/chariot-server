#!/bin/bash
java -cp build/libs/chariot-server-all-1.0.jar in.cs654.chariot.prashti.PrashtiServer >> ps.log 2>> ps2.log &
java -cp build/libs/chariot-server-all-1.0.jar in.cs654.chariot.turagraksa.ZooKeeperServer >> zks.log 2>> zks2.log &
