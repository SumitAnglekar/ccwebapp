variable "env"{
    description="Enter AWS Environment"
    type = "string"
}

variable "region"{
    description="Enter AWS Region"
    type="string"
}

variable "vpcCidrBlock"{
    description="Enter VPC Cidr Block"
    type = "string"
}

variable "subnetCdr"{
    description="Enter Appropriate Subnet Cidr Block"
    type = "list"
}

variable "subnetZone"{
    description="Enter appropriate subnet zone"
    type= "list"
}

variable "subnetName" {
    description="Enter appropriate subnet name"
    type= "string"
}


variable "vpcName"{
    description="Enter VPC Name"
    type= "string"
}

variable "gatewayName"{
    description="Enter appropriate Gateway Name"
    type= "string"
}