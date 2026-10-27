# OpsPilot OCI Terraform

This directory provisions the minimum infrastructure for the public OpsPilot
demo in Oracle Cloud Infrastructure: one VCN, one public subnet, an Internet
Gateway and route, an NSG, and one AMD/x86_64 compute instance.

Terraform does not deploy the application or create paid supporting services.
Never run `terraform apply` until the plan has been reviewed and OCI capacity
and Always Free eligibility have been confirmed for the target tenancy.

## Prerequisites and authentication

- Terraform 1.6 or newer
- An OCI CLI configuration at `~/.oci/config` with the `DEFAULT` profile
- API-key authentication configured for that profile
- A passphrase-protected private key, with its password supplied locally when
  needed through `OCI_PRIVATE_KEY_PASSWORD`
- An existing OCI compartment, expected to be `opspilot-demo`
- An SSH public key; the matching private key remains only on the administrator's
  machine

The provider uses OCI's standard configuration and environment resolution. No
tenancy OCID, user OCID, fingerprint, private key, or private-key password is
stored in Terraform source.

For PowerShell, set inputs only in the current shell:

```powershell
$env:OCI_PRIVATE_KEY_PASSWORD = '<private-key-passphrase>'
$env:TF_VAR_compartment_ocid = 'ocid1.compartment.oc1..replace_me'
$env:TF_VAR_admin_cidr = '203.0.113.10/32'
$env:TF_VAR_ssh_public_key = (Get-Content -Raw "$env:USERPROFILE\.ssh\id_ed25519.pub").Trim()
```

Alternatively, copy `terraform.tfvars.example` to the ignored
`terraform.tfvars` file and replace the placeholders. Do not commit that file.

## Usage

```text
terraform fmt -recursive
terraform init
terraform validate
terraform plan -out=opspilot.tfplan
```

The default region is `sa-saopaulo-1`, and the default availability domain is
`Vasp:SA-SAOPAULO-1-AD-1`. Both can be overridden with input variables.

## Network policy

The VM receives an ephemeral public IPv4 address. Its NSG permits inbound TCP
80 and 443 from the internet and TCP 22 only from `admin_cidr`. Ports 8080 and
5432 have no public ingress rules. The subnet uses a dedicated empty security
list so the VCN default security list cannot broaden this policy.

All outbound IPv4 traffic is allowed for operating-system packages, DNS,
GitHub, and external HTTPS APIs. This is intentionally simple for the single-VM
MVP and can be narrowed later if fixed destinations are available.

## Compute and image selection

The instance uses the fixed `VM.Standard.E2.1.Micro` shape with 1 GB of memory
(an AMD/x86_64 platform). The shape's CPU and memory are fixed, so Terraform
does not set a `shape_config` block. The instance is pinned to the Ubuntu 24.04
image `Canonical-Ubuntu-24.04-2026.08.25-0` with OCID
`ocid1.image.oc1.sa-saopaulo-1.aaaaaaaabnvkv25i7oj725yhi6e4ae5fod7ekzffeng3bwp2x37eanuauxrq`.
Pinning the image instead of automatically selecting the newest build keeps
deployments reproducible and avoids regressions in newer image builds.

The boot volume uses the image/default size; no extra or enlarged block volume
is requested. `VM.Standard.E2.1.Micro` capacity and Always Free eligibility are
tenancy- and region-dependent, capacity in an availability domain is not
guaranteed, and the Always Free allowance permits only a limited number of
eligible micro instances across the tenancy. Confirm both remaining tenancy
allowance and the plan's cost estimate before applying.

## Bootstrap boundary

Cloud-init refreshes package metadata, installs Java 17, PostgreSQL, Nginx, and
Git, enables PostgreSQL and Nginx, and creates `/opt/opspilot` owned by the
standard `ubuntu` user. It also creates and enables a 1 GB `/swapfile` and adds
it to `/etc/fstab`. Swap is a safety margin for transient pressure, not a
substitute for bounding the application heap.

PostgreSQL is configured for a low-traffic demo with 30 maximum connections,
128 MB shared buffers, a 384 MB effective cache estimate, 32 MB maintenance
work memory, 4 MB per-operation work memory, and 4 MB WAL buffers. PostgreSQL
continues to use its package defaults for network binding; port 5432 is not
opened by the NSG.

With only 1 GB of physical RAM shared by the OS, PostgreSQL, Nginx, and Java,
deploy a built Spring Boot JAR directly under `/opt/opspilot` and run it with
systemd rather than keeping Docker running. A recommended starting limit is:

```text
JAVA_TOOL_OPTIONS=-Xms128m -Xmx384m
```

Keep application secrets in a root-readable systemd environment file or an
external secret store, not in Terraform variables, cloud-init, the unit file,
or the repository. Monitor memory and swap use after deployment; the micro
shape is intended for a low-traffic demo and has little headroom for builds or
traffic spikes.

It intentionally does not clone the repository, deploy a JAR, add application
or Anthropic credentials, configure TLS certificates, or install production
Nginx proxy rules. Secrets must not be added to cloud-init because user data is
retained in instance metadata and Terraform-managed state.

## Outputs

After apply, Terraform reports the instance public IP, instance OCID, VCN OCID,
and subnet OCID. No secret values are output.
