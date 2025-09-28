terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.54.1"
    }
  }
}
resource "aws_instance" "this" {
  ami = var.ami_id
  instance_type = var.instance_type
  subnet_id = var.subnet_id
  vpc_security_group_ids = var.security_group_id
  key_name = var.key_name
  iam_instance_profile = var.iam_instance_profile
  associate_public_ip_address = true

  tags = {
    Name = "${var.project_name}-app"
  }
}