resource "google_compute_instance" "bonds_vm" {
  name         = var.instance_name
  machine_type = var.machine_type
  zone         = var.zone
  description  = var.description

  boot_disk {
    initialize_params {
      image = var.boot_image
      size  = var.boot_disk_size
    }
  }

  network_interface {
    network = var.network
    access_config {
      // Ephemeral public IP
    }
  }

  metadata_startup_script = var.startup_script

  tags = var.tags
}