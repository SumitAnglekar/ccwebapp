#This file has variable for defined or input  aws-profile and aws-regions

provider "aws" {
  #aws_profile and aws_region shall be defined in '.config' and '.credential' 
  #file while setting up the CLI environment
  #and their values are passed via command line
  profile = "${var.env}"
  region  = "${var.region}"
}
