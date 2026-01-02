variable "instance_name" {
  description = "Name of the compute instance"
  type        = string
}

variable "machine_type" {
  description = "Machine type for the instance"
  type        = string
  default     = "e2-medium"
}

variable "zone" {
  description = "GCP zone for the instance"
  type        = string
}

variable "description" {
  description = "Description of the instance"
  type        = string
  default     = ""
}

variable "boot_image" {
  description = "Boot disk image"
  type        = string
  default     = "ubuntu-os-cloud/ubuntu-minimal-2204-lts"
}

variable "boot_disk_size" {
  description = "Boot disk size in GB"
  type        = number
  default     = 20
}

variable "network" {
  description = "Network to attach instance to"
  type        = string
  default     = "default"
}

variable "startup_script" {
  description = "Startup script for the instance"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Network tags for the instance"
  type        = list(string)
  default     = []
}