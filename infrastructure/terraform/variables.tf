variable "compartment_ocid" {
  description = "OCID of the OCI compartment in which to create the OpsPilot demo resources."
  type        = string

  validation {
    condition     = can(regex("^ocid1\\.compartment\\.", var.compartment_ocid))
    error_message = "compartment_ocid must be an OCI compartment OCID."
  }
}

variable "ssh_public_key" {
  description = "SSH public key installed for the standard Ubuntu user."
  type        = string

  validation {
    condition     = can(regex("^(ssh-(rsa|ed25519)|ecdsa-sha2-nistp(256|384|521)) ", trimspace(var.ssh_public_key)))
    error_message = "ssh_public_key must be a valid OpenSSH-format public key."
  }
}

variable "admin_cidr" {
  description = "Single IPv4 CIDR allowed to connect to SSH, preferably an administrator /32 address."
  type        = string

  validation {
    condition     = can(cidrnetmask(var.admin_cidr))
    error_message = "admin_cidr must be a valid IPv4 CIDR, for example 203.0.113.10/32."
  }
}

variable "region" {
  description = "OCI region for the OpsPilot demo."
  type        = string
  default     = "sa-saopaulo-1"
}

variable "availability_domain" {
  description = "Availability domain in which to launch the VM."
  type        = string
  default     = "Vasp:SA-SAOPAULO-1-AD-1"
}

variable "vcn_cidr" {
  description = "IPv4 CIDR assigned to the dedicated OpsPilot VCN."
  type        = string
  default     = "10.20.0.0/16"
}

variable "public_subnet_cidr" {
  description = "IPv4 CIDR assigned to the public subnet."
  type        = string
  default     = "10.20.1.0/24"
}
