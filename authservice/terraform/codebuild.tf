resource "aws_iam_role" "codebuild_auth_role" {
  name = "codebuild-auth-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Service = "codebuild.amazonaws.com"
      },
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Name = "codebuild-auth-role"
  }
}

resource "aws_iam_role_policy_attachment" "codebuild_policy" {
  role       = aws_iam_role.codebuild_auth_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSCodeBuildDeveloperAccess"
}

resource "aws_codebuild_project" "auth_build" {
  name          = "auth-service-build"
  description   = "Build project for auth service"
  service_role  = aws_iam_role.codebuild_auth_role.arn
  build_timeout = 30
  artifacts {
    type = "NO_ARTIFACTS"
  }

  environment {
    compute_type                = "BUILD_GENERAL1_SMALL"
    image                       = "aws/codebuild/standard:5.0"
    type                        = "LINUX_CONTAINER"
    privileged_mode             = true
    environment_variable {
      name  = "ENV"
      value = "dev"
    }
  }

  source {
    type            = "GITHUB"
    location        = "https://github.com/It21258794/CTSE_Microservice.git"
    buildspec       = "buildspec.yml"
    git_clone_depth = 1
  }

  logs_config {
    cloudwatch_logs {
      group_name  = aws_cloudwatch_log_group.auth_logs.name
      stream_name = "auth-build"
    }
  }

  tags = {
    Name = "auth-service-build"
  }
}
