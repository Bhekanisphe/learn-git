resource "aws_connect_contact_flow" "BS-Test" {
  instance_id  = var.instance_id
  name         = "BS-Test - IaC"
  description  = "Test Contact Flow IaC"
  type         = "CONTACT_FLOW"
  tags = {
    "Name"        = "Test Contact Flow IaC",
  }
}