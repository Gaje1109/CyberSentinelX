variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket name for Terraform state"
  type        = string
  default     = "cybersentinelx-bits-capstone"
}

variable "state_key_prefix" {
  description = "Prefix/folder inside the bucket for your state file(s)"
  type        = string
  default     = "cybersentinelx-bits-capstone"
}