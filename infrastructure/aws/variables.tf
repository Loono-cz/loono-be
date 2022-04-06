variable "aws-region" {
  type    = string
  default = "eu-central-1"
}

variable "codename" {
  type    = string
  default = "loono"
}

# Internal domain, its not visible publicly, but it should be domain we own
# to comply with best practices => it ensures the internal domain names are globally unique.
variable "codename-domain" {
  type    = string
  default = "prod.loono.cz"
}

variable "database-username" {
  type    = string
  default = "loono"
}

variable "database-password" {
  type = string
}

variable "certificate-arn" {
  type = string
}

variable "google-app-credentials" {
  type = string
}

variable "onesignal-api-key" {
  type = string
}

variable "PATH_TO_PUBLIC_KEY" {
  default = "ed25519-key.pub"
}