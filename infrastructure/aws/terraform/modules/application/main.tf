
#### SECURITY GROUP #####

#Application security group
resource "aws_security_group" "application" {
  name          = "application_security_group"
  vpc_id        = "${var.vpc_id}"
  ingress{
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks  = "${var.subnetCidrBlock}"
  }
  ingress{
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks  = "${var.subnetCidrBlock}"
  }
  ingress{
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks  = "${var.subnetCidrBlock}"
  }
  ingress{
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks  = "${var.subnetCidrBlock}"
  }
}

#Database security group
resource "aws_security_group" "database"{
  name          = "database_security_group"
  vpc_id        = "${var.vpc_id}"
}

#Database security group rule
resource "aws_security_group_rule" "database"{

  type        = "ingress"
  from_port   = 5432
  to_port     = 5432
  protocol    = "tcp"
  #cidr_blocks  = "${var.subnetCidrBlock}"
  
  source_security_group_id  = "${aws_security_group.application.id}"
  security_group_id         = "${aws_security_group.database.id}"
}

# S3 Bucket
resource "aws_s3_bucket" "bucket" {
    bucket = "webapp.${var.env}.${var.domainName}"
    acl = "private"
    force_destroy = "true"

    server_side_encryption_configuration {
      rule {
        apply_server_side_encryption_by_default {
          sse_algorithm     = "aws:kms"
        }
      }
    }

    lifecycle_rule {
      enabled = true

      transition {
        days = 30
        storage_class = "STANDARD_IA"
      }
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
  db_subnet_group_name = "${module.networking.aws_db_subnet_group_name}"

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
