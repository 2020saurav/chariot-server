#!/usr/bin/python3

def handle(request):
  return globals()[request['function_name']]()

def testFunc():
  return {'answer': '42'}
