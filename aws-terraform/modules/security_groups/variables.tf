variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "ssh_ingress_cidr" {
  type = string
  default = "0.0.0.0/0"
}