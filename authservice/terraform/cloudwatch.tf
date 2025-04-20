resource "aws_cloudwatch_log_group" "auth_logs" {
  name              = "/eks/auth-service"
  retention_in_days = 14

  tags = {
    Name = "auth-service-log-group"
  }
}
