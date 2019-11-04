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
}

#VPC_Variable name defination
variable "vpcName" {
  type        = "string"
}

#Domain name to be used for S3 Bucket name
variable "domainName" {
    type    = "string"
}

#RDS owner
variable "rdsOwner"{
  type      = "string"
  default = "csye6225"
}

#RDS Instance Identifier
variable "rdsInstanceIdentifier"{
  type      = "string"
  default = "csye6225-fall2019"
}

#RDS username
variable "rdsUsername"{
  type      = "string"
  default = "dbuser"
}

#RDS Password
variable "rdsPassword"{
  type      = "string"  
}

#RDS DB Variable
variable "rdsDBName"{
  type      = "string"
  default = "csye6225"
}

#Dynamo Table Variable
variable "dynamoName"{
  type      = "string"
  default = "csye6225"
}

# ami id
variable "ami" {
  type = "string"
  description = "The AMI to use for the instance"
}

# SSH key
variable "aws_ssh_key" {
  type = "string"
  description = "The ssh key pair name configured in AWS"
}
