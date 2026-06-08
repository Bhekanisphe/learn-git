resource "aws_connect_contact_flow" "BS-Test-Flow" {
  instance_id  = var.instance_id
  name         = "BS-Test-Flow - IaC"
  description  = "Test Contact Flow IaC"
  type         = "CONTACT_FLOW"
  tags = {
    "Name"        = "Test Contact Flow IaC",
  }
}

resource "aws_connect_contact_flow" "BS-Test-Flow2" {
  instance_id  = var.instance_id
  name         = "BS-Test-Flow2 - IaC"
  description  = "Test Contact Flow 2 IaC"
  type         = "CONTACT_FLOW"
  tags = {
    "Name"        = "Test Contact Flow IaC 2",
  }
}