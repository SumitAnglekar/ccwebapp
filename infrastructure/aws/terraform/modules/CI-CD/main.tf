resource "aws_codedeploy_app" "example" {
  compute_platform = "ECS"
  name             = "csye6225-webapp"
}

resource "aws_codedeploy_deployment_group" "example" {
  app_name              = "${aws_codedeploy_app.example.name}"
  deployment_group_name = "csye6225-webapp-deployment"
  deployment_config_name = "CodeDeployDefault.AllAtOnce"
  service_role_arn      = "CodeDeployServiceRole"

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

  alarm_configuration {
    alarms  = ["my-alarm-name"]
    enabled = true
  }
}