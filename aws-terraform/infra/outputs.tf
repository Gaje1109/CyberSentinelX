## Step 6: Bubbles up handy info from child modules to your terminal

# output "ec2_public_ip" {
#   value = var.associate_eip ? module.eip[0].public_ip : module.ec2.public_ip
# }

output "app_url" {
  value = "http://${module.ec2_app.public_dns}/"
}


output "ec2_public_dns" {
  value = module.ec2_app.public_dns
}

output "s3_bucket" {
  value = module.s3_bucket.bucket_name
}