## Step 2: Declares the AWS Provider and its version, Sets the region (reads var.region from variables)

terraform {
  required_version = ">1.6.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">=5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = ">=3.5"
    }

  }

}

provider "aws" {
  region = var.region
}