#Getting the appropriate aws_availability zone
data "aws_availability_zones" "available" {}

#Creating a VPC resource with a vpc name
resource "aws_vpc" "main" {
  cidr_block = "${var.vpcCidrBlock}"
  enable_dns_hostnames = true
  tags = {
    Name = "${var.vpcName}"
  }
}

#Creating 3 subnets with appropraite subnet names and subnet-cidr-block
resource "aws_subnet" "main" {
  count = 3

  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  cidr_block        = "${var.subnetCidrBlock[count.index]}"
  vpc_id            = "${aws_vpc.main.id}"
  map_public_ip_on_launch="true"
  tags = {
     Name ="${var.vpcName}.subnet.${count.index}"  
     }
}

#Creating an internet-gateway
resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.main.id}"

  tags = {
    Name = "${var.vpcName}.gateway"
  }
}

#Creating a route-table resource
resource "aws_route_table" "main" {
  vpc_id = "${aws_vpc.main.id}"
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.main.id}"
  }

  tags = {
    Name = "${var.vpcName}.RouteTable"
  }
}

#Mapping the subnets to appropriate route table
resource "aws_route_table_association" "main" {
  count = 3

  subnet_id      = "${aws_subnet.main.*.id[count.index]}"
  route_table_id = "${aws_route_table.main.id}"
}

resource "aws_db_subnet_group" "default" {
  name       = "main"
  subnet_ids = "${aws_subnet.main.*.id}"

  tags = {
    Name = "My DB subnet group"
  }
}

#RDS DB instance
resource "aws_db_instance" "myRDS" {
  allocated_storage    = 20
  storage_type         = "gp2"
  name                 = "${var.rdsDBName}"
  username             = "${var.rdsUsername}"
  password             = "${var.rdsPassword}"
  identifier = "${var.rdsInstanceIdentifier}"
  engine            = "postgres"
  engine_version    = "10.10"
  instance_class    = "db.t2.medium"
  # storage_encrypted = false
  port     = "5432"
  # vpc_security_group_ids = [data.aws_security_group.default.id]

  final_snapshot_identifier = "${var.rdsInstanceIdentifier}-SNAPSHOT"

  publicly_accessible = true
  multi_az = false

  tags = {
    Name        = "myRDS"
    Owner       = "${var.rdsOwner}"
  }

  # DB subnet group
  db_subnet_group_name = "${aws_db_subnet_group.default.name}"

}


#Dynamo db
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "${var.dynamoName}"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "${var.dynamoName}"
  }
}
