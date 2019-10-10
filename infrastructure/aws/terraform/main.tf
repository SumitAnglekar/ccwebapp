data "aws_availability_zones" "available" {}

resource "aws_vpc" "main" {
  cidr_block           = "${var.vpcCidrBlock}"
  enable_dns_hostnames = false
  tags = {
    Name = "${var.vpcName}"
  }
}

resource "aws_subnet" "main" {
  count = 3

  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  cidr_block        = "${var.subnetCidrBlock[count.index]}"
  vpc_id            = "${aws_vpc.main.id}"

  tags = {
     Name ="${var.vpcName}.subnet"  
     }
}

resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.main.id}"

  tags = {
    Name = "${var.vpcName}.gateway"
  }
}

resource "aws_route_table" "main" {
  vpc_id = "${aws_vpc.main.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.main.id}"
  }
}

resource "aws_route_table_association" "main" {
  count = 3

  subnet_id      = "${aws_subnet.main.*.id[count.index]}"
  route_table_id = "${aws_route_table.main.id}"
}
