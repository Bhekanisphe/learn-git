terraform{
    backend "http"{
        address = "https://gitlab.com/api/v4/projects/78204586/terraform/state/$TF_STATE_NAME"
        username = "bhekani.mdletsher"
        password = var.token
    }
}