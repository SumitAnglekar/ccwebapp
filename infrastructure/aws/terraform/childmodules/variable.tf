variable "env" {
description = "Which environment do you want (options: dev,prod):"
} 
variable "region"{
    type="string"
}
variable "vpcName" {
    type="string"
}
variable "vpcCidrBlock" {
  type="string"
}


# variable "SubnetZones" {
#   type= "list"

# }


variable "subnetCidrBlock" {
  type = "list"

  default=[
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24"
  ]
}