#AWS Networking variables

#VPC_Cidr_Block variable defination
variable "vpcCidrBlock" {
  type        = "string"
}

#Subnet_Cidr_Block variable defination
#Type List
variable "subnetCidrBlock" {
  type        = "list"
}

variable "env" {
  type        = "string"
}

#AWS_Region variable defination
variable "region" {
  type        = "string"
}

#VPC_Variable name defination
variable "vpcName" {
  type        = "string"
}

