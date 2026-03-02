# MobileBuyIntegration Sample App

A sample Android app demonstrating how to integrate [Checkout Sheet Kit](../../README.md) with the Shopify Storefront API using [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin).

## Architecture

The app uses **Apollo GraphQL** for all Storefront API communication. GraphQL operations are defined as `.graphql` files, and Apollo Kotlin's code generation tool produces type-safe Kotlin data classes from them.

### Storefront API layer

```
app/src/main/graphql/                    # Source of truth — you edit these
├── schema.graphqls                          # API schema (downloaded)
├── CartFragment.graphql                     # Reusable cart fields
├── CartCreate.graphql                       # Create a new cart
├── CartLinesAdd.graphql                     # Add items to cart
├── CartLinesUpdate.graphql                  # Update item quantities
├── CartLinesRemove.graphql                  # Remove items from cart
├── FetchProducts.graphql                    # Product listing query
├── FetchProduct.graphql                     # Single product query
├── FetchCollections.graphql                 # Collection listing query
└── FetchCollection.graphql                  # Single collection query

app/build/generated/source/apollo/       # Auto-generated — do not edit
└── storefront/.../graphql/
    ├── CartCreateMutation.kt
    ├── FetchProductsQuery.kt
    ├── type/CartInput.kt, CartLineInput.kt, ...
    └── fragment/CartFragment.kt
```

**`app/src/main/graphql/`** contains the `.graphql` files you write and maintain. These define which fields the app fetches from the Storefront API.

**`app/build/generated/source/apollo/`** contains Kotlin code produced by Apollo's code generation tool. These files should not be edited by hand — they are regenerated from the `.graphql` files and the schema.

### How it works

1. `StorefrontApiClient.kt` creates an `ApolloClient` that points at the store's Storefront API endpoint and attaches the access token via an HTTP header. It is provided as a singleton via Koin dependency injection.
2. Repository classes (`CartRepository`, `ProductRepository`, `ProductCollectionRepository`) call suspend functions on the client using the generated operation types (e.g. `CartCreateMutation`, `FetchProductsQuery`).
3. Responses are automatically decoded into the generated Kotlin types, giving you compile-time safety on every field access.

## Setup

1. Copy the config template and fill in your store credentials:

   ```bash
   cp .env.example .env
   ```

   Then edit `.env` with your values:

   ```
   STOREFRONT_DOMAIN=your-store.myshopify.com
   STOREFRONT_ACCESS_TOKEN=your-token
   API_VERSION=2025-07
   ```

2. Open the project in Android Studio and sync Gradle.

3. Build and run.

## Updating the Storefront API version

When you want to target a newer Storefront API version (e.g. to access new fields or features), follow these steps:

### 1. Update the API version

Edit your `.env` and change the `API_VERSION` value:

```
API_VERSION=2025-10
```

### 2. Download the new schema

The schema defines what types and fields are available in the API. Run from the **repo root** (`checkout-sheet-kit-android/`):

```bash
dev apollo download_schema
```

This uses [`rover`](https://www.apollographql.com/docs/rover/) to introspect your store's Storefront API at the configured version and writes `schema.graphqls` into `app/src/main/graphql/`.

### 3. Update your GraphQL operations (if needed)

If the new API version introduces fields you want to use, or deprecates fields you currently use, edit the `.graphql` files in `app/src/main/graphql/`.

For example, to add a new field to products:

```graphql
# app/src/main/graphql/FetchProducts.graphql
query FetchProducts(...) {
  products(first: $numProducts) {
    nodes {
      id
      title
      myNewField    # <-- add new fields here
      ...
    }
  }
}
```

### 4. Run code generation

From the **repo root**:

```bash
dev apollo codegen
```

This reads the schema + your `.graphql` files and regenerates the Kotlin code in `app/build/generated/source/apollo/`. The command also runs `dev style` to check formatting.

### 5. Build and fix any issues

```bash
./gradlew :app:assembleDebug
```

If the new schema removed or renamed fields, you'll get compile errors pointing you to the exact lines that need updating.

## Dev commands reference

All commands are run from the **repo root** (`checkout-sheet-kit-android/`):

| Command | Description |
|---------|-------------|
| `dev apollo download_schema` | Download the Storefront API schema for this sample app |
| `dev apollo codegen` | Regenerate Kotlin types from `.graphql` files |
| `dev style` | Run detekt + Android lint checks |
| `dev build` | Build the library |
| `dev build samples` | Build all sample applications |
| `dev test` | Run all tests |

## Key files

| File | Purpose |
|------|---------|
| `app/src/main/graphql/schema.graphqls` | Storefront API schema (downloaded, not hand-written) |
| `app/build.gradle` | Apollo plugin config + BuildConfig fields |
| `.env` | Store credentials + API version (not checked into git) |
| `StorefrontApiClient.kt` | Apollo client setup, auth header |
| `CartRepository.kt` | Cart state, create/add/update/remove operations |
| `ProductRepository.kt` | Product fetching operations |
| `ProductCollectionRepository.kt` | Collection fetching operations |
