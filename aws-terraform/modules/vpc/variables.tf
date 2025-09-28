## Variables for VPC MODULE

variable "project_name" {
  type = string
}

variable "vpc_cidr" {
  type = string
  default = "10.0.0.0/16"
}

variable "azs" {
  type = list(string)
}

variable "public_subnets" {
  type = list(string)
}