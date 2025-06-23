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
        
        # Setup MobileBuyIntegration .env
        if [ ! -f "./samples/MobileBuyIntegration/.env" ]; then
          echo "Creating MobileBuyIntegration .env file..."
          cat > "./samples/MobileBuyIntegration/.env" << EOF
STOREFRONT_DOMAIN=${STOREFRONT_DOMAIN}
STOREFRONT_ACCESS_TOKEN=${STOREFRONT_ACCESS_TOKEN}
EOF
          
          # Add optional authentication configuration if provided
          if [ -n "$SHOP_ID" ] && [ -n "$CUSTOMER_ACCOUNT_API_CLIENT_ID" ]; then
            cat >> "./samples/MobileBuyIntegration/.env" << EOF

CUSTOMER_ACCOUNT_API_CLIENT_ID=${CUSTOMER_ACCOUNT_API_CLIENT_ID}
CUSTOMER_ACCOUNT_API_REDIRECT_URI=shop.${SHOP_ID}.app://callback

CUSTOMER_ACCOUNT_API_GRAPHQL_BASE_URL=https://shopify.com/${SHOP_ID}/account/customer/api/${CUSTOMER_ACCOUNT_API_VERSION}/graphql
CUSTOMER_ACCOUNT_API_AUTH_BASE_URL=https://shopify.com/authentication/${SHOP_ID}
EOF
          fi
          echo "✓ Created MobileBuyIntegration .env file"
        fi
        
        # Setup SimpleCheckout .env
        if [ ! -f "./samples/SimpleCheckout/.env" ]; then
          echo "Creating SimpleCheckout .env file..."
          cat > "./samples/SimpleCheckout/.env" << EOF
STOREFRONT_DOMAIN=${STOREFRONT_DOMAIN}
STOREFRONT_ACCESS_TOKEN=${STOREFRONT_ACCESS_TOKEN}
EOF
          
          # Add optional authentication configuration if provided
          if [ -n "$SHOP_ID" ] && [ -n "$CUSTOMER_ACCOUNT_API_CLIENT_ID" ]; then
            cat >> "./samples/SimpleCheckout/.env" << EOF

CUSTOMER_ACCOUNT_API_CLIENT_ID=${CUSTOMER_ACCOUNT_API_CLIENT_ID}
CUSTOMER_ACCOUNT_API_REDIRECT_URI=shop.${SHOP_ID}.app://callback

CUSTOMER_ACCOUNT_API_GRAPHQL_BASE_URL=https://shopify.com/${SHOP_ID}/account/customer/api/${CUSTOMER_ACCOUNT_API_VERSION}/graphql
CUSTOMER_ACCOUNT_API_AUTH_BASE_URL=https://shopify.com/authentication/${SHOP_ID}
EOF
          fi
          echo "✓ Created SimpleCheckout .env file"
        fi
        
        echo "✓ Sample app .env files are ready!"
