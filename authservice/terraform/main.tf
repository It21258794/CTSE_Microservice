provider "aws" {
  region = var.region
}

# ------------------------------
# VPC and Subnets
# ------------------------------
resource "aws_vpc" "auth_service_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "auth-service-vpc"
  }
}

resource "aws_subnet" "auth_service_public_subnet" {
  vpc_id                  = aws_vpc.auth_service_vpc.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.az_public
  map_public_ip_on_launch = true

  tags = {
    Name                                        = "auth-service-public-subnet"
    "kubernetes.io/role/elb"                   = "1"
    "kubernetes.io/cluster/auth-service-cluster" = "owned"
  }
}

resource "aws_subnet" "auth_service_private_subnet" {
  vpc_id            = aws_vpc.auth_service_vpc.id
  cidr_block        = var.private_subnet_cidr
  availability_zone = var.az_private

  tags = {
    Name                                        = "auth-service-private-subnet"
    "kubernetes.io/role/internal-elb"          = "1"
    "kubernetes.io/cluster/auth-service-cluster" = "owned"
  }
}

resource "aws_internet_gateway" "auth_service_igw" {
  vpc_id = aws_vpc.auth_service_vpc.id

  tags = {
    Name = "auth-service-igw"
  }
}

resource "aws_route_table" "auth_service_public_rt" {
  vpc_id = aws_vpc.auth_service_vpc.id

  tags = {
    Name = "auth-service-public-rt"
  }
}

resource "aws_route" "auth_service_public_route" {
  route_table_id         = aws_route_table.auth_service_public_rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.auth_service_igw.id
}

resource "aws_route_table_association" "auth_service_public_rta" {
  subnet_id      = aws_subnet.auth_service_public_subnet.id
  route_table_id = aws_route_table.auth_service_public_rt.id
}

# ------------------------------
# Security Group
# ------------------------------
resource "aws_security_group" "auth_service_sg" {
  vpc_id = aws_vpc.auth_service_vpc.id

  ingress {
    description = "Allow control plane to worker nodes"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow worker node to control plane"
    from_port   = 1025
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Worker-to-worker"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "auth-service-sg"
  }
}

# ------------------------------
# IAM Role for EKS Cluster
# ------------------------------
resource "aws_iam_role" "eks_role" {
  name = "eks-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Service = "eks.amazonaws.com"
      },
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Name = "eks-role"
  }
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  role       = aws_iam_role.eks_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_iam_role_policy_attachment" "eks_service_policy" {
  role       = aws_iam_role.eks_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSServicePolicy"
}

# ------------------------------
# IAM Role for Node Group
# ------------------------------
resource "aws_iam_role" "eks_node_role" {
  name = "eks-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Service = "ec2.amazonaws.com"
      },
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Name = "eks-node-role"
  }
}

resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

resource "aws_iam_role_policy_attachment" "ec2_container_registry_read_only" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# ------------------------------
# EKS Cluster
# ------------------------------
resource "aws_eks_cluster" "auth_service_cluster" {
  name     = "auth-service-cluster"
  role_arn = aws_iam_role.eks_role.arn

  vpc_config {
    subnet_ids         = [
      aws_subnet.auth_service_public_subnet.id,
      aws_subnet.auth_service_private_subnet.id
    ]
    security_group_ids     = [aws_security_group.auth_service_sg.id]
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
    aws_iam_role_policy_attachment.eks_service_policy
  ]

  tags = {
    Name = "auth-service-cluster"
  }
}

# ------------------------------
# EKS Node Group
# ------------------------------
resource "aws_eks_node_group" "auth_node_group" {
  cluster_name    = aws_eks_cluster.auth_service_cluster.name
  node_group_name = "auth-node-group"
  node_role_arn   = aws_iam_role.eks_node_role.arn

  subnet_ids = [
    aws_subnet.auth_service_public_subnet.id
  ]

  instance_types = var.instance_types

  scaling_config {
    desired_size = var.desired_capacity
    max_size     = var.max_capacity
    min_size     = var.min_capacity
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.ec2_container_registry_read_only,
    aws_iam_role_policy_attachment.ssm
  ]

  tags = {
    Name = "auth-node-group"
  }
}

# ------------------------------
# ECR Repository
# ------------------------------
resource "aws_ecr_repository" "auth_service_repo" {
  name = "auth-service-repo"

  image_scanning_configuration {
    scan_on_push = true
  }

  image_tag_mutability = "MUTABLE"

  tags = {
    Name = "auth-service-ecr"
  }
}

output "auth_service_ecr_repository_url" {
  description = "ECR repository URL for the Auth Service"
  value       = aws_ecr_repository.auth_service_repo.repository_url
}
