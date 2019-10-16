#AWS_Environment variable defination

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