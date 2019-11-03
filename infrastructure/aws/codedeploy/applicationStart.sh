#!/bin/bash

# reset cloudwatch agent config
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/home/centos/cloudwatch-agent-config.json \
    -s

# start tomcat service
sudo systemctl start tomcat

