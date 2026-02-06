terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }

    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }

    gitlab = {
      source = "gitlabhq/gitlab"
      version = "18.8.2"
    }

  }
}

provider "aws" {
  region = "af-south-1"
}

variable "TOKEN" {
  type = string
  sensitive = true
}

provider "gitlab" {
  base_url = "https://https://gitlab.com/bhekani.mdletsher-group/api/v4/"
  token = var.TOKEN
}

