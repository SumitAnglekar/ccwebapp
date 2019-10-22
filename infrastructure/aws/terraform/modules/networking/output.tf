output "vpc_id" {
    value = aws_vpc.main.id
}

output "aws_db_subnet_group_name" {
    value = "${aws_db_subnet_group.default.name}"
}