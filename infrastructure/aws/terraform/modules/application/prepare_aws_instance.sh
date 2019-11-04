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
sudo touch /opt/cloudwatch_config.json
sudo chmod 777 /opt/cloudwatch_config.json
sudo echo '{\"agent\": {\"metrics_collection_interval\": 10,\"logfile\": \"/var/logs/amazon-cloudwatch-agent.log\"},' > /opt/cloudwatch_config.json
sudo echo '\"logs\": {\"logs_collected\": {\"files\": {\"collect_list\": [{\"file_path\": \"/opt/tomcat/logs/csye6225.log\",\"log_group_name\": \"csye6225_fall2019\",\"log_stream_name\": \"webapp\",\"timestamp_format\": \"%H:%M:%S %y %b %-d\"}]}},\"log_stream_name\": \"cloudwatch_log_stream\"},' >> /opt/cloudwatch_config.json
sudo echo '\"metrics\":{\"metrics_collected\": {\"statsd\": {\"service_address\": \":8125\",\"metrics_collection_interval\":10,\"metrics_aggregation_interval\":0}}}}' >> /opt/cloudwatch_config.json
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/cloudwatch_config.json -s
sudo systemctl restart amazon-cloudwatch-agent.service