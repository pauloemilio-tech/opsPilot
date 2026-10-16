output "instance_public_ip" {
  description = "Ephemeral public IPv4 address assigned to the OpsPilot demo VM."
  value       = oci_core_instance.opspilot_demo.public_ip
}

output "instance_id" {
  description = "OCID of the OpsPilot demo VM."
  value       = oci_core_instance.opspilot_demo.id
}

output "vcn_id" {
  description = "OCID of the dedicated OpsPilot VCN."
  value       = oci_core_vcn.opspilot_demo.id
}

output "subnet_id" {
  description = "OCID of the public subnet."
  value       = oci_core_subnet.public.id
}
