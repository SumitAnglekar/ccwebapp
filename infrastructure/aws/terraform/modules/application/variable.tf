# AWS S3 Bucket Variables

variable "env" {
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