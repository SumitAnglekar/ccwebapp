#AWS_Environment variable defination

#VPC_Cidr_Block variable defination
variable "vpcCidrBlock" {
  type        = "string"
}

#Subnet_Cidr_Block variable defination
#Type List
variable "subnetCidrBlock" {
  type        = "list"
}

#VPC_Variable name defination
variable "vpcName" {
  type        = "string"
}