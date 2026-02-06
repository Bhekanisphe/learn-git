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

  }
}

provider "aws" {
  region = "af-south-1"
}


provider "gitlab" {
  token    = var.gitlab_token
  base_url = "https://gitlab.com/bhekani.mdletsher-group/learn-gitlab-app.git"
}
