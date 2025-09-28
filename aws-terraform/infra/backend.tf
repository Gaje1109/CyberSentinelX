## Step 1 : Tells terraform where to keep state file. Read during terraform init

terraform {
  backend "s3" {
    bucket  = "cybersentinelx-bits-capstone" # from bootstrap output
    key     = "cybersentinelx-bits-capstone/terraform.tfstate"
    region  = "ap-south-1"
    encrypt = true
  }
}