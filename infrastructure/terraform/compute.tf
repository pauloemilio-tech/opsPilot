resource "oci_core_instance" "opspilot_demo" {
  compartment_id      = var.compartment_ocid
  availability_domain = var.availability_domain
  display_name        = "opspilot-demo-vm"
  shape               = "VM.Standard.E2.1.Micro"

  create_vnic_details {
    assign_public_ip = true
    display_name     = "opspilot-demo-primary-vnic"
    hostname_label   = "demo"
    nsg_ids          = [oci_core_network_security_group.instance.id]
    subnet_id        = oci_core_subnet.public.id
  }

  source_details {
    source_type             = "image"
    boot_volume_size_in_gbs = 50
    # Pin the image for reproducible deployments and to avoid regressions in newer image builds.
    source_id = "ocid1.image.oc1.sa-saopaulo-1.aaaaaaaabnvkv25i7oj725yhi6e4ae5fod7ekzffeng3bwp2x37eanuauxrq"
  }

  metadata = {
    ssh_authorized_keys = trimspace(var.ssh_public_key)
    user_data           = base64encode(file("${path.module}/cloud-init.yaml"))
  }
}
