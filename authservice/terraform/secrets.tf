resource "aws_secretsmanager_secret" "auth_service_secret" {
  name        = "auth-service-secret-v2"
  description = "Secrets for the auth-service"

  tags = {
    Name = "auth-service-secret-v2"
  }
}

resource "aws_secretsmanager_secret_version" "auth_service_secret_version" {
  secret_id     = aws_secretsmanager_secret.auth_service_secret.id
  secret_string = jsonencode({
    DB_USER     = "authuser"
    DB_PASSWORD = "authpass123"
    JWT_SECRET  = "super-secure-jwt-secret"
  })
}
