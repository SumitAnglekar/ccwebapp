resource "aws_codedeploy_app" "example" {
  compute_platform = "${var.compute_platform}"
  name             = "${var.app_name}"
}

resource "aws_codedeploy_deployment_group" "example" {
  app_name              = "${aws_codedeploy_app.example.name}"
  deployment_group_name = "${var.deployment_group_name}"
  deployment_config_name = "${var.deployment_config_name}"
  service_role_arn      = "${var.service_role}"

  ec2_tag_filter {
    key   = "filterkey"
    type  = "KEY_AND_VALUE"
    value = "filtervalue"
  }
    
  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
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

  load_balancer_info {
    # elb_info {
    #   name = "${aws_elb.example.name}"
    # }
  }

  alarm_configuration {
    alarms  = ["my-alarm-name"]
    enabled = true
  }
}