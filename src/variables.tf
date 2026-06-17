variable "instance_id" {

    type = string
    description = "The ID of the AWS Connect instance"
    default = "b6d63c3c-2832-4cef-a017-d8a74a72657d"
}

variable "days_of_week" {
    type = set(string)
    description = "List of days of the week for hoo"
    default = ["MONDAY","TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
}
