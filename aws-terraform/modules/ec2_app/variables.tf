variable "project_name" {
  type = string
}

variable "ami_id" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "subnet_id"{
  type = string
}

variable "security_group_id" {
  type = list(string)
}

variable "key_name" {
  type = string
}

variable "iam_instance_profile" {
  type = string
}

# variable "user_date" {
#   type = string
# }