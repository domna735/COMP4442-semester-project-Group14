
#!/bin/bash


TARGET_DIR=$(dirname "$0")

PRIVATE_KEY="$TARGET_DIR/ECDSA_384_private.pem"
PUBLIC_KEY="$TARGET_DIR/ECDSA_384_public.pem"

# Generate KEY PAIR: For JWT SIGNING (ECDSA)
echo "Generating ECDSA key pair in $TARGET_DIR..."

# Generate Private Key
openssl genpkey -algorithm EC \
    -pkeyopt ec_paramgen_curve:secp384r1 \
    -out "$PRIVATE_KEY"

# Extract Public Key
openssl pkey -in "$PRIVATE_KEY" \
    -pubout -out "$PUBLIC_KEY"

# Verify the keys
echo "-----------------------------------"
echo "Verifying ECDSA signing key pair:"
openssl ec -in "$PRIVATE_KEY" -text -noout
openssl pkey -in "$PUBLIC_KEY" -pubin -text -noout

echo "-----------------------------------"
echo "Keys successfully created at:"
echo "Private: $PRIVATE_KEY"
echo "Public: $PUBLIC_KEY"