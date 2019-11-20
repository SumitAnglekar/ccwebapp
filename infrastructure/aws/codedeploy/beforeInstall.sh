#!/bin/bash
# stop tomcat service
echo "inside after stop"
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/*