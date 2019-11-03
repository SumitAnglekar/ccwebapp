#!/bin/bash
# stop tomcat service
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/recipe.war