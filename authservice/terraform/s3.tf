resource "random_id" "bucket_id" {
  byte_length = 4
}

resource "aws_s3_bucket" "auth_user_profiles" {
  bucket = "auth-user-profile-bucket-${random_id.bucket_id.hex}"

  tags = {
    Name        = "auth-user-profile-bucket"
    Environment = "dev"
  }
}

resource "aws_s3_bucket_ownership_controls" "auth_user_profile_ownership" {
  bucket = aws_s3_bucket.auth_user_profiles.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "auth_bucket_block" {
  bucket = aws_s3_bucket.auth_user_profiles.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
