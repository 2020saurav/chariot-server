#!/usr/bin/python3

import io
import sys

import avro.schema
import avro.io

from avro.datafile import DataFileReader, DataFileWriter
from avro.io import DatumReader, DatumWriter

sys.path.append('/root/chariot/')

import handler

requestSchema  = avro.schema.Parse(open('avro-schema/basicRequest.avsc').read())
responseSchema = avro.schema.Parse(open('avro-schema/basicResponse.avsc').read())

def buildResponseBody(response, request):
  responseBody = {
    'request_id'    : request['request_id'],
    'status'        : 'SUCCESS',
    'function_name' : request['function_name'],
    'response'      : response
  }
  return responseBody

if len(sys.argv) < 2:
  print('Request ID not present. Aborting')
else:
  requestID    = sys.argv[1]
  rawBytes     = open('/tmp/'+requestID+'.req', 'rb').read()
  bytesReader  = io.BytesIO(rawBytes)
  decoder      = avro.io.BinaryDecoder(bytesReader)
  reader       = avro.io.DatumReader(requestSchema)
  request      = reader.read(decoder)
  response     = handler.handle(request)
  writer       = avro.io.DatumWriter(responseSchema)
  bytesWriter  = io.BytesIO()
  encoder      = avro.io.BinaryEncoder(bytesWriter)
  responseBody = buildResponseBody(response, request)
  writer.write(responseBody, encoder)
  responseFile = open('/tmp/'+requestID+'.res', 'wb')
  responseFile.write(bytesWriter.getvalue())
  print('Response saved in /tmp/'+requestID+'.res')
