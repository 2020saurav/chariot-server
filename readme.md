# CHARIOT [![Build Status](https://travis-ci.org/2020saurav/chariot-server.svg?branch=master)](https://travis-ci.org/2020saurav/chariot-server)
CHARIOT is a framework to simplify request processing for Internet of Things. The idea is to do heavy computations on
legacy machines such as desktops or laptops rather than on IoT devices such as sensors or web cameras. This eliminates
requirement of powerful RAM or processors for any task on IoT devices and saves battery power of such devices. This
idea is inspired from Pub-Sub pattern and RPC protocol.

This work was done as a course project in CS654A Software Architecture by Abhilash Kumar and Saurav Kumar at IIT Kanpur.

## Nomenclature
In a chariot, the functionality (of movement) is provided by the wagon but the actual hard work is done by the horses.
Symbolically, in this architecture the IoT devices become the wagon which has its own functionality but the processing
shall be done by machines with high computational resources. Names of components:
* Processing Machines : Ashva (Sanskrit for horse)
* Coordinating Machines : Prashti (Sanskrit for horse leader)
* Zookeeper : Turagraksa (Sanskrit for patron of horse)

## Architecture

![logical](https://cloud.githubusercontent.com/assets/3881510/14587134/da32e114-04ca-11e6-88f4-0b8155ac2c6f.png)

![physical](https://cloud.githubusercontent.com/assets/3881510/14587133/d005e376-04ca-11e6-8ede-991a7f1a843d.png)

![process](https://cloud.githubusercontent.com/assets/3881510/14587124/a9d13458-04ca-11e6-8011-88d12b1a9859.png)

## Understanding the system
TODO


## Build and Deployment
This is a Gradle project which should be built using `gradle build`. The `build.gradle` file contains several tasks
to deploy AshvaServer, PrashtiServer, ZookeeperServer, run different intergration tests and build complete jar.
Deployment is faster without using Gradle.
```bash
gradle -q buildCompleteJar
java -cp path/to/completejar in.cs654...Server
```
## Building applications for CHARIOT framework
Writing applications for CHARIOT framework is very simple and elegant. To build applications, developers need to create
a docker image which has the right functions in it. This docker image runs on the chariot-server. For simplification,
a base docker image `2020saurav/chariot:1.0` should be used (recommended). This image is built on Ubuntu 14.04 and has
scripts to parse requests and responses into objects from bytes. One just needs to add the relevant functions for
execution of RPC functions.

### Building your docker image
```bash
docker pull 2020saurav/chariot:1.0
docker run -t -i 2020saurav/chariot:1.0 /bin/bash
cd ~/chariot
vi handler.py
```
The `handler.py` looks like:
```python
#!/usr/bin/python3

def handle(request):
  return globals()[request['function_name']]()

def testFunc():
  return {'answer': '42'}
```

Edit `handler.py` to create any function, but remember to return a dictionary. The keys in the dictionary will be used
to extract values from the response sent to the client. The RPC client can be in any language. At present, we are
providing support for python inside docker image. If you dig deeper, it is super simple to create interface in any
language. `/bin/chariot` in the docker image is the executable that is called by the Ashva processor. This executable
transfers the request to handler.py (by importing it).

Once `handler.py` is edited and ready to work, exit from the container
```bash
exit
docker commit -m "Commit message" -a "Name of author" <-container-id-> <-username/newImageName:version->
docker push <-username/newImageName:version-> # to push it to docker hub. requires to be logged in
```

Once the docker image is ready, making calls to chariot server is even easier.

### Java Application

* Import chariot jar into your project (available under releases)
* Make a request:
```java
PrashtiClient client = new PrashtiClient();
BasicRequest request = BasicRequest.newBuilder()
        .setRequestId(CommonUtils.randomString(32))
        .setDeviceId(<deviceId>)
        .setFunctionName(<functionName>)
        .setArguments(new ArrayList<String>())
        .setExtraData(new HashMap<String, String>())
        .build();
BasicResponse response = client.call(request);
```

### Python Application
* Import chariot library (available [here](https://github.com/2020saurav/chariot-pyclient))
* Make a request:
```python
#!/usr/bin/python3
from chariot import *
client = PrashtiClient()
request = {
    'device_id' : <deviceId>,
    'request_id' : randomReqId(),
    'function_name' : <functionName>,
    'arguments' : [],
    'extra_data' : {}
}
response = client.call(request)
```

### FUTURE WORKS
* Authentication
* Encryption
* IPv6 support (very minor changes required)
