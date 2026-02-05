resource "aws_connect_queue" "EDB_CCS_Debit" {
  instance_id           = var.instance_id
  name                  = "EDB CCS Debit"
  description           = "EDB CCS Debit Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Example Queue",
  }
}