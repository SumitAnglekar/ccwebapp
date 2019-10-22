#! /bin/sh
sudo echo 'JAVA_OPTS=-Daws.s3.bucketname='${s3_bucket_name} >> /opt/tomcat/bin/setenv.sh