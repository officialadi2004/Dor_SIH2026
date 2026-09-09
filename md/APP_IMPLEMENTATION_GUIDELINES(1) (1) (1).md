# Artisan — Application Implementation Guidelines

## 1. Purpose of this document

The database layer for the current MVP is now defined as 25 PostgreSQL application tables, followed by Supabase Storage setup in migration `026_storage.sql`.

This document covers the remaining work that should be implemented while building the application rather than as additional core tables.

The PRD defines the product as an offline-first digital business assistant with cloud AI assistance. It specifically does not attempt to make the entire AI stack work offline. fileciteturn40file0L18-L43 fileciteturn40file0L45-L57

---

# 2. Current architecture

```text
                    ┌───────────────────────┐
                    │      Mobile App       │
                    │       Artisan         │
                    └───────────┬───────────┘
                                │
                    ┌───────────▼───────────┐
                    │ Customer / Business   │
                    │       Websites        │
                    └───────────┬───────────┘
                                │
                                v
                    ┌───────────────────────┐
                    │ Backend / API Layer   │
                    │ Auth + validation +   │
                    │ business workflows    │
                    └──────┬────────┬───────┘
                           │        │
             ┌─────────────┘        └──────────────┐
             v                                     v
   ┌────────────────────┐                ┌────────────────────┐
   │ Supabase PostgreSQL│                │ Supabase Storage   │
   │ canonical data     │                │ private media      │
   └─────────┬──────────┘                └─────────┬──────────┘
             │                                     │
             └────────────────┬────────────────────┘
                              v
                    ┌───────────────────────┐
                    │ Cloud AI / Providers  │
                    │ via trusted backend   │
                    └───────────────────────┘
```

Do not let the mobile/web clients call AI providers directly with secret API keys.

---

# 3. Supabase setup checklist

## 3.1 Authentication

Use one Supabase Auth system for:

- artisan
- customer
- business buyer
- administrator

The application role is stored in `profiles.role`; Supabase Auth establishes identity through `auth.users`.

The current schema intentionally separates the common authenticated user from domain-specific records. `profiles` is the root application identity record and should not be used to store artisan/product/business data. fileciteturn40file7L1125-L1162

### MVP authentication

Start with:

- email/password
- email verification if enabled for the deployment
- secure password reset
- session persistence
- logout

Add phone/OTP or social login only if actually required by the final product flow.

### Auth metadata rule

Never treat client-provided role metadata as authoritative for authorization.

The existing signup trigger creates the application profile and the database has role-change protection. Backend authorization must still check the canonical `profiles.role`.

---

# 4. Storage

Run:

```text
026_storage.sql
```

Buckets:

```text
artisan-media
product-media
marketing-media
```

All are private.

Use UUID-first object paths:

```text
<bucket>/<auth.uid()>/...
```

See `026_storage_setup_report.md` for the exact storage policy and path rules.

### Public media rule

Do not make product media buckets public merely because customers need to see published products.

Instead:

```text
Customer requests product
        ↓
Backend verifies product is published
        ↓
Backend creates short-lived signed URL
        ↓
Customer receives media URL
```

This prevents storage ownership and product publication from becoming the same authorization rule.

---

# 5. RLS and authorization

RLS is a security boundary, not a replacement for backend authorization.

Every request must satisfy both:

```text
Authenticated identity
        +
Application/business authorization
```

### Before frontend development

Create a role-based test matrix covering:

| Actor | Must be able to | Must NOT be able to |
|---|---|---|
| Artisan A | manage own artisan/profile/product/inventory data | access Artisan B private data |
| Artisan B | manage own data | access Artisan A data |
| Customer | view published products and create own enquiries | edit artisan/product data |
| Business | view published products and manage own bulk requests/messages | edit artisan/product/order data |
| Admin | controlled administrative access | bypass audit/security controls casually |
| Anonymous | public published content only where explicitly allowed | private user/business data |

Run these tests with real authenticated sessions, not only service-role tests.

The PRD requires role-based access for artisan, customer, business and administrator roles. fileciteturn40file0L164-L186

---

# 6. Backend / Edge Functions

Use a trusted server-side layer for orchestration.

Recommended logical functions/services:

```text
ai-story
ai-product
ai-translate
ai-image
ai-pricing
marketing-generate
news-ingest
ishots-generate
sync
media-signed-url
order-confirm
inventory-adjust
```

These can be separate Edge Functions or grouped into a backend service. Do not create a function for every tiny database operation.

### Backend responsibilities

The backend should handle:

- authorization checks
- validation
- AI provider calls
- secret management
- signed URL generation
- transactional business workflows
- audit events
- AI usage accounting
- sync processing
- third-party API integrations

---

# 7. AI architecture

The PRD deliberately keeps heavy AI processing in the cloud while lightweight local functionality remains on the device. fileciteturn40file0L91-L127

Use the following pattern:

```text
User action
   ↓
Backend creates ai_jobs row
   ↓
Worker / Edge Function processes request
   ↓
Provider API
   ↓
Validate result
   ↓
Write durable feature result
   ↓
Write ai_usage
   ↓
Mark ai_jobs completed/failed
   ↓
Return/sync result to client
```

### Important separation

```text
ai_jobs   = execution state
ai_usage  = provider consumption/cost
feature tables = durable business result
```

Do not put the final product description only in `ai_jobs.output_reference`.

The actual product description belongs in `products.description` or the appropriate translation row.

---

# 8. AI safety rules

All AI-generated content must remain editable by the artisan.

The PRD specifically requires editable product descriptions, artisan stories, marketing content and price suggestions. fileciteturn40file0L130-L155

AI must not invent:

- materials
- dimensions
- certifications
- artisan history
- awards
- geographic claims
- cultural heritage claims

Treat the AI output as a draft, not as unquestioned truth. fileciteturn40file0L141-L152

### Pricing

The pricing model produces an advisory recommendation.

The artisan's chosen price remains the canonical value in `products.price`.

Never allow the AI recommendation to silently overwrite the artisan's final price.

---

# 9. Voice cataloging

Recommended pipeline:

```text
Voice recording
      ↓
Store raw audio temporarily/in storage
      ↓
Speech-to-text
      ↓
Structured product extraction
      ↓
Validation against fixed product schema
      ↓
Draft product
      ↓
Artisan review/edit
      ↓
Publish
```

Do not allow an AI model to create arbitrary database columns or categories.

The controlled `product_categories` table is the taxonomy source of truth.

Do not send the same audio to multiple AI providers unnecessarily. Cache intermediate results where practical to control cost. The PRD explicitly recommends a staged voice pipeline and avoiding repeated processing. fileciteturn40file8L1312-L1335

---

# 10. Image enhancement

Pipeline:

```text
Original upload
      ↓
product_images.original_path
      ↓
AI enhancement job
      ↓
product_images.enhanced_path
      ↓
Thumbnail generation
      ↓
product_images.thumbnail_path
```

Never overwrite the original.

Do not run enhancement whenever a product page is opened. Process once and reuse the stored result. fileciteturn40file8L1337-L1341

---

# 11. Dynamic pricing

Pricing input should combine:

- product information
- artisan-provided cost information
- comparable market data

Then:

```text
Market data
    +
Artisan cost inputs
    +
Product context
    ↓
Pricing analysis
    ↓
Recommendation + explanation + range
    ↓
Artisan chooses final price
    ↓
products.price
```

Do not describe the recommendation as an objective or guaranteed market price.

The current market-data provider is intentionally not hard-coded into the database design; the provider should sit behind a service layer.

---

# 12. Translation

Supported MVP languages:

```text
en
hi
mr
```

Keep the canonical product data separate from translated name/description fields through `product_translations`.

Use deterministic UI localization for interface text. Do not call an LLM every time a static button label is displayed.

---

# 13. Social marketing

Workflow:

```text
Product/profile
      ↓
Generate caption + hashtags + creative
      ↓
marketing_contents.status = draft
      ↓
Artisan reviews
      ↓
Approved / ready
      ↓
Optional external publishing
      ↓
Published
```

No autonomous posting without explicit authorization/approval.

OAuth tokens and provider credentials must remain server-side. Never expose them to the mobile/web client.

The PRD treats social publishing as a cloud-dependent capability and keeps provider integration modular. fileciteturn40file0L45-L57

---

# 14. iShots

Recommended pipeline:

```text
News provider
      ↓
news_articles
      ↓
Relevance filtering
      ↓
AI summarization
      ↓
ishots
      ↓
Personalized delivery
```

Do not summarize every article separately for every artisan.

Prefer shared ingestion, relevance filtering and batching where practical. fileciteturn40file8L1343-L1359

The news provider is deliberately abstracted behind a service layer because the final provider is not fixed. fileciteturn40file6L1019-L1026

---

# 15. Offline-first implementation

The mobile app should use a local database/cache.

Possible technologies include:

- SQLite
- Drift
- WatermelonDB
- Realm

Choose one based on the actual mobile stack; do not add several.

### Offline flow

```text
No internet
    ↓
Local read/create/edit
    ↓
Local outbox
    ↓
Connectivity returns
    ↓
Sync batch
    ↓
sync_queue
    ↓
Trusted backend validation
    ↓
Canonical PostgreSQL tables
    ↓
Sync result back to device
```

The PRD defines exactly this model: local capture/edit/save, pending synchronization, cloud processing, then synchronization back to the device. fileciteturn40file0L59-L87

### Do not call it

```text
Fully offline AI
```

Call it:

```text
Offline-first with cloud AI assistance
```

---

# 16. Sync rules

`sync_queue` is not the source of truth.

Canonical state remains in the appropriate business table.

The worker must:

1. authenticate the user
2. validate the operation
3. verify ownership
4. validate the current canonical state
5. execute a transaction where necessary
6. create an audit event for important operations
7. mark the queue operation completed/failed

Use `(user_id, client_operation_id)` as the idempotency boundary.

Do not allow an offline client to manufacture a confirmed order by placing `confirmed` inside an untrusted queue payload. Confirmed orders are server-controlled. fileciteturn40file4L611-L635

---

# 17. Inventory transaction rules

Inventory is one of the places where the backend must use database transactions.

Never do:

```text
read quantity
↓
change quantity in application memory
↓
write quantity
```

without concurrency protection.

Use a transaction with row locking or an equivalent atomic database operation:

```text
BEGIN
  lock inventory row
  validate available quantity
  update inventory
  insert inventory_movements row
COMMIT
```

A bulk request does not automatically permanently reduce inventory.

Reservation/release should happen only through the controlled order workflow.

---

# 18. Confirmed bulk orders

The order flow should be server-controlled:

```text
bulk_order_requests
        ↓
artisan/buyer clarification
        ↓
accepted
        ↓
final quantity + final price + requirements
        ↓
confirmed
        ↓
orders
```

The `orders` table is intentionally minimal.

It is not a payment, shipping, delivery, refund, invoice or logistics system. The PRD explicitly excludes those capabilities from the MVP. fileciteturn40file6L929-L948

---

# 19. Audit logging

Important business/security actions should be written to `audit_logs` by trusted backend code.

Examples:

```text
product_published
product_archived
inventory_added
inventory_removed
inventory_adjusted
bulk_request_created
bulk_request_accepted
bulk_request_rejected
bulk_request_confirmed
order_confirmed
order_cancelled
social_account_connected
social_account_disconnected
marketing_content_approved
marketing_content_published
sync_operation_applied
sync_operation_failed
ai_job_completed
```

Do not rely on the mobile client saying that an operation happened. The backend should create the audit event after it actually accepts/performs the operation. fileciteturn40file1L201-L232

Never store secrets, access tokens or binary content in audit metadata. fileciteturn40file1L234-L247

---

# 20. Secrets and environment variables

Client-side environment variables may contain public Supabase configuration such as the project URL and publishable/anon key where appropriate.

Never put these in the client:

- AI provider secret keys
- news provider secret keys
- Meta/Instagram client secrets
- OAuth refresh tokens
- service-role keys
- database passwords
- encryption keys

Server-side secrets belong in the backend/Edge Function secret store.

Never commit `.env` files containing real credentials.

---

# 21. AI cost control

Track provider usage through `ai_usage`.

At minimum monitor:

- provider
- model
- feature
- token usage where available
- audio duration
- image count
- estimated cost

The PRD explicitly calls for AI usage/cost tracking and recommends caching, avoiding repeated processing and batching expensive operations. fileciteturn40file8L1312-L1364

---

# 22. Realtime

Do not enable Realtime for every table.

Use it selectively where live updates materially improve the product, such as:

- bulk-order messages
- AI job status where useful
- potentially request status changes

Normal product/profile reads do not need permanent realtime subscriptions.

---

# 23. Search

For MVP, use PostgreSQL search first.

Start with:

- product name
- description
- category
- material
- tags
- artisan name/location where useful

Do not introduce Elasticsearch/Algolia/vector infrastructure unless real scale or product requirements justify it.

A later migration may add PostgreSQL full-text search indexes/generated search vectors if the simple `ILIKE`/basic search implementation becomes insufficient.

---

# 24. Database-side items still worth implementing

The 25-table schema is the core application data model. After `026_storage.sql`, there are **no additional core tables required for the locked MVP**.

However, several database-side mechanisms should be implemented/verified as part of application development.

## A. Transactional inventory RPC/function

Recommended before production use.

Purpose:

- atomically add/remove/adjust stock
- prevent negative stock
- write `inventory_movements` in the same transaction
- handle concurrent updates safely

This should be a trusted backend operation rather than a client-side multi-query sequence.

## B. Server-controlled order confirmation

Recommended before B2B testing with real users.

The backend should atomically:

```text
validate bulk request
        ↓
confirm final quantity/price
        ↓
create orders row
        ↓
apply any required reservation movement
        ↓
write audit log
```

The exact inventory reservation policy should be finalized before implementing this transaction.

## C. RLS integration test suite

This is mandatory before production.

Test every table using separate artisan/customer/business identities.

Do not assume a policy is correct merely because the SQL was accepted.

## D. Search optimization migration

Not required immediately.

Add PostgreSQL full-text indexes only after the initial search implementation is measured.

## E. Retention/cleanup jobs

Not required for the first demo, but plan controlled retention for:

- old sync queue records
- audit records at large scale
- temporary media
- failed/expired AI artifacts

Do not add automatic destructive deletion casually.

---

# 25. Database items NOT required right now

Do not add tables for:

- payments
- invoices
- shipping
- couriers
- delivery tracking
- refunds
- escrow
- ONDC
- GeM
- demand forecasting
- AI negotiation
- scam detection
- collaboration network
- IoT
- blockchain
- full offline LLM

These are outside the locked MVP scope. The PRD ends the MVP at product publication, customer connection/enquiry and confirmed bulk-order request. fileciteturn40file8L1426-L1458

---

# 26. Observability

Backend monitoring should cover:

- API errors
- AI failures
- processing latency
- request volume
- storage usage
- AI usage/cost
- failed synchronization
- third-party integration failures

These are explicitly identified as operational requirements in the PRD. fileciteturn40file8L1367-L1381

---

# 27. Production security checklist

Before deployment:

- RLS enabled on application tables
- storage buckets private
- storage ownership policies tested
- no service-role key in client
- AI keys server-side only
- OAuth credentials server-side only
- signed URLs short-lived
- input validation on all backend endpoints
- rate limiting on AI endpoints
- file type/size validation
- image processing isolated from original files
- audit logs generated server-side
- database backups enabled
- monitoring enabled
- error messages do not expose provider secrets or SQL details
- CORS restricted to the actual application origins

---

# 28. Recommended build order

```text
1. Run/verify migrations 001–025
2. Run 026_storage.sql
3. Verify Auth signup -> profiles
4. Verify RLS with all roles
5. Build local mobile persistence
6. Build profile/story workflow
7. Build product + image upload workflow
8. Build AI job backend
9. Build image enhancement pipeline
10. Build pricing pipeline
11. Build customer website
12. Build business bulk-request workflow
13. Build inventory transaction workflow
14. Build marketing generation
15. Build iShots ingestion/relevance pipeline
16. Add offline sync worker
17. Add realtime only where useful
18. Add observability and cost controls
19. Run security/RLS integration tests
20. Production hardening
```

---

# 29. Final source-of-truth rule

Before adding any new table, function, provider or major feature, ask:

1. Is it required by the locked PRD?
2. Does it directly support an in-scope workflow?
3. Is it required for security, reliability, scalability or infrastructure?
4. Is it explicitly future scope?

If the answer is no, do not add it to the MVP without updating the PRD first.

This follows the project's existing source-of-truth rule and prevents the database from growing simply because a new feature sounds useful. fileciteturn40file8L1462-L1476

---

# 30. Bottom line

After `026_storage.sql`, the team does **not** need another large batch of database tables.

The important remaining work is implementation infrastructure:

```text
Supabase Auth
      +
RLS verification
      +
Private Storage
      +
Backend / Edge Functions
      +
AI job workers
      +
Offline local DB + sync
      +
Transactional inventory/order operations
      +
Signed media URLs
      +
Secrets management
      +
Observability
```

The current 25-table database is already designed to support the locked MVP. The next work should be making the application layer use it correctly rather than adding unnecessary schema.
