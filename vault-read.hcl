
path "sys/mounts" {
  capabilities = ["read", "list"]
}

path "kv/metadata/secret/*" {
  capabilities = ["list", "read"]
}


path "kv/data/secret/*" {
  capabilities = ["read"]
}