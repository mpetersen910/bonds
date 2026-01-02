output "instance_name" {
  description = "Name of the instance"
  value       = google_compute_instance.bonds_vm.name
}

output "instance_id" {
  description = "ID of the instance"
  value       = google_compute_instance.bonds_vm.id
}

output "public_ip" {
  description = "Public IP address of the instance"
  value       = google_compute_instance.bonds_vm.network_interface[0].access_config[0].nat_ip
}

output "self_link" {
  description = "Self link of the instance"
  value       = google_compute_instance.bonds_vm.self_link
}