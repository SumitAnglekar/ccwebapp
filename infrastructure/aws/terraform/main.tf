# AWS Networking module
module "networking" {
  source = "./modules/networking"

  # Input variables to the module
  vpcCidrBlock = "${var.vpcCidrBlock}"
  subnetCidrBlock = "${var.subnetCidrBlock}"
  vpcName = "${var.vpcName}"
  
}

# Application module
module "application" {
  source = "./modules/application"
  source = "./modules/networking"
  # Input variables to the module
  env = "${var.env}"
  region = "${var.region}"
  domainName = "${var.domainName}"
  rdsOwner = "${var.rdsOwner}"
  rdsInstanceIdentifier = "${var.rdsInstanceIdentifier}"
  rdsUsername = "${var.rdsUsername}"
  rdsPassword = "${var.rdsPassword}"
  rdsDBName = "${var.rdsDBName}"
  dynamoName = "${var.dynamoName}"

}