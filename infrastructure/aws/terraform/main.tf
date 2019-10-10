resource "aws_vpc" "main" {
    cidr_block = "${var.vpcCidrBlock}"
    enable_dns_hostnames = false
    tags = {
        Name = "${var.vpcName}"  
    }
}

resource "aws_subnet" "main" {
  count = 3

  availability_zone = "${var.subnetZone[count.index]}"
  cidr_block        = "${var.subnetCdr[count.index]}"
  vpc_id            = "${aws_vpc.main.id}"

  tags = "${
    map(
     "Name", "${var.subnetName}-${count.index}"
    )
  }"
}


resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.main.id}"

  tags = {
    Name = "${var.gatewayName}"
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