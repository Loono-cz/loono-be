terraform {
  required_version = "= 0.15.4"

  required_providers {
    aws = ">= 3.28.0"
  }

  backend "s3" {
    # For every new deployment this bucket needs to be changed, because AWS bucket names are
    # globally unique.
    bucket = "loono-terraform-backend"
    key    = "terraform.tfstate"
    region = "eu-north-1"
  }
}

provider "aws" {
  region = var.aws-region
}

# ----------------
# VPC
# ----------------

resource "aws_vpc" "vpc" {
  cidr_block = "10.0.0.0/16"

  enable_dns_hostnames = true

  tags = {
    Name = var.codename
  }
}

resource "aws_subnet" "private" {
  cidr_block        = "10.0.0.0/20"
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "${var.aws-region}a"

  tags = {
    Name = "${var.codename}-private"
  }
}

resource "aws_subnet" "public" {
  cidr_block        = "10.0.16.0/20"
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "${var.aws-region}a"

  tags = {
    Name = "${var.codename}-public"
  }
}

resource "aws_subnet" "db-private" {
  cidr_block        = "10.0.32.0/20"
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "${var.aws-region}b"

  tags = {
    Name = "${var.codename}-db-private"
  }
}

# ---------
# Internet Gateway
# ---------

resource "aws_internet_gateway" "internet-gateway" {
  vpc_id = aws_vpc.vpc.id
  tags   = {
    Name = "${var.codename}-internet-gateway"
  }
}

resource "aws_eip" "nat-ip" {
  vpc = true
}

# Pick the most recent NAT Ami.
# It'll deploy most recent version with every `terraform apply`
data "aws_ami" "nat-gateway" {
  owners      = [
    "amazon"]
  most_recent = true
  filter {
    name   = "name"
    values = [
      "amzn-ami-vpc-nat-*-x86_64-ebs"]
  }
}

resource "aws_eip_association" "nat-gateway" {
  instance_id   = aws_instance.nat-gateway.id
  allocation_id = aws_eip.nat-ip.id
}

resource "aws_instance" "nat-gateway" {
  ami                     = data.aws_ami.nat-gateway.id
  instance_type           = "t3.micro"
  subnet_id               = aws_subnet.public.id
  source_dest_check       = false
  disable_api_termination = false
  availability_zone       = "${var.aws-region}a"
  vpc_security_group_ids  = [
    aws_security_group.private-default-sg.id]

  tags = {
    Name = "NAT"
  }
}

# Routing table settings, we have 2 routing tables
# 1. public -> have access directly to internet gateway
# 2. private -> is routed to internet via NAT Gateway instance
resource "aws_route_table" "public-routes" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.internet-gateway.id
  }

  tags = {
    Name = "${var.codename}-public-routes"
  }
}

resource "aws_route_table" "private-routes" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block  = "0.0.0.0/0"
    instance_id = aws_instance.nat-gateway.id
  }

  tags = {
    Name = "${var.codename}-private-routes"
  }
}

resource "aws_route_table_association" "private-subnet" {
  route_table_id = aws_route_table.private-routes.id
  subnet_id      = aws_subnet.private.id
}

resource "aws_route_table_association" "db-subnet" {
  route_table_id = aws_route_table.public-routes.id
  subnet_id      = aws_subnet.db-private.id
}

resource "aws_route_table_association" "public-subnet" {
  route_table_id = aws_route_table.public-routes.id
  subnet_id      = aws_subnet.public.id
}

# -----------
# Security Groups
# -----------

resource "aws_security_group" "private-default-sg" {
  name        = "${var.codename}-private-default-sg"
  description = "Default, fully open SG for private network."
  vpc_id      = aws_vpc.vpc.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [
      "10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

# --------
# Route53 Internal Zone
# --------

resource "aws_route53_zone" "private" {
  name = "internal.${var.codename-domain}"

  vpc {
    vpc_id = aws_vpc.vpc.id
  }
}
