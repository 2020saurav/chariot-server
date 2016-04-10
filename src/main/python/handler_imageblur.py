#!/usr/bin/python3
# This is an example of how to write handler module inside docker container.
# The module must be named handler.py and kept in /root/chariot/ of the docker container
# Use base container 2020saurav:chariot from docker hub to build applications.

from PIL import ImageFilter, Image
import io
import base64

def handle(request):
    return globals()[request['function_name']](request)

def blur(request):
    imgBytes = base64.b64decode(request['extra_data']['imgBytes'])
    image    = Image.open(io.BytesIO(imgBytes))
    blurImg  = image.filter(ImageFilter.BLUR)
    blurImg.save('/tmp/'+request['request_id']+'.jpg')
    blurStr = base64.b64encode(open('/tmp/'+request['request_id']+'.jpg', 'rb').read())
    return {'blurStr': blurStr}
