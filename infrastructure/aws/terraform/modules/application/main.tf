##############################
# S3 Bucket for recipe images
##############################
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

##############################
# AWS LAUNCH CONFIGURATION
##############################
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
                                      aws_profile = "${var.env}",
                                      webapp_domain = "${var.env}.${var.domainName}",
                                      sns_topic_arn = "${aws_sns_topic.sns_recipes.arn}"
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

##############################
## AUTOSCALING GROUP
##############################
resource "aws_autoscaling_group" "autoscaling" {
  name                 = "terraform-asg-example"
  launch_configuration = "${aws_launch_configuration.asg_launch_config.name}"
  min_size             = 3
  max_size             = 10
  default_cooldown     = 60
  desired_capacity     = 3
  # load_balancers       = ["${aws_lb.appLoadbalancer.name}"]
  vpc_zone_identifier = ["${var.subnet_id}"]
  
  target_group_arns    = ["${aws_lb_target_group.alb-target-group.arn}"]

  tag {
    key                 = "Name"
    value               = "myEC2Instance"
    propagate_at_launch = true
  }
}

resource "aws_lb_target_group" "alb-target-group" {  
  name     = "alb-target-group"  
  port     = "8080"  
  protocol = "HTTP"  
  vpc_id   = "${var.vpc_id}"   
  tags     = {    
    name = "alb-target-group"    
  }   
  health_check {    
    healthy_threshold   = 3
    unhealthy_threshold = 5
    timeout             = 5
    interval            = 30
    path                = "/"
    port                = "8080"
  }
}

#### SECURITY GROUP #####
##############################

##LOAD BALANCER SECURITY GROUP
resource "aws_security_group" "loadbalancer" {
  name          = "loadbalancer_security_group"
  vpc_id        = "${var.vpc_id}"
  # ingress{
  #   from_port   = 22
  #   to_port     = 22
  #   protocol    = "tcp"
  #   cidr_blocks  = ["0.0.0.0/0"]
  # }
  # ingress{
  #   from_port   = 80
  #   to_port     = 80
  #   protocol    = "tcp"
  #   cidr_blocks  = ["0.0.0.0/0"]
  # }
  ingress{
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  # ingress{
  #   from_port   = 8080
  #   to_port     = 8080
  #   protocol    = "tcp"
  #   cidr_blocks  = ["0.0.0.0/0"]
  # }
  # // Egress is used here to communicate anywhere with any given protocol
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
  evaluation_periods  = "3"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "10"
  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.autoscaling.name}"
  }
  alarm_description = "This metric monitors ec2 cpu utilization"
  alarm_actions     = ["${aws_autoscaling_policy.WebServerScaleUpPolicy.arn}"]
}

resource "aws_cloudwatch_metric_alarm" "CPUAlarmLow" {
  alarm_name          = "CPUAlarmLow"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "3"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "8"
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
  ingress{
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  tags          = {
    Name        = "Application Security Group"
    Environment = "${var.env}"
  }
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
# resource "aws_elb" "appLoadbalancer" {
#   name               = "ApplicationLoadbalancer"
#   security_groups    = ["${aws_security_group.loadbalancer.id}"]
#   subnets            = ["${var.subnet_id}"]
  
#   health_check {
#     healthy_threshold = 3
#     unhealthy_threshold = 5
#     timeout = 5
#     interval = 30
#     target = "HTTP:8080/"
#   }

#   listener {
#     instance_port      = 8080
#     instance_protocol  = "http"
#     lb_port            = 443
#     lb_protocol        = "https"
#     ssl_certificate_id = "${data.aws_acm_certificate.aws_ssl_certificate.arn}"
#   }

# }

resource "aws_lb" "appLoadbalancer" {
  name               = "appLoadbalancer"
  internal           = false
  load_balancer_type = "application"
  security_groups    = ["${aws_security_group.loadbalancer.id}"]
  subnets            = "${var.subnet_id_list}"
  ip_address_type    = "ipv4"
  tags = {
    Environment = "${var.env}"
    Name = "appLoadbalancer"
  }
}

resource "aws_lb_listener" "webapp_listener" {
  load_balancer_arn = "${aws_lb.appLoadbalancer.arn}"
  port              = "443"
  protocol          = "HTTPS"
  certificate_arn   = "${data.aws_acm_certificate.aws_ssl_certificate.arn}"

  default_action {
    type             = "forward"
    target_group_arn = "${aws_lb_target_group.alb-target-group.arn}"
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

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  tags = {
    Name        = "${var.dynamoName}"
  }
}

#CodeDeploy App and Group for webapp
resource "aws_codedeploy_app" "code_deploy_app" {
  compute_platform = "Server"
  name             = "csye6225-webapp"
}

resource "aws_codedeploy_deployment_group" "code_deploy_deployment_group" {
  app_name              = "${aws_codedeploy_app.code_deploy_app.name}"
  deployment_group_name = "csye6225-webapp-deployment"
  deployment_config_name = "CodeDeployDefault.AllAtOnce"
  service_role_arn      = "${aws_iam_role.code_deploy_role.arn}"
  autoscaling_groups    = ["${aws_autoscaling_group.autoscaling.name}"]

  load_balancer_info {
    target_group_info {
      name = "${aws_lb_target_group.alb-target-group.name}"
    }
  }

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

  depends_on = [aws_codedeploy_app.code_deploy_app]
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
      "Resource":[ 
        "arn:aws:s3:::codedeploy.${var.env}.${var.domainName}/*",
        "arn:aws:s3:::lambda.${var.env}.${var.domainName}/*"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_policy" "CircleCI-Lambda" {
  name = "circleci_s3_policy"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "lambda:*"
        ],
        
      "Resource": "arn:aws:lambda:${var.region}:${local.user_account_id}:function:${aws_lambda_function.sns_lambda_email.function_name}"
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

# Attach the policy for CodeDeploy role for webapp
resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = "${aws_iam_role.code_deploy_role.name}"
}

# # Attach the policy for CodeDeploy role for lambda
resource "aws_iam_role_policy_attachment" "AWSCodeDeployRoleforLambda" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRoleForLambda"
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
  domain = "*.${var.domainName}"
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
    name    = "${aws_lb.appLoadbalancer.dns_name}"
    zone_id = "${aws_lb.appLoadbalancer.zone_id}"
    evaluate_target_health = true
  }
}

#Firewall Config

resource "aws_cloudformation_stack" "firewallowasp" {
    name: "firewallowasp"

    parameters{
      
    }

      template_body = <<STACK
      { 
         "Parameters": {
            "IPtoBlock1": {
                "Description": "IPAddress to be blocked",
                "Default": "155.33.133.6/32",
                "Type": "String"
            },
            "IPtoBlock2": {
                "Description": "IPAddress to be blocked",
                "Default": "192.0.7.0/24",
                "Type": "String"
            }
          },
        "Resources": {
        "wafrSQLiSet": {
            "Type": "AWS::WAFRegional::SqlInjectionMatchSet",
            "Properties": {
                "Name": "wafrSQLiSet",
                "SqlInjectionMatchTuples": [
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "BODY"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "BODY"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "cookie"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "cookie"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "Authorization"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "Authorization"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    }
                ]
            }
        },
        "wafrSQLiRule": {
            "Type": "AWS::WAFRegional::Rule",
            "DependsOn": [
                "wafrSQLiSet"
            ],
            "Properties": {
                "MetricName": "wafrSQLiRule",
                "Name": "wafr-SQLiRule",
                "Predicates": [
                    {
                        "Type": "SqlInjectionMatch",
                        "Negated": false,
                        "DataId": {
                            "Ref": "wafrSQLiSet"
                        }
                    }
                ]
            }
        },
          "MyIPSetWhiteList": {
            "Type": "AWS::WAFRegional::IPSet",
            "Properties": {
                "Name": "WhiteList IP Address Set",
                "IPSetDescriptors": [
                    {
                        "Type": "IPV4",
                        "Value": "155.33.135.11/32"
                    },
                    {
                        "Type": "IPV4",
                        "Value": "192.0.7.0/24"
                    }
                ]
            }
        },
        "MyIPSetWhiteListRule": {
            "Type": "AWS::WAFRegional::Rule",
            "Properties": {
                "Name": "WhiteList IP Address Rule",
                "MetricName": "MyIPSetWhiteListRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "MyIPSetWhiteList"
                        },
                        "Negated": false,
                        "Type": "IPMatch"
                    }
                ]
            }
        },
        "myIPSetBlacklist": {
            "Type": "AWS::WAFRegional::IPSet",
            "Properties": {
                "Name": "myIPSetBlacklist",
                "IPSetDescriptors": [
                    {
                        "Type": "IPV4",
                        "Value": {
                            "Ref": "IPtoBlock1"
                        }
                    },
                    {
                        "Type": "IPV4",
                        "Value": {
                            "Ref": "IPtoBlock2"
                        }
                    }
                ]
            }
        },
        "myIPSetBlacklistRule": {
            "Type": "AWS::WAFRegional::Rule",
            "DependsOn": [
                "myIPSetBlacklist"
            ],
            "Properties": {
                "Name": "Blacklist IP Address Rule",
                "MetricName": "myIPSetBlacklistRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "myIPSetBlacklist"
                        },
                        "Negated": false,
                        "Type": "IPMatch"
                    }
                ]
            }
        },
         "MyScanProbesSet": {
            "Type": "AWS::WAFRegional::IPSet",
            "Properties": {
                "Name": "MyScanProbesSet"
            }
        },
        "MyScansProbesRule": {
            "Type": "AWS::WAFRegional::Rule",
            "DependsOn": "MyScanProbesSet",
            "Properties": {
                "Name": "MyScansProbesRule",
                "MetricName": "SecurityAutomationsScansProbesRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "MyScanProbesSet"
                        },
                        "Negated": false,
                        "Type": "IPMatch"
                    }
                ]
            }
        },
        "DetectXSS": {
            "Type": "AWS::WAFRegional::XssMatchSet",
            "Properties": {
                "Name": "XssMatchSet",
                "XssMatchTuples": [
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TextTransformation": "URL_DECODE"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TextTransformation": "HTML_ENTITY_DECODE"
                    }
                ]
            }
        },
        "XSSRule": {
            "Type": "AWS::WAFRegional::Rule",
            "Properties": {
                "Name": "XSSRule",
                "MetricName": "XSSRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "DetectXSS"
                        },
                        "Negated": false,
                        "Type": "XssMatch"
                    }
                ]
            }
        },
        "sizeRestrict": {
            "Type": "AWS::WAFRegional::SizeConstraintSet",
            "Properties": {
                "Name": "sizeRestrict",
                "SizeConstraints": [
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TextTransformation": "NONE",
                        "ComparisonOperator": "GT",
                        "Size": "512"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TextTransformation": "NONE",
                        "ComparisonOperator": "GT",
                        "Size": "1024"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "BODY"
                        },
                        "TextTransformation": "NONE",
                        "ComparisonOperator": "GT",
                        "Size": "204800"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "cookie"
                        },
                        "TextTransformation": "NONE",
                        "ComparisonOperator": "GT",
                        "Size": "4096"
                    }
                ]
            }
        },
        "reqSizeRule": {
            "Type": "AWS::WAFRegional::Rule",
            "DependsOn": [
                "sizeRestrict"
            ],
            "Properties": {
                "MetricName": "reqSizeRule",
                "Name": "reqSizeRule",
                "Predicates": [
                    {
                        "Type": "SizeConstraint",
                        "Negated": false,
                        "DataId": {
                            "Ref": "sizeRestrict"
                        }
                    }
                ]
            }
        },
       "PathStringSetReferers": {
            "Type": "AWS::WAFRegional::ByteMatchSet",
            "Properties": {
                "Name": "Path String Referers Set",
                "ByteMatchTuples": [
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": "../",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": "../",
                        "TextTransformation": "HTML_ENTITY_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TargetString": "../",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TargetString": "../",
                        "TextTransformation": "HTML_ENTITY_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": "://",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": "://",
                        "TextTransformation": "HTML_ENTITY_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TargetString": "://",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "QUERY_STRING"
                        },
                        "TargetString": "://",
                        "TextTransformation": "HTML_ENTITY_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    }
                ]
            }
        },
        "PathStringSetReferersRule": {
            "Type": "AWS::WAFRegional::Rule",
            "Properties": {
                "Name": "PathStringSetReferersRule",
                "MetricName": "PathStringSetReferersRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "PathStringSetReferers"
                        },
                        "Negated": false,
                        "Type": "ByteMatch"
                    }
                ]
            }
        },
        "BadReferers": {
            "Type": "AWS::WAFRegional::ByteMatchSet",
            "Properties": {
                "Name": "Bad Referers",
                "ByteMatchTuples": [
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "cookie"
                        },
                        "TargetString": "badrefer1",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "HEADER",
                            "Data": "authorization"
                        },
                        "TargetString": "QGdtYWlsLmNvbQ==",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "CONTAINS"
                    }
                ]
            }
        },
        "BadReferersRule": {
            "Type": "AWS::WAFRegional::Rule",
            "Properties": {
                "Name": "BadReferersRule",
                "MetricName": "BadReferersRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "BadReferers"
                        },
                        "Negated": false,
                        "Type": "ByteMatch"
                    }
                ]
            }
        },
        "ServerSideIncludesSet": {
            "Type": "AWS::WAFRegional::ByteMatchSet",
            "Properties": {
                "Name": "Server Side Includes Set",
                "ByteMatchTuples": [
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": "/includes",
                        "TextTransformation": "URL_DECODE",
                        "PositionalConstraint": "STARTS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".cfg",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".conf",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".config",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".ini",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".log",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".bak",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".bakup",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    },
                    {
                        "FieldToMatch": {
                            "Type": "URI"
                        },
                        "TargetString": ".txt",
                        "TextTransformation": "LOWERCASE",
                        "PositionalConstraint": "ENDS_WITH"
                    }
                ]
            }
        },
        "ServerSideIncludesRule": {
            "Type": "AWS::WAFRegional::Rule",
            "Properties": {
                "Name": "ServerSideIncludesRule",
                "MetricName": "ServerSideIncludesRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "ServerSideIncludesSet"
                        },
                        "Negated": false,
                        "Type": "ByteMatch"
                    }
                ]
            }
        },
         "WAFAutoBlockSet": {
            "Type": "AWS::WAFRegional::IPSet",
            "Properties": {
                "Name": "Auto Block Set"
            }
        },
        "MyAutoBlockRule": {
            "Type": "AWS::WAFRegional::Rule",
            "DependsOn": "WAFAutoBlockSet",
            "Properties": {
                "Name": "Auto Block Rule",
                "MetricName": "AutoBlockRule",
                "Predicates": [
                    {
                        "DataId": {
                            "Ref": "WAFAutoBlockSet"
                        },
                        "Negated": false,
                        "Type": "IPMatch"
                    }
                ]
            }
        },
         "MyWebACL": {
            "Type": "AWS::WAFRegional::WebACL",
            "Properties": {
                "Name": "MyWebACL",
                "DefaultAction": {
                    "Type": "ALLOW"
                },
                "MetricName": "MyWebACL",
                "Rules": [
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 1,
                        "RuleId": {
                            "Ref": "reqSizeRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "ALLOW"
                        },
                        "Priority": 2,
                        "RuleId": {
                            "Ref": "MyIPSetWhiteListRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 3,
                        "RuleId": {
                            "Ref": "myIPSetBlacklistRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 4,
                        "RuleId": {
                            "Ref": "MyAutoBlockRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 5,
                        "RuleId": {
                            "Ref": "wafrSQLiRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 6,
                        "RuleId": {
                            "Ref": "BadReferersRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 7,
                        "RuleId": {
                            "Ref": "PathStringSetReferersRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 8,
                        "RuleId": {
                            "Ref": "ServerSideIncludesRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 9,
                        "RuleId": {
                            "Ref": "XSSRule"
                        }
                    },
                    {
                        "Action": {
                            "Type": "BLOCK"
                        },
                        "Priority": 10,
                        "RuleId": {
                            "Ref": "MyScansProbesRule"
                        }
                    }
                ]
            }
        },
        "MyWebACLAssociation": {
            "Type": "AWS::WAFRegional::WebACLAssociation",
            "DependsOn": [
                "MyWebACL"
            ],
            "Properties": {
                "ResourceArn": {
                    "Fn::ImportValue": "ApplicationLoadBalancer"
                },
                "WebACLId": {
                    "Ref": "MyWebACL"
                }
            }
        }
    }
  }                    
  STACK
}

#Firewall config ended (Need to pass loadbalancer resource ARN in the final rule and also update the IP for whitelist and blacklist)

