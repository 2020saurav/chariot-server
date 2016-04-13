#!/bin/bash
gradle  rPS >> ps.log 2>> ps2.log &
gradle  rZKS >> zks.log 2>> zks2.log &
