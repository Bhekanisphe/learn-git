resource "aws_connect_contact_flow" "BM-Test-Flow" {
  instance_id  = var.instance_id
  name         = "BM-Test-Flow"
  description  = "Test Contact Flow created by IaC"
  type         = "CONTACT_FLOW"
  tags = {
    "Name"        = "Test Contact Flow IaC",
  }
}