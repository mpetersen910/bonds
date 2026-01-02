terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
}

locals {
  startup_script = <<-EOF
  #!/bin/bash
  apt-get update
  apt-get install -y wget apt-transport-https gnupg

  # Add Corretto repository
  wget -O - https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg
  echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list

  # Install Corretto 25
  apt-get update
  apt-get install -y java-25-amazon-corretto-jdk
  EOF
}

# US East instance
module "bonds_us_east" {
  source = "./modules/bonds-instance"

  instance_name  = "bonds-vm-us-east"
  zone           = "us-east1-b"
  description    = "Bonds VM in US East"
  machine_type   = var.machine_type
  startup_script = local.startup_script
  tags           = ["http-server", "bonds"]
}

# # US West instance
# module "bonds_us_west" {
#   source = "./modules/bonds-instance"
#
#   instance_name  = "bonds-vm-us-west"
#   zone           = "us-west1-b"
#   description    = "Bonds VM in US West"
#   machine_type   = var.machine_type
#   startup_script = local.startup_script
#   tags           = ["http-server", "bonds"]
# }
#
# # Europe instance (optional - just add/remove as needed)
# module "bonds_europe" {
#   source = "./modules/bonds-instance"
#
#   instance_name  = "bonds-vm-europe"
#   zone           = "europe-west1-b"
#   description    = "Bonds VM in Europe"
#   machine_type   = var.machine_type
#   startup_script = local.startup_script
#   tags           = ["http-server", "bonds"]
# }

# Shared firewall rule (applies to all instances with "bonds" tag)
resource "google_compute_firewall" "bonds_firewall" {
  name        = "allow-bonds"
  network     = "default"
  description = "Allow traffic to Bonds app on port 8080"

  allow {
    protocol = "tcp"
    ports    = ["8080"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["bonds"]
}