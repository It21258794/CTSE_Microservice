# Identity (domain or email)
resource "aws_ses_email_identity" "auth_service_email" {
  email = "noreply@yourdomain.com" # change this to your verified domain/email
}

# (Optional) IAM policy for SES sending permissions
resource "aws_iam_policy" "ses_send_policy" {
  name        = "SESSendPolicy"
  description = "Allows sending emails via SES"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["ses:SendEmail", "ses:SendRawEmail"],
        Resource = "*"
      }
    ]
  })
}

# (Optional) Attach to IAM Role (e.g., Lambda/EC2 using SES)
resource "aws_iam_role_policy_attachment" "attach_ses_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = aws_iam_policy.ses_send_policy.arn
}
