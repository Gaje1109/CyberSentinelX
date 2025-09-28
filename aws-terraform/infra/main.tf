## Step 5: This is the orchestrator that calls child modules

data "aws_availability_zones" "available" {}

## Calling VPC module
module "vpc" {
  source         = "../modules/vpc"
  vpc_cidr       = var.vpc_cidr
  project_name   = var.project_name
  azs            = var.azs
  public_subnets = var.public_subnets
}

## Calling Security Groups  module
module "security_groups" {
  source           = "../modules/security_groups"
  vpc_id           = module.vpc.vpc_id
  ssh_ingress_cidr = var.ssh_ingress_cidr
  project_name     = var.project_name
}

## Calling S3 Bucket module
module "s3_bucket" {
  source       = "../modules/s3_bucket"
  project_name = var.project_name
}

## Calling IAM Instance Role module
module "iam_instance_role" {
  source         = "../modules/iam_instance_role"
  project_name   = var.project_name
  s3_bucket_name = module.s3_bucket.bucket_name
}

# Latest Ubuntu
data "aws_ssm_parameter" "ubuntu_2404_amd64_gp3" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

# Use this ID in your EC2 module
locals {
  ubuntu_ami_id = data.aws_ssm_parameter.ubuntu_2404_amd64_gp3.value
}

## Calling EC2 module
module "ec2_app" {
  source               = "../modules/ec2_app"
  project_name         = var.project_name
  ami_id               = local.ubuntu_ami_id
  instance_type        = var.instance_type
  subnet_id            = module.vpc.public_subnets_ids[0]
  security_group_id    = [module.security_groups.app_sg_id]
  key_name             = var.ssh_key_name
  iam_instance_profile = module.iam_instance_role.instance_profile
  #user_data = local.user_data
}