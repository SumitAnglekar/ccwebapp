#AWS_Environment variable defination
variable "env" {
  description = "Enter AWS Environment"
  type        = "string"
}

#AWS_Region variable defination
variable "region" {
  description = "Enter AWS Region"
  type        = "string"
}

#VPC_Cidr_Block variable defination
variable "vpcCidrBlock" {
  description = "Enter VPC Cidr Block"
  type        = "string"
}

#Subnet_Cidr_Block variable defination
#Type List
variable "subnetCidrBlock" {
  description = "Enter Appropriate Subnet Cidr Block"
  type        = "list"
}

#VPC_Variable name defination
variable "vpcName" {
  description = "Enter VPC Name"
  type        = "string"
}