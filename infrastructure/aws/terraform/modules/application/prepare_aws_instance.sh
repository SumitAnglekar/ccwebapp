#! /bin/sh
sudo touch /opt/tomcat/bin/setenv.sh
sudo chmod 777 /opt/tomcat/bin/setenv.sh
sudo echo 'export JAVA_OPTS="-Daws.s3.bucketname='${s3_bucket_name}'"' > /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.url=jdbc:postgresql://'${aws_db_endpoint}'/'${aws_db_name}'"' >> /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.username='${aws_db_username}'"' >> /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.password='${aws_db_password}'"' >> /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Daws.region='${aws_region}'"' >> /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Daws.profile='${aws_profile}'"' >> /opt/tomcat/bin/setenv.sh
sudo echo 'JAVA_OPTS="$JAVA_OPTS -Dlogging.file.name=/opt/tomcat/logs/csye6225.log"' >> /opt/tomcat/bin/setenv.sh
sudo systemctl restart tomcat
