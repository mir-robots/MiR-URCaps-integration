#!/usr/bin/env python

import sys
import os
import requests
import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

class MirRestClient:
    timeout = 1.0
    host = "192.168.12.20"
    port = 8080
    default_headers = {'Content-Type': 'application/json', 'Authorization': 'Basic YWRtaW46OGM2OTc2ZTViNTQxMDQxNWJkZTkwOGJkNGRlZTE1ZGZiMTY3YTljODczZmM0YmI4YTgxZjZmMmFiNDQ4YTkxOA=='}

    def __init__(self, host=None, port=None):
        self.host = host if host else "192.168.12.20"
        self.port = port if port else "8080"
        sys.stdout.write("MiR REST daemon started")
        if os.path.isfile('ip.txt'):
            with open('ip.txt', 'r') as f:
                self.host = f.read()

    def _form_url(self, endpoint):
        url = "http://" + self.host + ":" + str(self.port) + "/v2.0.0/"
        url = url + endpoint
        return url

    def ping(self):
        return "pong"

    def set_ip(self, ip):
        self.host = ip
        with open('ip.txt', 'w') as f:
            f.write(ip)
        return "Ok"

    def get_register(self, register):
        url = self._form_url("registers/" + str(register) + "?whitelist=value")
        data = requests.get(url, headers=self.default_headers, timeout=self.timeout)
        res = data.status_code
        if res == 200:
            if 'value' in data.json():
                return data.json()['value']
        else:
            return None

    def write_register(self, register, value):
        url = self._form_url("registers/" + str(register))
        body = {'value': value}
        requests.put(url, json=body, headers=self.default_headers, timeout=self.timeout)

    def get_register_labels(self):
        url = self._form_url("registers?whitelist=id,label")
        data = requests.get(url, headers=self.default_headers, timeout=self.timeout)
        res = data.status_code
        if res == 200:
            return data.text.encode('ascii', 'ignore')
        else:
            return None

    def get_status(self):
        url = self._form_url("status")
        data = requests.get(url, headers=self.default_headers, timeout=self.timeout)
        res = data.status_code
        if res == 200:
            return data.text.encode('ascii', 'ignore')
        else:
            return ""

    def get_current_mission(self):
        url = self._form_url("mission_queue/search?whitelist=state,mission_id")
        body = {"filters": [
            {
            "operator": "IN",
            "fieldname": "state",
            "value": [
                "Pending",
                "Executing"]
            }
        ]}
        data = requests.post(url, json=body, headers=self.default_headers, timeout=self.timeout)
        res = data.status_code
        payload = data.json()

        if res == 201:
            if len(payload) > 0 and "mission_id" in payload[0]:
                url = self._form_url("missions/" + payload[0]["mission_id"] + "?whitelist=name")
                data = requests.get(url, headers=self.default_headers, timeout=self.timeout)
                res = data.status_code
                payload = data.json()

                if res == 200 and "name" in payload:
                    return payload["name"]

        return "-"

    def _put_state(self, state):
        url = self._form_url("status")
        body = {'state_id': state}
        requests.put(url, json=body, headers=self.default_headers, timeout=self.timeout)

    def continue_robot(self):
        self._put_state(3)
        return "Ok"

    def pause_robot(self):
        self._put_state(4)
        return "Ok"


if __name__ == "__main__":
    client = MirRestClient()
    
    server = SimpleXMLRPCServer(("", 34567), allow_none=True)
    server.register_instance(client)
    server.serve_forever()
