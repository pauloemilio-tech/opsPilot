resource "oci_core_vcn" "opspilot_demo" {
  compartment_id = var.compartment_ocid
  cidr_block     = var.vcn_cidr
  display_name   = "opspilot-demo-vcn"
  dns_label      = "opspilot"
}

resource "oci_core_internet_gateway" "opspilot_demo" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.opspilot_demo.id
  display_name   = "opspilot-demo-internet-gateway"
  enabled        = true
}

resource "oci_core_route_table" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.opspilot_demo.id
  display_name   = "opspilot-demo-public-routes"

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.opspilot_demo.id
  }
}

# The subnet deliberately uses an empty security list. All VM traffic is governed
# by the NSG below, avoiding the broader rules in OCI's default VCN security list.
resource "oci_core_security_list" "public_subnet" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.opspilot_demo.id
  display_name   = "opspilot-demo-public-subnet-security-list"
}

resource "oci_core_subnet" "public" {
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.opspilot_demo.id
  cidr_block                 = var.public_subnet_cidr
  display_name               = "opspilot-demo-public-subnet"
  dns_label                  = "public"
  route_table_id             = oci_core_route_table.public.id
  security_list_ids          = [oci_core_security_list.public_subnet.id]
  prohibit_public_ip_on_vnic = false
}

resource "oci_core_network_security_group" "instance" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.opspilot_demo.id
  display_name   = "opspilot-demo-instance-nsg"
}

resource "oci_core_network_security_group_security_rule" "ssh" {
  network_security_group_id = oci_core_network_security_group.instance.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = var.admin_cidr
  source_type               = "CIDR_BLOCK"
  stateless                 = false

  tcp_options {
    destination_port_range {
      min = 22
      max = 22
    }
  }
}

resource "oci_core_network_security_group_security_rule" "http" {
  network_security_group_id = oci_core_network_security_group.instance.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"
  stateless                 = false

  tcp_options {
    destination_port_range {
      min = 80
      max = 80
    }
  }
}

resource "oci_core_network_security_group_security_rule" "https" {
  network_security_group_id = oci_core_network_security_group.instance.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"
  stateless                 = false

  tcp_options {
    destination_port_range {
      min = 443
      max = 443
    }
  }
}

# Unrestricted outbound traffic keeps package repositories, DNS, GitHub, and
# external HTTPS APIs reachable. Inbound response traffic is statefully allowed.
resource "oci_core_network_security_group_security_rule" "egress" {
  network_security_group_id = oci_core_network_security_group.instance.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
  stateless                 = false
}
