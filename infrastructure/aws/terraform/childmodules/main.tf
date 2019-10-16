module "vpc" {
 
  source = "../"

  SubnetZones = "${var.SubnetZones}"
  vpcCidrBlock = "${var.vpcCidrBlock}"
  vpcName = "${var.vpcName}"
  subnetCidrBlock = "${var.subnetCidrBlock}"

  # SubnetZones = "${var.SubnetZones}"
  # SubnetCidrBlock = "${var.Cidrblock}"
  # myvpc_id="${data.aws_vpc.selected.id}"
}