module "vpc" {
 
  source = "../"
  vpcCidrBlock = "${var.vpcCidrBlock}"
  vpcName = "${var.vpcName}"
  subnetCidrBlock = "${var.subnetCidrBlock}"
  rdsPassword = "${var.rdsPassword}"
}