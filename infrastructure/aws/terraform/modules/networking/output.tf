output "vpc_id" {
    value = aws_vpc.main.id
}

output "subnet_id" {
    value = aws_subnet.main.*.id[1]
}

output "subnet_id_list" {
    value = aws_subnet.main.*.id
}

output "aws_db_subnet_group_name" {
    value = aws_db_subnet_group.main.name
}
