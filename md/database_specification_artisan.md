# Database Specification — Artisan 

**Status:** Finalized from PRD v1.0  
**Database:** Supabase PostgreSQL  
**Authentication:** Supabase Auth (`auth.users`)  
**Object storage:** Supabase Storage  
**Scope:** MVP only

This document defines the database entities before implementation. The PostgreSQL migration in `supabase\_schema\_artisan.sql` is generated from this model.

\---

## 1\. Entity Model

### Identity and roles

1. `profiles`

   * One row per authenticated application user.
   * References `auth.users`.
   * Role: artisan, customer, business, admin.
   * Stores only common account-level information.
2. `artisan\_profiles`

   * One-to-one with a user whose role is `artisan`.
   * Represents the artisan's public/business identity.
   * Separate from products.
3. `artisan\_stories`

   * One-to-one with `artisan\_profiles`.
   * Stores the voice-derived transcript and editable generated story.
   * The story is about the artisan, not an individual product.
4. `business\_profiles`

   * One-to-one with a user whose role is `business`.
   * Stores business identity/contact information.

\---

## 2\. Product and Catalog Entities

5. `product\_categories`

   * Controlled product taxonomy.
   * Prevents the AI model from creating arbitrary categories.
6. `products`

   * Main product record.
   * Belongs to exactly one artisan.
   * Contains the fixed product schema:
name, description, category, material, dimensions, optional colour, price, craft type/tags, status.
7. `product\_translations`

   * Language-specific versions of product name/description.
   * Supports English, Hindi and Marathi.
   * Keeps the canonical product record separate from translated content.
8. `product\_images`

   * Product image metadata.
   * Stores original, enhanced and thumbnail storage paths.
   * Keeps the original image available for recovery/reprocessing.

\---

## 3\. Inventory Entities

9. `inventory`

   * One-to-one with a product.
   * Stores quantity on hand, reserved quantity and low-stock threshold.
   * Availability is represented by controlled product/inventory state.
10. `inventory\_movements`
* Append-only-style history of stock changes.
* Supports manual additions/removals, adjustments and future order reservation/release.
* Gives the team an audit trail instead of overwriting stock history.

\---

## 4\. Pricing and Market Entities

11. `market\_data\_sources`
* Provider registry.
* Keeps the pricing system independent of whichever market-data provider is selected later.
* Current provider decision remains ON HOLD.
12. `market\_data\_records`
* Normalized market observations/comparable prices.
* Can reference a provider source.
* Raw provider payload may be retained in JSONB for traceability.
13. `pricing\_recommendations`
* Historical pricing-analysis outputs for a product.
* Stores input cost information, market snapshot, recommendation/range, explanation and model.
* The artisan's actual final product price remains in `products.price`.

\---

## 5\. Customer Linkage Entities

14. `customer\_enquiries`
* Connects an individual customer to an artisan and optionally a product.
* Stores the enquiry message, artisan response and status.
* No payment or delivery data.

\---

## 6\. B2B / Bulk Order Entities

15. `bulk\_order\_requests`
* A business's request to purchase a product in bulk.
* Stores requested quantity, optional requested price, requirements, response information, final agreed quantity/price and status.
16. `bulk\_order\_messages`
* Conversation messages attached to a bulk request.
* Only the business and artisan involved in that request can access them.
17. `orders`
* Minimal confirmed commercial record created from a confirmed bulk request.
* One-to-one with `bulk\_order\_requests`.
* Intentionally contains no payment, delivery, courier or tracking data.

\---

## 7\. Social Media Entities

18. `social\_accounts`
* Authorized artisan social-media connection.
* MVP platform: Instagram.
* Stores account metadata and an encrypted token field for server-side use if direct publishing is enabled.
19. `marketing\_contents`
* AI-generated marketing material for an artisan/product.
* Stores caption, hashtags, creative path, approval and optional publishing metadata.
* Works even when direct Instagram publishing is unavailable.

\---

## 8\. iShots / News Entities

20. `news\_articles`
* Normalized external news/content items.
* Stores provider, source URL, publication date and provider payload.
21. `ishots`
* Short artisan-facing content derived from a news article.
* Can be global or personalized to an artisan.
* Stores relevance score/reason, headline, why-it-matters, summary and optional action.

\---

## 9\. AI and Platform Entities

22. `ai\_jobs`
* Tracks asynchronous AI operations.
* Examples: transcription, catalog extraction, translation, image enhancement, pricing analysis, marketing generation and iShot processing.
* Links a job to the relevant product/artisan/content record where applicable.
23. `ai\_usage`
* Tracks model/provider usage and estimated cost.
* Enables cost monitoring, budgeting and provider comparison.
24. `sync\_queue`
* Tracks client operations that need synchronization after offline use.
* Uses an idempotent `client\_operation\_id` per user.
25. `audit\_logs`
* Records important security/business actions.
* Used for operational traceability rather than storing complete application history.

\---

# 3\. Relationships

```text
auth.users
    |
    └── profiles
          |
          +── artisan\_profiles
          |      |
          |      +── artisan\_stories
          |      |
          |      +── products
          |             |
          |             +── product\_images
          |             +── product\_translations
          |             +── inventory
          |             |      └── inventory\_movements
          |             +── pricing\_recommendations
          |             +── customer\_enquiries
          |             +── bulk\_order\_requests
          |             +── marketing\_contents
          |
          +── business\_profiles
          |      └── bulk\_order\_requests
          |             ├── bulk\_order\_messages
          |             └── orders
          |
          └── customer\_enquiries

product\_categories
    └── products

market\_data\_sources
    └── market\_data\_records

news\_articles
    └── ishots

profiles
    ├── ai\_jobs
    ├── ai\_usage
    ├── sync\_queue
    └── audit\_logs
```

\---

# 4\. Important Design Decisions

## 4.1 Do not put stock directly into `products`

The PRD requires stock, but inventory is operational data. It is therefore kept in a dedicated `inventory` table.

This allows inventory history and future expansion without redesigning the product table.

## 4.2 Do not put the artisan story directly into `artisan\_profiles`

The story is a separate domain object and may later require versioning, translation or additional media.

## 4.3 Do not let AI define database fields

The product schema is fixed.

AI produces values for defined fields; it does not create columns or arbitrary product attributes.

## 4.4 Keep current price separate from price recommendations

`products.price` is the artisan's actual published price.

`pricing\_recommendations` stores AI analysis history.

This prevents a recommendation from silently changing the seller's price.

## 4.5 Keep market data provider-agnostic

The market-data provider is still undecided. The schema therefore separates:

* source/provider
* raw market observation
* pricing recommendation

No specific provider is embedded into the product model.

## 4.6 Bulk request and confirmed order are different

A request is a negotiation/decision record.

An order is created only after confirmation.

The order deliberately excludes fulfilment/payment data because those are future scope.

## 4.7 Original and enhanced images are different assets

The original must never be overwritten by AI processing.

## 4.8 AI jobs are asynchronous

The application should not hold an HTTP request open while an AI model processes an image/audio file.

## 4.9 Supabase Auth owns authentication identity

Application tables reference `auth.users`.

Passwords are not stored in the application database.

\---

# 5\. RLS Model

RLS follows ownership rather than simply role.

### Artisan

Can manage only:

* Own artisan profile.
* Own story.
* Own products.
* Own images.
* Own inventory.
* Own pricing records.
* Own marketing content.
* Bulk requests involving own products.
* Own social accounts.

### Customer

Can:

* Read published products/profiles.
* Create enquiries as themselves.
* Read their own enquiries.

### Business

Can:

* Read published products/profiles.
* Create bulk requests as themselves.
* Read/update their own bulk requests.
* Read/send messages belonging to their requests.
* Read their own confirmed orders.

### Public/anonymous

Can read only deliberately public information:

* Published artisan profiles.
* Published artisan stories.
* Published products.
* Published product images through the application's media-access mechanism.
* Active categories.
* Published global iShots/news where appropriate.

### Admin

Can manage platform configuration and moderation data.

Admin access is not treated as automatic unrestricted access to all private user data.

\---

# 6\. Storage Model

Use Supabase Storage instead of PostgreSQL for binary files.

Buckets:

* `artisan-media`
* `product-media`
* `marketing-media`

Recommended path convention:

```text
<auth-user-id>/...
```

Examples:

```text
<user-id>/profile/avatar.jpg
<user-id>/stories/<story-id>/audio.m4a
<user-id>/products/<product-id>/original/<image-id>.jpg
<user-id>/products/<product-id>/enhanced/<image-id>.jpg
<user-id>/products/<product-id>/thumbnail/<image-id>.jpg
<user-id>/marketing/<content-id>/creative.jpg
```

The database stores the storage path, not the binary file.

All buckets are private in the migration. Published media should be delivered through controlled/signed access rather than exposing original uploads.

\---

# 7\. What Is Deliberately NOT an Entity

The following do not get database tables in MVP:

* Payments.
* Payment transactions.
* Delivery.
* Shipments.
* Couriers.
* Tracking events.
* Invoices.
* Escrow.
* Negotiation sessions.
* Scam scores.
* Demand forecasts.
* Artisan collaboration matches.
* IoT devices.
* Blockchain records.
* Healthcare/medical records.
* Full CRM contacts.
* Marketplace accounts for GeM/ONDC.

If these become required later, they should be introduced as new bounded modules rather than prematurely adding tables.

\---

# 8\. State Machines

## Product

```text
DRAFT
  ↓
READY\_FOR\_REVIEW
  ↓
PUBLISHED
  ↓
UNPUBLISHED
  ↓
ARCHIVED
```

## Bulk request

```text
REQUESTED
  ↓
UNDER\_REVIEW
  ↓
CLARIFICATION\_REQUIRED  ←→  UNDER\_REVIEW
  ↓
ACCEPTED
  ↓
CONFIRMED
```

Terminal alternatives:

```text
REJECTED
CANCELLED
```

## AI job

```text
QUEUED
  ↓
PROCESSING
  ↓
COMPLETED

PROCESSING → FAILED
QUEUED/PROCESSING → CANCELLED
```

## Enquiry

```text
OPEN → RESPONDED → CLOSED
```

\---

# 9\. Scalability Notes

The schema is intentionally normalized around the main business objects.

For scale:

* Index all foreign keys used in list/filter queries.
* Index product publication/category/artisan fields.
* Use GIN indexing for product tags.
* Keep large media out of PostgreSQL.
* Use asynchronous AI processing.
* Store AI usage separately from application transactions.
* Keep external providers behind service abstractions.
* Use idempotency for offline synchronization.
* Use RLS for direct Supabase client access.
* Use server-side/service-role operations for privileged jobs.
* Avoid exposing provider API keys to clients.

The schema is designed to support future payment, logistics, marketplace and advanced intelligence modules without making those modules part of the MVP.

