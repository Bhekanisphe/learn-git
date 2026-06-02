resource "aws_connect_queue" "EDB_CCS_Debit" {
  instance_id           = var.instance_id
  name                  = "EDB CCS Debit - IaC"
  description           = "EDB CCS Debit Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Example Queue",
  }
}

resource "aws_connect_queue" "EDB_CCS_Credit" {
  instance_id           = var.instance_id
  name                  = "EDB CCS Credit - IaC"
  description           = "EDB CCS Credit Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Example Queue - 2",
  }
}

resource "aws_connect_queue" "EDB_CCS_Debit" {
  instance_id           = var.instance_id
  name                  = "EDB CCS Debit - IaC"
  description           = "EDB CCS Debit Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Example Queue",
  }
}

resource "aws_connect_queue" "Technical_Wifi" {
  instance_id           = var.instance_id
  name                  = "Technical Wifi IaC"
  description           = "Technical Wifi Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Technical Wifi Queue",
  }
}

resource "aws_connect_queue" "Technical_Laptop" {
  instance_id           = var.instance_id
  name                  = "Technical Laptop IaC"
  description           = "Technical Laptop Queue"
  hours_of_operation_id = aws_connect_hours_of_operation.EDB_HOO.hours_of_operation_id

  tags = {
    "Name" = "Technical Laptop Queue",
  }
}

