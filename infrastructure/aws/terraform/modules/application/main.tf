# S3 Bucket for recipe images
resource "aws_s3_bucket" "bucket" {
  bucket = "webapp.${var.env}.${var.domainName}"
  acl = "private"
  force_destroy = "true"

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm     = "aws:kms"
      }
    }
  }

  lifecycle_rule {
    enabled = true

    transition {
      days = 30
      storage_class = "STANDARD_IA"
    }
  }
}

data "aws_ami" "packer_ami" {
  owners = ["self"]
  most_recent = true

  filter {
    name = "tag:OS_Version"
    values = ["centos"]
  }
}

# AWS LAUNCH CONFIGURATION

resource "aws_launch_configuration" "asg_launch_config" {
  name          = "asg_launch_config"
  image_id      = "${data.aws_ami.packer_ami.id}"
  instance_type = "t2.micro"
  security_groups = [ "${aws_security_group.application.id}" ]
  key_name      = "${var.aws_ssh_key}"
  user_data     = "${templatefile("${path.module}/prepare_aws_instance.sh",
                                    {
                                      s3_bucket_name = "${aws_s3_bucket.bucket.id}",
                                      aws_db_endpoint = "${aws_db_instance.myRDS.endpoint}",
                                      aws_db_name = "${aws_db_instance.myRDS.name}",
                                      aws_db_username = "${aws_db_instance.myRDS.username}",
                                      aws_db_password = "${aws_db_instance.myRDS.password}",
                                      aws_region = "${var.region}",
                                      aws_profile = "${var.env}"
                                    })}"

  associate_public_ip_address = true
  iam_instance_profile = "${aws_iam_instance_profile.ec2_profile.name}"

  root_block_device {
    volume_type = "gp2"
    volume_size = "20"
    delete_on_termination = true
  }

  depends_on = [aws_s3_bucket.bucket,aws_db_instance.myRDS]
}

## AUTOSCALING GROUP
resource "aws_autoscaling_group" "autoscaling" {
  name                 = "terraform-asg-example"
  launch_configuration = "${aws_launch_configuration.asg_launch_config.name}"
  min_size             = 3
  max_size             = 10
  default_cooldown     = 60
  desired_capacity     = 3
  load_balancers       = ["${aws_elb.application_loadbalancer.name}"]
  vpc_zone_identifier = ["${var.subnet_id}"]
  health_check_type = "ELB"
}

#

#### SECURITY GROUP #####
##LOAD BALANCER SECURITY GROUP
resource "aws_security_group" "loadbalancer" {
  name          = "loadbalancer_security_group"
  vpc_id        = "${var.vpc_id}"
  ingress{
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  // Egress is used here to communicate anywhere with any given protocol
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags          = {
    Name        = "LoadBalancer Security Group"
    Environment = "${var.env}"
  }
}

resource "aws_autoscaling_policy" "WebServerScaleUpPolicy" {
  name                   = "WebServerScaleUpPolicy"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.autoscaling.name}"
}

resource "aws_autoscaling_policy" "WebServerScaleDownPolicy" {
  name                   = "WebServerScaleDownPolicy"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.autoscaling.name}"
}

resource "aws_cloudwatch_metric_alarm" "CPUAlarmHigh" {
  alarm_name          = "CPUAlarmHigh"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "90"
  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.autoscaling.name}"
  }
  alarm_description = "This metric monitors ec2 cpu utilization"
  alarm_actions     = ["${aws_autoscaling_policy.WebServerScaleUpPolicy.arn}"]
}

resource "aws_cloudwatch_metric_alarm" "CPUAlarmLow" {
  alarm_name          = "CPUAlarmLow"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "70"
  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.autoscaling.name}"
  }
  alarm_description = "This metric monitors ec2 cpu utilization"
  alarm_actions     = ["${aws_autoscaling_policy.WebServerScaleDownPolicy.arn}"]
}

#Application security group
resource "aws_security_group" "application" {
  name          = "application_security_group"
  vpc_id        = "${var.vpc_id}"
  ingress{
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  tags          = {
    Name        = "Application Security Group"
    Environment = "${var.env}"
  }
}
## check this
# Application security group rule
resource "aws_security_group_rule" "application"{

  type        = "ingress"
  from_port   = 8080
  to_port     = 8080
  protocol    = "tcp"
  // cidr_blocks  = "${var.subnet_id_list}"
  // Source of the traffic should be the loadbalancer security group, hence we pass on the loadbalancer id instance
  source_security_group_id  = "${aws_security_group.loadbalancer.id}"
  //Reference the above created application security group
  security_group_id         = "${aws_security_group.application.id}"
}

# Database security group
resource "aws_security_group" "database"{
  name          = "database_security_group"
  vpc_id        = "${var.vpc_id}"
  tags          = {
    Name        = "Database Security Group"
    Environment = "${var.env}"
  }
}


# Database security group rule
resource "aws_security_group_rule" "database"{

  type        = "ingress"
  from_port   = 5432
  to_port     = 5432
  protocol    = "tcp"
  // cidr_blocks  = "${var.subnet_id_list}"
  // Source of the traffic should be the application security group, hence we pass on the application id instance
  source_security_group_id  = "${aws_security_group.application.id}"
  //Reference the above created database security group
  security_group_id         = "${aws_security_group.database.id}"
}

#RDS DB instance
resource "aws_db_instance" "myRDS" {
  allocated_storage    = 20
  storage_type         = "gp2"
  name                 = "${var.rdsDBName}"
  username             = "${var.rdsUsername}"
  password             = "${var.rdsPassword}"
  identifier = "${var.rdsInstanceIdentifier}"
  engine            = "postgres"
  engine_version    = "10.10"
  instance_class    = "db.t2.medium"
  # storage_encrypted = false
  port     = "5432"
  vpc_security_group_ids = [ "${aws_security_group.database.id}" ]
  final_snapshot_identifier = "${var.rdsInstanceIdentifier}-SNAPSHOT"
  skip_final_snapshot = true
  
  publicly_accessible = true
  multi_az = false

  tags = {
    Name        = "myRDS"
    Owner       = "${var.rdsOwner}"
  }

  # DB subnet group
  db_subnet_group_name = "${var.aws_db_subnet_group_name}"

}

# LoadBalancer
resource "aws_elb" "application_loadbalancer" {
  name               = "ApplicationLoadbalancer"
  security_groups    = ["${aws_security_group.loadbalancer.id}"]
  subnets            = ["${var.subnet_id}"]
  
  health_check {
    healthy_threshold = 3
    unhealthy_threshold = 5
    timeout = 5
    interval = 30
    target = "HTTP:8080/"
  }

  # listener {
  #   instance_port     = 8080
  #   instance_protocol = "http"
  #   lb_port           = 443
  #   lb_protocol       = "http"
  # }

  listener {
    instance_port      = 8080
    instance_protocol  = "http"
    lb_port            = 443
    lb_protocol        = "https"
    ssl_certificate_id = "${data.aws_acm_certificate.aws_ssl_certificate.arn}"
  }

}



# # EC2 Instance
# resource "aws_instance" "ec2_instance" {
#   ami = "${data.aws_ami.packer_ami.id}"
#   instance_type = "t2.micro"
#   security_groups = [ "${aws_security_group.application.id}" ]
#   subnet_id = "${var.subnet_id}"
#   disable_api_termination = false
#   key_name = "${var.aws_ssh_key}"
#   iam_instance_profile = "${aws_iam_instance_profile.ec2_profile.name}"
#   user_data = "${templatefile("${path.module}/prepare_aws_instance.sh",
#                                     {
#                                       s3_bucket_name = "${aws_s3_bucket.bucket.id}",
#                                       aws_db_endpoint = "${aws_db_instance.myRDS.endpoint}",
#                                       aws_db_name = "${aws_db_instance.myRDS.name}",
#                                       aws_db_username = "${aws_db_instance.myRDS.username}",
#                                       aws_db_password = "${aws_db_instance.myRDS.password}",
#                                       aws_region = "${var.region}",
#                                       aws_profile = "${var.env}"
#                                     })}"

#   root_block_device {
#     volume_type = "gp2"
#     volume_size = "20"
#     delete_on_termination = true
#   }

#   tags = {
#     Name        = "myEC2Instance"
#   }

#   depends_on = [aws_s3_bucket.bucket,aws_db_instance.myRDS]
# }


#Dynamo db
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "${var.dynamoName}"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "${var.dynamoName}"
  }
}

#CodeDeploy App and Group
resource "aws_codedeploy_app" "code_deploy_app" {
  compute_platform = "Server"
  name             = "csye6225-webapp"
}

resource "aws_codedeploy_deployment_group" "code_deploy_deployment_group" {
  app_name              = "csye6225-webapp"
  deployment_group_name = "csye6225-webapp-deployment"
  deployment_config_name = "CodeDeployDefault.AllAtOnce"
  service_role_arn      = "${aws_iam_role.code_deploy_role.arn}"

  ec2_tag_filter {
    key   = "Name"
    type  = "KEY_AND_VALUE"
    value = "myEC2Instance"
  }
    
  deployment_style {
    deployment_option = "WITHOUT_TRAFFIC_CONTROL"
    deployment_type   = "IN_PLACE"
  }

  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }

  alarm_configuration {
    alarms  = ["Deployment-Alarm"]
    enabled = true
  }
}

###### IAM ROLES AND POLICIES ######

# Role for EC2 Instance
resource "aws_iam_role" "EC2_Role" {
  name = "EC2_Role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

  tags = {
      Name = "EC2 Role"
  }
}

# Profile for the EC2 Role
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "ec2_profile"
  role = "${aws_iam_role.EC2_Role.name}"
}

# Policy for EC2 Role
# This policy allows to read & upload data from S3 bucket
resource "aws_iam_role_policy" "CodeDeploy-EC2-S3" {
  name = "ec2_policy"
  role = "${aws_iam_role.EC2_Role.id}"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:Get*",
        "s3:List*",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:DeleteObjectVersion"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::codedeploy.${var.env}.${var.domainName}/*",
        "arn:aws:s3:::webapp.${var.env}.${var.domainName}/*"
      ]
    }
  ]
}
EOF
}

# Attach the policy to the EC2 role for CloudWatch Agent
resource "aws_iam_role_policy_attachment" "cloud_watch_EC2" {
  role = "${aws_iam_role.EC2_Role.name}"
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

# Add policies for circleci user

resource "aws_iam_policy" "circleci_user_policy" {
  name = "circleci_ec2_policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": [
      "ec2:AttachVolume",
      "ec2:AuthorizeSecurityGroupIngress",
      "ec2:CopyImage",
      "ec2:CreateImage",
      "ec2:CreateKeypair",
      "ec2:CreateSecurityGroup",
      "ec2:CreateSnapshot",
      "ec2:CreateTags",
      "ec2:CreateVolume",
      "ec2:DeleteKeyPair",
      "ec2:DeleteSecurityGroup",
      "ec2:DeleteSnapshot",
      "ec2:DeleteVolume",
      "ec2:DeregisterImage",
      "ec2:DescribeImageAttribute",
      "ec2:DescribeImages",
      "ec2:DescribeInstances",
      "ec2:DescribeInstanceStatus",
      "ec2:DescribeRegions",
      "ec2:DescribeSecurityGroups",
      "ec2:DescribeSnapshots",
      "ec2:DescribeSubnets",
      "ec2:DescribeTags",
      "ec2:DescribeVolumes",
      "ec2:DetachVolume",
      "ec2:GetPasswordData",
      "ec2:ModifyImageAttribute",
      "ec2:ModifyInstanceAttribute",
      "ec2:ModifySnapshotAttribute",
      "ec2:RegisterImage",
      "ec2:RunInstances",
      "ec2:StopInstances",
      "ec2:TerminateInstances"
    ],
    "Resource" : "*"
  }]
}
EOF
}

resource "aws_iam_policy" "CircleCI-Upload-To-S3" {
  name = "circleci_s3_policy"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject"
        ],
      "Resource": "arn:aws:s3:::codedeploy.${var.env}.${var.domainName}/*"
    }
  ]
}
EOF
}

data "aws_caller_identity" "current" {}

locals {
  user_account_id = "${data.aws_caller_identity.current.account_id}"
}

resource "aws_iam_policy" "CircleCI-Code-Deploy" {
  name = "circleci_codedeploy_policy"
  policy = <<EOF
{
  "Version" : "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:RegisterApplicationRevision",
        "codedeploy:GetApplicationRevision"
      ],
      "Resource": "arn:aws:codedeploy:${var.region}:${local.user_account_id}:application:${aws_codedeploy_app.code_deploy_app.name}"
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.region}:${local.user_account_id}:deploymentgroup:${aws_codedeploy_app.code_deploy_app.name}/${aws_codedeploy_deployment_group.code_deploy_deployment_group.deployment_group_name}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:GetDeploymentConfig"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.region}:${local.user_account_id}:deploymentconfig:CodeDeployDefault.OneAtATime",
        "arn:aws:codedeploy:${var.region}:${local.user_account_id}:deploymentconfig:CodeDeployDefault.HalfAtATime",
        "arn:aws:codedeploy:${var.region}:${local.user_account_id}:deploymentconfig:CodeDeployDefault.AllAtOnce"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_user_policy_attachment" "circleci_ec2_policy_attach" {
  user = "circleci"
  policy_arn = "${aws_iam_policy.circleci_user_policy.arn}"
}

resource "aws_iam_user_policy_attachment" "circleci_s3_policy_attach" {
  user = "circleci"
  policy_arn = "${aws_iam_policy.CircleCI-Upload-To-S3.arn}"
}

resource "aws_iam_user_policy_attachment" "circleci_codedeploy_policy_attach" {
  user = "circleci"
  policy_arn = "${aws_iam_policy.CircleCI-Code-Deploy.arn}"
}

# IAM Role for CodeDeploy
resource "aws_iam_role" "code_deploy_role" {
  name = "CodeDeployServiceRole"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "codedeploy.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

# Attach the policy for CodeDeploy role
resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = "${aws_iam_role.code_deploy_role.name}"
}

#SNS topic and policies

resource "aws_sns_topic" "sns_recipes" {
  name = "SNS_Topic_Recipes"
}

resource "aws_sns_topic_policy" "sns_recipes_policy" {
  arn = "${aws_sns_topic.sns_recipes.arn}"
  policy = "${data.aws_iam_policy_document.sns-topic-policy.json}"
}

data "aws_iam_policy_document" "sns-topic-policy" {
  policy_id = "__default_policy_ID"

  statement {
    actions = [
      "SNS:Subscribe",
      "SNS:SetTopicAttributes",
      "SNS:RemovePermission",
      "SNS:Receive",
      "SNS:Publish",
      "SNS:ListSubscriptionsByTopic",
      "SNS:GetTopicAttributes",
      "SNS:DeleteTopic",
      "SNS:AddPermission",
    ]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceOwner"

      values = [
        "${local.user_account_id}",
      ]
    }

    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    resources = [
      "${aws_sns_topic.sns_recipes.arn}",
    ]

    sid = "__default_statement_ID"
  }
}

#Lambda Function
resource "aws_lambda_function" "sns_lambda_email" {
  filename      = "function.zip"
  function_name = "lambda_function_name"
  role          = "${aws_iam_role.iam_for_lambda.arn}"
  handler       = "index.handler"
  runtime       = "nodejs8.10"
  source_code_hash = "${filebase64sha256("function.zip")}"
}

#SNS topic subscription to Lambda
resource "aws_sns_topic_subscription" "lambda" {
  topic_arn = "${aws_sns_topic.sns_recipes.arn}"
  protocol  = "lambda"
  endpoint  = "${aws_lambda_function.sns_lambda_email.arn}"
}

#SNS Lambda permission
resource "aws_lambda_permission" "with_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.sns_lambda_email.function_name}"
  principal     = "sns.amazonaws.com"
  source_arn    = "${aws_sns_topic.sns_recipes.arn}"
}

#Lambda Policy
//TODO add exact resource names
resource "aws_iam_policy" "lambda_policy" {
 name        = "lambda_policy"
 description = "Policy for cloud watch and code deploy"
 policy      = <<EOF
{
   "Version": "2012-10-17",
   "Statement": [
       {
           "Effect": "Allow",
           "Action": [
               "logs:CreateLogGroup",
               "logs:CreateLogStream",
               "logs:PutLogEvents"
           ],
           "Resource": "*"
       },
       {
         "Sid": "LambdaDynamoDBAccess",
         "Effect": "Allow",
         "Action": [
             "dynamodb:GetItem",
             "dynamodb:PutItem",
             "dynamodb:UpdateItem"
         ],
         "Resource": "*"
       },
       {
         "Sid": "LambdaSESAccess",
         "Effect": "Allow",
         "Action": [
             "ses:VerifyEmailAddress",
             "ses:SendEmail",
             "ses:SendRawEmail"
         ],
         "Resource": "*"
       }
   ]
}
 EOF
}

#IAM Role for lambda with sns
resource "aws_iam_role" "iam_for_lambda" {
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

#Attach the policy for Lambda iam role
resource "aws_iam_role_policy_attachment" "lambda_role_policy_attach" {
  role       = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.lambda_policy.arn}"
}

//#Cloudwatch log group
//data "aws_cloudwatch_log_group" "lambda_cloudwatch_group" {
//  name = "csye6225_fall2019"
//}
//
//resource "aws_cloudwatch_log_stream" "lambda_cloudwatch_stream" {
//  name           = "lambda"
//  log_group_name = "${data.aws_cloudwatch_log_group.lambda_cloudwatch_group.name}"
//}


# Find a certificate issued by (not imported into) ACM
data "aws_acm_certificate" "aws_ssl_certificate" {
  domain      = "${var.env}.${var.domainName}"
  types       = ["AMAZON_ISSUED"]
  most_recent = true
}

data "aws_route53_zone" "route53" {
  name         = "${var.env}.${var.domainName}."
  # private_zone = false
}

resource "aws_route53_record" "recordset" {
  zone_id = "${data.aws_route53_zone.route53.zone_id}"
  name    = "${data.aws_route53_zone.route53.name}"
  type    = "A"
  
  alias {
    name    = "${aws_elb.application_loadbalancer.dns_name}"
    zone_id = "${aws_elb.application_loadbalancer.zone_id}"
    evaluate_target_health = true
  }
}

