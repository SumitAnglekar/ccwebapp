variable "env" {
  description = "Enter AWS Environment"
  type        = "string"
}

variable "region" {
  description = "Enter AWS Region"
  type        = "string"
}

variable "vpcCidrBlock" {
  description = "Enter VPC Cidr Block"
  type        = "string"
}

variable "subnetCidrBlock" {
  description = "Enter Appropriate Subnet Cidr Block"
  type        = "list"
}


variable "vpcName" {
  description = "Enter VPC Name"
  type        = "string"
}