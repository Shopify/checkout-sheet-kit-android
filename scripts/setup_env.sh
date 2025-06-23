#!/bin/bash

# Function to create .env file for a sample app
create_env_file() {
    local app_name="$1"
    local env_path="./samples/${app_name}/.env"
    
    if [ ! -f "$env_path" ]; then
        echo "Creating ${app_name} .env file..."
        cat > "$env_path" << EOF
STOREFRONT_DOMAIN=${STOREFRONT_DOMAIN}
STOREFRONT_ACCESS_TOKEN=${STOREFRONT_ACCESS_TOKEN}
EOF
        
        # Add optional authentication configuration if provided
        if [ -n "$SHOP_ID" ] && [ -n "$CUSTOMER_ACCOUNT_API_CLIENT_ID" ]; then
            cat >> "$env_path" << EOF

CUSTOMER_ACCOUNT_API_CLIENT_ID=${CUSTOMER_ACCOUNT_API_CLIENT_ID}
CUSTOMER_ACCOUNT_API_REDIRECT_URI=shop.${SHOP_ID}.app://callback

CUSTOMER_ACCOUNT_API_GRAPHQL_BASE_URL=https://shopify.com/${SHOP_ID}/account/customer/api/${CUSTOMER_ACCOUNT_API_VERSION}/graphql
CUSTOMER_ACCOUNT_API_AUTH_BASE_URL=https://shopify.com/authentication/${SHOP_ID}
EOF
        fi
        echo "✓ Created ${app_name} .env file"
    fi
}

# Prompt for Shopify credentials
echo "Setting up .env files for sample apps..."
echo "Please provide your Shopify storefront credentials:"
read -p "Storefront Domain (e.g., yourstore.myshopify.com): " STOREFRONT_DOMAIN
read -p "Storefront Access Token: " STOREFRONT_ACCESS_TOKEN

if [ -z "$STOREFRONT_DOMAIN" ] || [ -z "$STOREFRONT_ACCESS_TOKEN" ]; then
    echo "Error: Both storefront domain and access token are required."
    exit 1
fi

# Optional authentication configuration
echo ""
echo "Optional: Customer Account API configuration (needed for authentication features)"
echo "Press Enter to skip if you don't need authentication features."
read -p "Shop ID (optional): " SHOP_ID
read -p "Customer Account API Client ID (optional): " CUSTOMER_ACCOUNT_API_CLIENT_ID
read -p "Customer Account API Version (default: unstable): " CUSTOMER_ACCOUNT_API_VERSION

# Set default API version if not provided
if [ -z "$CUSTOMER_ACCOUNT_API_VERSION" ]; then
    CUSTOMER_ACCOUNT_API_VERSION="unstable"
fi

# Create .env files for both sample apps
create_env_file "MobileBuyIntegration"
create_env_file "SimpleCheckout"

echo "✓ Sample app .env files are ready!"
