data "oci_core_images" "ubuntu_2404_x86_64" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Canonical Ubuntu"
  operating_system_version = "24.04"
  shape                    = "VM.Standard.E2.1.Micro"
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"
}

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
    source_type = "image"
    source_id   = data.oci_core_images.ubuntu_2404_x86_64.images[0].id
  }

  metadata = {
    ssh_authorized_keys = trimspace(var.ssh_public_key)
    user_data           = base64encode(file("${path.module}/cloud-init.yaml"))
  }

  lifecycle {
    precondition {
      condition     = length(data.oci_core_images.ubuntu_2404_x86_64.images) > 0
      error_message = "No compatible Canonical Ubuntu 24.04 x86_64 image was found for VM.Standard.E2.1.Micro in this region."
    }
  }
}
