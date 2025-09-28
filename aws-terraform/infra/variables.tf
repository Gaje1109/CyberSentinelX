## Step 3: Defines all input variables the root modules expects (project name, VPC CIDRs, repo URLs, etc)

variable "project_name" {
  type    = string
  default = "CyberSentinelX"
}
variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "azs" {
  description = "Availability Zones to use"
  type        = list(string)
  default     = ["ap-south-1a", "ap-south-1b"]
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "public_subnets" {
  type    = list(string)
  default = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "ssh_ingress_cidr" {
  description = "Your IP/CIDR allowed to SSH"
  type        = string
  default     = "0.0.0.0/0"
}

variable "ssh_key_name" {
  description = "Existing EC2 key pair name in aws to SSH into the instance"
  type        = string
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "app_repo_url" {
  type        = string
  description = "Git URL of CyberSentinelX app"
}

variable "app_branch" {
  type    = string
  default = "main"
}
variable "python_dir" {
  type    = string
  default = "."
}
variable "java_dir" {
  type    = string
  default = "urlExelScanner"
}

variable "associate_eip" {
  type    = bool
  default = true
}