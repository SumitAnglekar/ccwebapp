provider "aws" {
  profile    = "${var.env}"
  region     = "${var.region}"
}

