terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.54.1"
    }
  }
}
## Declaration for VPC MODULE

## Create a VPC
resource "aws_vpc" "this" {
  cidr_block = var.vpc_cidr
  enable_dns_support =  true
  enable_dns_hostnames = true

  tags = {
  Name = "${var.project_name}-vpc"
    }
}

## Internet Gateway
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

## Public Subnets
resource "aws_subnet" "public" {
  ## Create multiple public subnets dynamically based on cidr list provided
  for_each = {for idx, cidr in var.public_subnets : idx => cidr}
  vpc_id = aws_vpc.this.id
  cidr_block = each.value
  availability_zone = element(var.azs, tonumber(each.key))
  map_public_ip_on_launch = true

  tags = {
  Name = "${var.project_name}-public-${each.key}"
    }
}

## Public Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

## Route for Internet Access
resource "aws_route" "default_igw" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.igw.id
}


## Associate Subnets with Route Table
resource "aws_route_table_association" "public" {
  for_each       = aws_subnet.public
  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}