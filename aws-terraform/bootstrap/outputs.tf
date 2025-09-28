output "state_bucket" {
  description = "Name of the S3 bucket holding Terraform state"
  value       = aws_s3_bucket.tf_state.bucket
}

output "state_key" {
  description = "Suggested object key for your main stack's state file"
  value       = "${var.state_key_prefix}/terraform.tfstate"
}

output "state_uri" {
  description = "Full s3:// URI to the state object (once the backend uses this key)"
  value       = "s3://${aws_s3_bucket.tf_state.bucket}/${var.state_key_prefix}/terraform.tfstate"
}