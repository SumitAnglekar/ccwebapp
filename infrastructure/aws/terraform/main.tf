# AWS Networking module
module "networking" {
  source = "./modules/networking"

  # Input variables to the module
  env = "${var.env}"
  region  = "${var.region}"
  vpcCidrBlock = "${var.vpcCidrBlock}"
  subnetCidrBlock = "${var.subnetCidrBlock}"
  vpcName = "${var.vpcName}"
  
}

# Application module
module "application" {
  source = "./modules/application"

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
  subnetCidrBlock = "${var.subnetCidrBlock}"
  aws_db_subnet_group_name = module.networking.aws_db_subnet_group_name
  aws_ssh_key = "${var.aws_ssh_key}"

  vpc_id = module.networking.vpc_id
  subnet_id = module.networking.subnet_id
  subnet_id_list = module.networking.subnet_id_list
}