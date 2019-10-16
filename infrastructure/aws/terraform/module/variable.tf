#AWS_Environment variable defination


variable "env" {
  type        = "string"
}

#AWS_Region variable defination
variable "region" {
  type        = "string"
}

#VPC_Cidr_Block variable defination
variable "vpcCidrBlock" {
  type        = "string"
}

#Subnet_Cidr_Block variable defination
#Type List
variable "subnetCidrBlock" {
  type        = "list"
  default = [
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24"
  ]
}

#VPC_Variable name defination
variable "vpcName" {
  type        = "string"
}