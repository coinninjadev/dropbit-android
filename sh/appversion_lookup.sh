#!/usr/bin/python

import sys
import json
import urllib2

url = "https://rink.hockeyapp.net/api/2/apps/c684dc8c4bbe4a858649d6cd16c67ec3/app_versions"
headers = {
  "X-HockeyAppToken": ""
}


if __name__ == "__main__":
    headers['X-HockeyAppToken'] = sys.argv[1]
    request = urllib2.Request(url, None, headers)
    response = urllib2.urlopen(request)
    data = json.loads(response.read())
    versions = data.get("app_versions", [])
    if len(versions) > 0:
        print int(versions[0].get("version")) + 1
    else:
        print "0"
