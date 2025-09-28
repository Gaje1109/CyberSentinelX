
## Security Group for Application Layer
resource "aws_security_group" "app"{
  name = "${var.project_name}-app-sg"
  description = "Allow web and SSH"
  vpc_id = var.vpc_id

  ## Allow SSH Access
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [var.ssh_ingress_cidr]
  }

  ## Allow HTTP traffic (for Web Servers)
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ## Allow HTTPs traffic (for secure web apps)
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ## Allow all outbound traffic
  ingress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}