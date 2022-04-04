resource "aws_key_pair" "mykeypair" {
  key_name   = "loono-be-dev-key-pair"
  public_key = file(var.PATH_TO_PUBLIC_KEY)
  lifecycle {
    ignore_changes = [public_key]
  }
  tags = {
    Description  = "key edp for loono backend"
  }
}

