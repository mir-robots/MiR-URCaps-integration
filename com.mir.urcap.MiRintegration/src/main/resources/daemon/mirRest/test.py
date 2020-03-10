#!/usr/bin/env python

import xmlrpclib
import socket

proxy = xmlrpclib.ServerProxy("http://127.0.0.1:34567")
socket.setdefaulttimeout(5)
print proxy.set_ip("192.168.15.132")
print proxy.ping()
print proxy.get_register(1)
print proxy.write_register(1,0)
print proxy.get_status()
print proxy.continue_robot()
print proxy.get_current_mission()