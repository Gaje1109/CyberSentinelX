terraform {
  required_providers {
    random = {
      source  = "hashicorp/random"
      version = "3.6.2"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "5.54.1"
    }
  }
}

## Random suffic for bucket name
resource "random_id" "suffix" {
  byte_length = 3
}

## S3 bucket (for static assets, files, backups, etc)
resource "aws_s3_bucket" "this" {
  bucket = "${var.project_name}-static-${random_id.suffix.hex}"

  tags = {
    Name = "${var.project_name}-static"
  }
}

## Enable Versioning
resource "aws_s3_bucket_versioning" "v" {
  bucket = aws_s3_bucket.this.id
  versioning_configuration {
    status = "Enabled"
  }
}

## Server-side encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "enc" {
  bucket = aws_s3_bucket.this.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

## Block all public access
resource "aws_s3_bucket_public_access_block" "pab" {
  bucket = aws_s3_bucket.this.id
  block_public_acls = true
  block_public_policy = true
  ignore_public_acls = true
  restrict_public_buckets = true
}