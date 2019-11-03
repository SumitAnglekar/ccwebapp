resource "aws_codedeploy_app" "code_deploy_app" {
  compute_platform = "${var.compute_platform}"
  name             = "${var.app_name}"
}

resource "aws_iam_role" "code_deploy_role" {
  name = "${var.service_role}"

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

resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = "${aws_iam_role.code_deploy_role.name}"
}


resource "aws_codedeploy_deployment_group" "code_deploy_deployment_group" {
  app_name              = "${aws_codedeploy_app.code_deploy_app.name}"
  deployment_group_name = "${var.deployment_group_name}"
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

#   trigger_configuration {
#     trigger_events     = ["DeploymentFailure"]
#     trigger_name       = "example-trigger"
#     trigger_target_arn = "${aws_sns_topic.example.arn}"
#   }

  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }

  alarm_configuration {
    alarms  = ["my-alarm-name"]
    enabled = true
  }
}