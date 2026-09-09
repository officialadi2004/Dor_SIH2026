# Product Requirements Document (PRD)

# AI-Driven Market Linkage and Smart Cataloging Mobile Application for Marginalized Artisans

**Document status:** Product Source of Truth\
**Version:** 1.0\
**Date:** 2026-09-06\
**Primary platform:** Artisan mobile application\
**Secondary platforms:** Customer web application, Business/B2B buyer
web application\
**Primary backend:** Supabase/PostgreSQL + object storage +
application/API layer\
**AI approach:** Hybrid cloud + limited offline/on-device functionality\
**MVP principle:** Build one complete, reliable artisan-to-market
workflow rather than a collection of disconnected AI features.

------------------------------------------------------------------------

# 1. Purpose of This Document

This PRD is the **single source of truth for the product scope**.

All team members, developers, designers, AI agents, database designers,
backend developers, frontend developers, and integration developers
should use this document when deciding what the application should and
should not do.

If a proposed feature is not explicitly included in the **In Scope**
sections or required to support an in-scope workflow, it should be
treated as **out of scope** unless this PRD is formally updated.

The product should prioritize:

1.  Simple artisan experience.
2.  Complete product digitization.
3.  Professional product presentation.
4.  Multilingual accessibility.
5.  Useful price guidance.
6.  Market linkage.
7.  Social-media marketing assistance.
8.  Basic inventory management.
9.  B2B bulk-order connection.
10. Reliable operation in low-connectivity environments.
11. Scalable and cost-conscious architecture.

------------------------------------------------------------------------

# 2. Problem Statement

Marginalized artisans and micro-entrepreneurs often have high-quality
products but face difficulty entering and competing in digital commerce.

The primary barriers are:

-   Low digital literacy.
-   Language barriers.
-   Difficulty creating professional product photographs.
-   Difficulty writing product descriptions.
-   Difficulty creating structured product catalogues.
-   Limited understanding of competitive market pricing.
-   Difficulty marketing products consistently on social media.
-   Limited access to larger buyers and B2B opportunities.
-   Limited technical knowledge required by digital commerce platforms.

The government already provides market exposure through exhibitions,
fairs, cluster programs, and digital market initiatives. The problem
this product addresses is the **digital enablement gap** between the
artisan's physical product and the requirements of modern digital
commerce.

The product is therefore not intended to replace existing marketplaces.

It acts as an **AI-powered virtual business manager and market-linkage
layer** that helps artisans become digitally ready and connect with
potential buyers.

------------------------------------------------------------------------

# 3. Product Vision

> **Help an artisan turn a physical craft into a professional digital
> product listing, understand its market price, market it online,
> maintain availability, and connect with customers and bulk buyers ---
> without requiring advanced digital skills.**

The central workflow is:

**Artisan → Story/Profile → Product → Voice Catalog → AI Photo
Enhancement → Price Guidance → Publish → Market/Promote → Buyer
Connection → Bulk Order Request**

The application should feel like one continuous workflow rather than
separate AI tools.

------------------------------------------------------------------------

# 4. Product Positioning

## What the product is

An AI-powered business companion for artisans that simplifies:

-   Artisan onboarding.
-   Digital identity/profile creation.
-   Product cataloguing.
-   Product photography.
-   Pricing assistance.
-   Multilingual interaction.
-   Social-media marketing.
-   Inventory management.
-   Customer discovery.
-   B2B bulk-order connections.
-   Relevant business/news updates.

## What the product is NOT

The product is not:

-   A replacement for Amazon, GeM, ONDC, IndiaHandmade, or other
    marketplaces.
-   A payment platform.
-   A delivery/logistics platform.
-   A full enterprise ERP.
-   A banking application.
-   An autonomous AI sales agent.
-   A financial advisory platform.
-   A guaranteed price-prediction system.
-   A social network for artisans.
-   A healthcare application.
-   A full-scale CRM.

------------------------------------------------------------------------

# 5. Target Users

The system has three primary user types.

## 5.1 Artisan

The primary user.

A micro-entrepreneur, craftsperson, artisan, weaver, or other seller who
wants to digitize and market products without needing advanced technical
knowledge.

Primary needs:

-   Simple onboarding.
-   Voice-first interaction.
-   Multilingual UI.
-   Easy product creation.
-   Better photographs.
-   Professional descriptions.
-   Price guidance.
-   Product catalogue.
-   Inventory management.
-   Social-media marketing.
-   Customer discovery.
-   Bulk-order opportunities.

Primary platform:

**Mobile application**

------------------------------------------------------------------------

## 5.2 Individual Customer

An individual interested in discovering and purchasing artisan products.

Primary needs:

-   Discover products.
-   Search/filter products.
-   View product details.
-   View artisan profile/story.
-   Understand price and availability.
-   Contact/connect with artisan.
-   Submit product enquiries.

Primary platform:

**Customer web application**

### Important MVP boundary

The customer website does **not** process payment or delivery.

The MVP provides **market linkage and enquiry/connection**, not complete
e-commerce fulfilment.

------------------------------------------------------------------------

## 5.3 Business / B2B Buyer

A business, retailer, organization, event organizer, institution, or
other buyer looking for products in bulk.

Primary needs:

-   Discover artisans/products.
-   Identify products suitable for bulk procurement.
-   Specify quantity and requirements.
-   Send bulk order requests.
-   Review responses.
-   Communicate with artisan.
-   Confirm the bulk order.

Primary platform:

**Business web application**

### Important MVP boundary

The business workflow ends at **order confirmation**.

Payment, shipping, logistics, delivery tracking, invoicing, and external
procurement systems are outside the MVP.

------------------------------------------------------------------------

# 6. Product Scope

## 6.1 Core Features

The product contains the following core features:

1.  Voice-Based Product Cataloging.
2.  AI Product Photo Enhancement.
3.  Dynamic Pricing Assistance.
4.  Multilingual Experience.
5.  Artisan Story/Profile Creation.
6.  Social Media Marketing.
7.  Inventory Management.
8.  Customer Market Linkage.
9.  B2B Bulk Order Dashboard.

## 6.2 Complementary Feature

10. iShots --- relevant short business/market/news updates for artisans.

------------------------------------------------------------------------

# 7. Core Product Workflow

The primary artisan journey should follow this order:

``` text
Login / Sign Up
      ↓
Create Artisan Profile
      ↓
Add Artisan Story
      ↓
Create Product
      ↓
Describe Product by Voice
      ↓
AI Creates Structured Product Information
      ↓
Capture / Upload Product Photos
      ↓
AI Enhances Product Photos
      ↓
Review Product Listing
      ↓
Get Price Recommendation
      ↓
Set Final Selling Price
      ↓
Publish Product
      ↓
Manage Inventory
      ↓
Market Product on Social Media
      ↓
Receive Customer Enquiries / B2B Requests
      ↓
Confirm Bulk Orders
```

Not every artisan has to complete every step immediately.

For example, an artisan can save a product as a draft and return later.

------------------------------------------------------------------------

# 8. User Journey 1 --- Artisan Mobile Application

## 8.1 Authentication

The artisan starts by creating an account or logging in.

### Required

-   Mobile/email authentication.
-   Secure session management.
-   User role identification.
-   Profile completion status.

### Recommended

Mobile-first authentication should be optimized for low-literacy users.

Where practical, the interface should minimize typing.

------------------------------------------------------------------------

# 9. Artisan Profile and Story

After authentication, the artisan is guided through profile creation.

## 9.1 Purpose

The profile represents the **person and craft**, not an individual
product.

The profile should communicate:

-   Who the artisan is.
-   What craft they practice.
-   Their location.
-   Their experience.
-   Their traditional/cultural background.
-   Their craft journey.

## 9.2 Voice-Based Story Input

The artisan can describe their story using voice.

Example:

> "My family has been making Paithani sarees for three generations..."

The system converts the voice input into structured information and an
editable artisan story.

## 9.3 AI Processing

``` text
Voice Input
    ↓
Speech-to-Text
    ↓
Language Detection
    ↓
Information Extraction
    ↓
Structured Artisan Profile
    ↓
AI Story Draft
    ↓
Artisan Review/Edit
    ↓
Publish Profile
```

## 9.4 Data Integrity Rule

The AI must not invent:

-   Family history.
-   Years of experience.
-   Awards.
-   Geographic origin.
-   Traditional claims.
-   Certifications.
-   Materials.
-   Cultural facts.

AI may reorganize, translate, summarize, and professionally rewrite
information provided by the artisan.

The artisan must be able to edit the generated content before
publication.

------------------------------------------------------------------------

# 10. Product Cataloging

This is the primary product workflow.

The artisan should be able to create a product without manually writing
a conventional e-commerce listing.

## 10.1 Input

The artisan provides:

-   Voice description.
-   Product images.
-   Required structured information when missing.

## 10.2 Voice Cataloging Workflow

``` text
Artisan speaks
      ↓
Speech-to-Text
      ↓
Language processing
      ↓
Product information extraction
      ↓
Missing-field detection
      ↓
Simple follow-up questions if required
      ↓
Structured product object
      ↓
Professional product description
      ↓
Artisan review
      ↓
Product listing
```

## 10.3 AI must not hallucinate product attributes

If the artisan does not provide a material, dimension, or other required
attribute, the system must not invent it.

Example:

If the artisan says:

> "This is a hand-painted wooden box."

The AI may infer a category such as "wooden handicraft" if the
classification is sufficiently reliable.

It must not invent:

-   "Teak wood."
-   "10 x 10 cm."
-   "Handmade in 1995."

If required information is missing, the application should ask the
artisan for it.

------------------------------------------------------------------------

# 11. Fixed Product Schema

The MVP uses a fixed product structure.

The system should not allow the LLM to create arbitrary database fields.

## Required/primary product information

-   Product name.
-   Product description.
-   Category.
-   Material.
-   Dimensions.
-   Price.
-   Product images.
-   Artisan profile reference.
-   Stock quantity.
-   Availability status.

## Optional product information

-   Primary colour.
-   Secondary colour.
-   Weight.
-   Tags/keywords.
-   Craft type.
-   Additional notes.

### Colour decision

Colour should be **optional**, not mandatory.

Reason:

-   Product images already communicate colour.
-   Some handmade products have multiple or variable colours.
-   Requiring colour increases artisan effort.
-   A structured colour field can still be useful for search/filtering.

Therefore:

**Primary colour = optional structured field.**

------------------------------------------------------------------------

# 12. Product Categories

Categories should be controlled values rather than arbitrary
LLM-generated text.

Example categories:

-   Textiles.
-   Clothing.
-   Jewellery.
-   Home Décor.
-   Pottery.
-   Woodcraft.
-   Metalcraft.
-   Handicrafts.
-   Paintings.
-   Bags.
-   Accessories.
-   Other.

The exact category list can be expanded through controlled database
configuration.

------------------------------------------------------------------------

# 13. AI Product Photo Enhancement

The photo enhancement feature is part of the product creation workflow.

## 13.1 Purpose

Help artisans create professional-looking product photographs without
requiring photography skills.

## 13.2 Functions

The MVP may include:

-   Background removal.
-   Background cleanup.
-   Lighting correction.
-   Basic quality improvement.
-   Cropping.
-   Aspect-ratio formatting.
-   E-commerce-friendly presentation.

## 13.3 Workflow

``` text
Camera / Gallery
      ↓
Image Upload
      ↓
Image Validation
      ↓
AI Image Enhancement
      ↓
Preview
      ↓
Artisan Approval
      ↓
Store Final Image
      ↓
Attach to Product
```

## 13.4 Original image preservation

The original uploaded image must be preserved separately from the
processed image.

This allows:

-   Reprocessing.
-   Recovery.
-   Comparison.
-   Future improvements.

------------------------------------------------------------------------

# 14. Product Listing

After cataloging and image processing, the artisan receives a complete
product listing preview.

The artisan should be able to:

-   Review.
-   Edit.
-   Save as draft.
-   Publish.
-   Unpublish.
-   Update.

The AI-generated information is a **draft/assistance layer**, not an
irreversible action.

------------------------------------------------------------------------

# 15. Dynamic Pricing Assistant

The pricing feature provides a suggested competitive price based on
available product and market information.

## 15.1 Important Scope Decision

The exact market-data source is **NOT YET DECIDED**.

This must remain a tracked product dependency.

No developer should assume a particular external market-data provider
until the team explicitly decides one.

### Current status

**ON HOLD / DECISION PENDING**

The implementation should therefore be modular enough to support
different market-data sources later.

Do not hard-code the pricing engine around a specific provider.

------------------------------------------------------------------------

# 16. Proposed Pricing Workflow

``` text
Product Information
      +
Artisan Cost Information
      +
Comparable Market Information
      ↓
Market Analysis
      ↓
Price Recommendation
      ↓
Artisan Review
      ↓
Artisan Sets Final Price
```

## Potential inputs

-   Product category.
-   Material.
-   Dimensions.
-   Product characteristics.
-   Artisan-provided cost.
-   Raw material cost.
-   Labour/craft cost where provided.
-   Comparable market prices.
-   Market trends where available.

## Output

The system should provide:

-   Suggested price.
-   Suggested price range.
-   Brief explanation of major factors.
-   Market comparison where data is available.

The artisan always controls the final price.

## Pricing disclaimer

The system must not present the recommendation as a guaranteed "correct
price."

Use language such as:

> "Suggested market range"

or

> "Recommended selling range"

rather than:

> "Guaranteed optimal price."

------------------------------------------------------------------------

# 17. Multilingual Experience

MVP languages:

1.  English.
2.  Hindi.
3.  Marathi.

The application should support multilingual interaction across the
artisan workflow.

## Priority

Voice interaction is particularly important for:

-   Product descriptions.
-   Artisan stories.
-   Navigation assistance where supported.

## Language architecture

The system should separate:

-   UI language.
-   Input language.
-   Output language.

For example, an artisan may speak Marathi while the generated product
description is produced in English and/or Hindi.

## Important limitation

Do not claim support for all Indian or all regional languages in the
MVP.

Additional languages can be added later.

------------------------------------------------------------------------

# 18. Social Media Marketing

The social-media feature helps artisans promote products after creating
a product listing.

## 18.1 MVP outputs

The system should generate:

-   Promotional image/poster.
-   Social-media caption.
-   Hashtags.
-   Short promotional copy.
-   Product-focused marketing content.

## 18.2 Optional direct publishing

Direct Instagram publishing may be supported as an integration.

However, the application must also work if direct publishing is
unavailable.

Therefore:

``` text
Product
 ↓
AI Marketing Generator
 ↓
Poster + Caption + Hashtags
 ↓
Artisan Review
 ↓
    ├── Save/Share/Download
    └── Optional authorized Instagram publishing
```

## 18.3 User authorization

The system must never publish content to an artisan's social-media
account without explicit authorization.

The artisan should have an approval step before publishing.

## 18.4 Direct Instagram integration status

The product should treat direct publishing as an **optional
integration**, not a dependency for the core product.

------------------------------------------------------------------------

# 19. Inventory Management

Inventory is an explicit part of the product and should be implemented
properly, while remaining simple.

## 19.1 Inventory model

Each product should maintain:

-   Stock quantity.
-   Availability status.
-   Optional low-stock threshold.
-   Last inventory update.

## 19.2 Availability statuses

Recommended:

-   Draft.
-   Available.
-   Low Stock.
-   Out of Stock.
-   Made to Order.
-   Unavailable.

## 19.3 Basic inventory operations

The artisan can:

-   Add stock.
-   Reduce stock.
-   Set stock quantity.
-   Mark as made-to-order.
-   Mark unavailable.
-   Restore availability.

## 19.4 MVP does not include

-   Warehouse management.
-   Multi-location warehouses.
-   Purchase-order management.
-   Supplier inventory.
-   Automated procurement.
-   Advanced forecasting.

------------------------------------------------------------------------

# 20. Individual Customer Website

The customer website exists to provide market visibility and direct
connection.

## 20.1 Customer can

-   Browse products.
-   Search products.
-   Filter products.
-   View product images.
-   View product information.
-   View artisan profile.
-   Read artisan story.
-   See price.
-   See availability.
-   Send an enquiry/contact request.

## 20.2 Customer cannot in MVP

-   Make online payment.
-   Request delivery.
-   Track shipping.
-   Select a courier.
-   Generate shipping labels.
-   Receive automated delivery estimates.
-   Use an integrated checkout system.

## 20.3 Why

The core problem statement is about **market linkage and digital
readiness**, not building an end-to-end e-commerce logistics company.

Keeping payment and delivery out prevents unnecessary scope expansion.

------------------------------------------------------------------------

# 21. Business / B2B Buyer Website

The B2B website provides a separate experience for businesses requiring
larger quantities.

## 21.1 Business can

-   Register/login.
-   Browse products.
-   Search/filter products.
-   View artisan profiles.
-   View product availability.
-   Enter desired bulk quantity.
-   Submit a bulk order request.
-   Add requirements/notes.
-   View request status.
-   Communicate/respond through the request workflow.
-   Confirm an order after artisan acceptance.

## 21.2 Bulk Order Workflow

``` text
Business finds product
       ↓
Selects bulk quantity
       ↓
Adds requirements
       ↓
Submits bulk request
       ↓
Artisan receives request
       ↓
Artisan reviews
       ↓
Artisan accepts / rejects / requests clarification
       ↓
Business and artisan communicate if necessary
       ↓
Final quantity / price / requirements agreed
       ↓
Order confirmed
```

## 21.3 Important boundary

The system ends at:

**Confirmed Bulk Order**

It does not handle:

-   Payment.
-   Delivery.
-   Logistics.
-   Shipping provider integration.
-   Shipment tracking.
-   Invoice settlement.
-   Escrow.

These are future scope.

------------------------------------------------------------------------

# 22. Bulk Order Data

A bulk request should contain at minimum:

-   Buyer.
-   Product.
-   Requested quantity.
-   Artisan.
-   Request date.
-   Required-by date if provided.
-   Buyer notes.
-   Artisan response.
-   Final agreed quantity.
-   Final agreed price.
-   Final requirements.
-   Status.

------------------------------------------------------------------------

# 23. Bulk Order Status

Recommended lifecycle:

``` text
REQUESTED
   ↓
UNDER_REVIEW
   ↓
CLARIFICATION_REQUIRED (optional)
   ↓
ACCEPTED
   ↓
CONFIRMED
```

Alternative terminal states:

-   REJECTED.
-   CANCELLED.

The status must clearly distinguish:

**Artisan accepted the request**

from

**Both sides confirmed the order.**

------------------------------------------------------------------------

# 24. Order Scope

The MVP uses the term "order" only for the basic commercial record
created after a business bulk request is accepted and confirmed.

The order record is informational and operational.

It is not a payment or fulfilment system.

## Order does not contain

-   Payment transaction processing.
-   Shipping transaction.
-   Courier assignment.
-   Delivery tracking.
-   Refund processing.
-   Tax invoice settlement.
-   Escrow.

These are future integrations.

------------------------------------------------------------------------

# 25. iShots

iShots is the complementary information feature.

## Purpose

Provide artisans with short, useful market/business information relevant
to their craft.

Examples:

-   Craft industry news.
-   Relevant market trends.
-   Festival-related opportunities.
-   Government/artisan scheme updates.
-   Relevant consumer trends.
-   Craft-specific business information.

## Proposed source

A news API or other licensed news/content source.

## Filtering

News should be filtered using the artisan's profile information, such
as:

-   Craft category.
-   Product category.
-   Region where relevant.
-   Business interests.

Example:

An artisan whose profile indicates:

> Textile / Handloom

should receive more textile/handloom-related updates than unrelated
technology news.

## Important rule

The system must not present every news article as relevant merely
because an AI model can summarize it.

The relevance pipeline should first identify relevant content and then
summarize it.

## MVP output

Each iShot should be short and actionable.

Possible structure:

**Headline**

**Why it matters to you**

**Short summary**

**Source**

**Date**

**Optional action**

## News API dependency

The exact news API/provider is not yet fixed.

The implementation should therefore isolate the news provider behind a
service layer.

Do not tightly couple the entire application to one provider.

------------------------------------------------------------------------

# 26. Offline Functionality

The application must support basic functionality when connectivity is
unavailable.

The goal is **not** to make the entire AI system run offline.

The goal is:

> **The app should remain useful offline and synchronize when
> connectivity returns.**

## 26.1 Offline capabilities

The artisan should be able to:

-   Open the application.
-   View previously downloaded/saved information.
-   View existing drafts.
-   Record product voice input.
-   Capture product photos.
-   Save product drafts.
-   View saved artisan profile information.
-   View saved products.
-   Perform basic local edits.
-   Queue data for synchronization.

## 26.2 Online-dependent capabilities

These may require cloud connectivity:

-   Advanced speech-to-text.
-   Advanced catalog generation.
-   Complex translation.
-   AI image enhancement.
-   Market analysis.
-   AI marketing generation.
-   News retrieval.
-   Cloud synchronization.
-   Social-media publishing.

## 26.3 Offline architecture principle

``` text
No Internet
    ↓
Capture / Edit / Save Locally
    ↓
Pending Sync Queue
    ↓
Internet Returns
    ↓
Upload / Synchronize
    ↓
Cloud AI Processing
    ↓
Updated Result
    ↓
Sync Back to Device
```

## 26.4 Important terminology

The product should be described as:

> **Offline-first with cloud AI assistance**

rather than:

> Fully offline AI application.

------------------------------------------------------------------------

# 27. Simple AI Architecture

The architecture should remain understandable.

## 27.1 Local/device side

Only lightweight functionality needed for basic operation should run
locally.

Examples:

-   Local data storage.
-   Basic voice/navigation functionality where technically supported.
-   Basic intent recognition where technically feasible.
-   Draft management.
-   Sync queue.

## 27.2 Cloud side

Advanced processing occurs in cloud services:

-   Speech-to-text.
-   Product information extraction.
-   Description generation.
-   Translation.
-   Image enhancement.
-   Pricing analysis.
-   Marketing content generation.
-   iShot relevance and summarization.

## 27.3 Design principle

Do not force a large language model onto every device.

The application should use the cloud for heavier AI tasks and keep local
functionality lightweight.

------------------------------------------------------------------------

# 28. AI Safety and Data Rules

## 28.1 AI-generated text must be editable

Artisans must be able to review and modify:

-   Product descriptions.
-   Artisan stories.
-   Marketing content.
-   Price suggestions.

## 28.2 AI must not invent facts

Particularly:

-   Product materials.
-   Dimensions.
-   Certifications.
-   Artisan history.
-   Awards.
-   Geographic claims.
-   Cultural heritage claims.

## 28.3 Price recommendations are advisory

The artisan controls the final price.

## 28.4 Image processing must preserve originals

Original user-uploaded images must remain available separately from
processed images.

------------------------------------------------------------------------

# 29. Authentication and Authorization

The system must support role-based access.

Roles:

-   Artisan.
-   Individual Customer.
-   Business Buyer.
-   Administrator.

## Artisan permissions

Artisans can manage:

-   Their profile.
-   Their story.
-   Their products.
-   Product images.
-   Inventory.
-   Pricing.
-   Marketing content.
-   Bulk-order requests associated with their products.

## Customer permissions

Customers can:

-   View public products.
-   View public artisan profiles.
-   Submit enquiries.

## Business permissions

Businesses can:

-   View public products.
-   Submit bulk requests.
-   View their own requests.
-   Manage their own request workflow.

## Administrator permissions

Administrators may:

-   Manage categories.
-   Moderate content.
-   Manage platform configuration.
-   Review system-level information.

Administrators should not automatically have unrestricted access to
private user data.

------------------------------------------------------------------------

# 30. Data Ownership Principles

## Artisan-owned data

Examples:

-   Profile.
-   Story.
-   Product listings.
-   Product images.
-   Inventory.
-   Pricing.
-   Marketing drafts.

An artisan should only be able to modify their own records.

## Buyer-owned data

Examples:

-   Account information.
-   Enquiries.
-   Bulk-order requests.

A buyer should only be able to modify their own records.

## Public data

Examples:

-   Published product listing.
-   Published product images.
-   Public artisan profile.
-   Public artisan story.

The system must clearly distinguish draft/private information from
published information.

------------------------------------------------------------------------

# 31. Media Storage

The system will need object storage for:

-   Artisan profile photos.
-   Product originals.
-   Product enhanced images.
-   Marketing creatives.
-   Optional audio recordings.
-   Other approved media.

## Storage principles

-   Do not store large binary files directly in PostgreSQL.
-   Store files in object storage.
-   Store metadata and references in PostgreSQL.
-   Use unique object paths.
-   Validate file type and size.
-   Use access policies.
-   Generate optimized versions where necessary.
-   Keep original and processed images separate.

------------------------------------------------------------------------

# 32. AI Job Architecture

AI processing should not block the entire application when a task takes
time.

Potential AI jobs:

-   Voice transcription.
-   Product extraction.
-   Product description generation.
-   Translation.
-   Image enhancement.
-   Pricing analysis.
-   Marketing generation.
-   iShot summarization/relevance.

A generic AI-job pattern should be considered:

``` text
User Action
    ↓
Create AI Job
    ↓
Queue / Worker
    ↓
AI Provider
    ↓
Validate Output
    ↓
Store Result
    ↓
Update Application
```

This makes the system easier to scale and makes provider changes easier
later.

------------------------------------------------------------------------

# 33. AI Provider Abstraction

AI providers should not be hard-coded into every feature.

The application should have a service abstraction such as:

``` text
SpeechService
CatalogService
TranslationService
ImageService
PricingService
MarketingService
NewsService
```

Each service can internally use one or more external providers.

This allows the team to change:

-   Model.
-   Provider.
-   API version.
-   Cost tier.

without rewriting the entire application.

------------------------------------------------------------------------

# 34. AI Cost Control Requirements

AI usage can become one of the largest variable costs.

The system should therefore track AI usage.

Potential usage information:

-   User.
-   Feature.
-   AI provider.
-   Model.
-   Request count.
-   Input units/tokens where available.
-   Output units/tokens where available.
-   Image processing count.
-   Audio duration where applicable.
-   Estimated cost.
-   Timestamp.
-   Job status.

## Cost-control principles

1.  Do not call an LLM when deterministic code is sufficient.
2.  Avoid repeatedly processing the same image.
3.  Cache reusable AI results.
4.  Store generated content.
5.  Resize/compress images before expensive processing where
    appropriate.
6.  Use smaller models for simple extraction/classification.
7.  Use larger models only for tasks that actually need them.
8.  Process news in batches where possible.
9.  Avoid generating multiple marketing variants unnecessarily.
10. Maintain configurable usage limits.

------------------------------------------------------------------------

# 35. Product Image Cost Strategy

Images can become expensive in both AI processing and storage.

The system should:

-   Preserve the original.
-   Generate optimized display versions.
-   Avoid unnecessary duplicate processing.
-   Use thumbnails for catalogue browsing.
-   Use full-resolution assets only when needed.
-   Store metadata describing image processing status.

Example image lifecycle:

``` text
Original
  ↓
Validation
  ↓
AI Enhancement
  ↓
Enhanced Master
  ↓
Thumbnail / Web Optimized Version
```

------------------------------------------------------------------------

# 36. Database Principles

The database must be designed for scalability from the beginning.

Requirements:

-   PostgreSQL.
-   UUID-based primary identifiers.
-   Foreign-key relationships.
-   Appropriate indexes.
-   Constraints.
-   Created/updated timestamps.
-   Soft deletion where appropriate.
-   Role-aware access control.
-   Row Level Security where applicable.
-   Normalized transactional data.
-   JSON/JSONB only where flexibility is actually useful.
-   No arbitrary product schema generated by AI.

The database specification will be produced after this PRD is locked.

------------------------------------------------------------------------

# 37. Search Requirements

Customer and business users should be able to discover products through:

-   Product name.
-   Category.
-   Material.
-   Craft type where available.
-   Optional colour.
-   Artisan.
-   Availability.

Search should be designed so that it can later support semantic/AI
search without requiring a complete database redesign.

However, semantic search is not a mandatory MVP requirement unless
needed for the implemented prototype.

------------------------------------------------------------------------

# 38. Product Publication States

Products should have controlled lifecycle states:

``` text
DRAFT
   ↓
READY_FOR_REVIEW
   ↓
PUBLISHED
   ↓
UNPUBLISHED
```

Possible additional state:

-   ARCHIVED.

A product should not become publicly visible simply because an AI
generation step completed.

The artisan must explicitly publish it.

------------------------------------------------------------------------

# 39. Error Handling

The application must clearly handle:

## No internet

Show:

> "You're offline. Your work has been saved and will sync when you're
> connected."

Do not silently fail.

## AI processing failure

Show:

> "We couldn't process this right now. Your original information has
> been saved."

Allow retry.

## Missing product information

Ask a simple follow-up question rather than inventing data.

## Image failure

Preserve the original and allow retry/manual use.

## API provider failure

The application should degrade gracefully where possible.

------------------------------------------------------------------------

# 40. Reliability Principle

The application should follow:

> **Graceful degradation.**

When connectivity or an AI provider is unavailable:

-   The application should not lose artisan data.
-   Drafts should remain available.
-   Original images should remain available.
-   Pending work should be queued.
-   AI-dependent steps should be clearly marked as pending.
-   The user should know what happened.

------------------------------------------------------------------------

# 41. Scalability Requirements

The system should be designed so that the following can scale
independently:

-   API requests.
-   AI jobs.
-   Image processing.
-   News processing.
-   Social-media processing.
-   Database operations.
-   File storage.

AI workloads should not require the main application server to remain
blocked while processing.

Use asynchronous/background processing where appropriate.

------------------------------------------------------------------------

# 42. Security Requirements

Minimum requirements:

-   Secure authentication.
-   Password/credential protection through managed authentication where
    possible.
-   Authorization checks on every protected resource.
-   Row Level Security for user-owned data.
-   Secure object storage.
-   HTTPS/TLS for network communication.
-   No API keys embedded in mobile/web clients.
-   Server-side secrets management.
-   Input validation.
-   File validation.
-   Rate limiting for public/expensive endpoints.
-   Audit logging for important actions.
-   Secure handling of third-party OAuth tokens.

------------------------------------------------------------------------

# 43. API Key and Secret Management

External API keys must never be placed directly in:

-   Mobile source code.
-   Public frontend code.
-   Git repositories.
-   Client-side environment variables that are shipped to users.

Secrets should remain server-side.

Examples:

-   AI API keys.
-   News API keys.
-   Social-media credentials.
-   Storage service secrets.

------------------------------------------------------------------------

# 44. Third-Party Integration Strategy

The architecture should isolate external integrations.

Potential integrations:

-   AI provider.
-   Speech-to-text provider.
-   Translation provider.
-   News API.
-   Instagram/Meta APIs.
-   Optional future marketplace integrations.

No external provider should be treated as the core database or source of
truth for the product.

The application's database remains the source of truth for:

-   Artisan profiles.
-   Products.
-   Inventory.
-   Bulk requests.
-   Product status.
-   Generated content.

------------------------------------------------------------------------

# 45. Marketplace Integration

The problem statement mentions connection to larger B2B buyers or
government e-marketplaces.

For MVP, the product should focus on **market readiness and linkage**.

The architecture should allow future integration with:

-   Government e-marketplaces.
-   ONDC ecosystem participants.
-   Other e-commerce channels.

However, a full marketplace integration is **not required unless
explicitly implemented and tested**.

Do not claim direct live integration with a marketplace merely because
the architecture could support it.

------------------------------------------------------------------------

# 46. Customer and Business Website Relationship

These are two separate experiences.

## Customer Website

Purpose:

**Discover + connect**

Primary action:

**Enquiry / contact**

## Business Website

Purpose:

**Discover + request bulk quantity + confirm order**

Primary action:

**Bulk order request**

This separation prevents the MVP from becoming a complete consumer
e-commerce system.

------------------------------------------------------------------------

# 47. Non-Goals / Explicitly Out of Scope

The following are NOT part of the current MVP.

## Payments

-   Payment gateway.
-   UPI integration.
-   Card payments.
-   Wallets.
-   Payment settlement.
-   Refund processing.

**Status: Future scope**

## Delivery and Logistics

-   Courier integration.
-   Delivery tracking.
-   Shipping labels.
-   Logistics partner APIs.
-   Route optimization.
-   Delivery estimates.

**Status: Future scope**

## Advanced B2B Commerce

-   Automated procurement.
-   Purchase orders.
-   Invoicing.
-   GST workflow.
-   Escrow.
-   Contract management.

**Status: Future scope**

## AI Demand Intelligence

Removed from current scope.

No:

-   Demand forecasting.
-   Buyer trend prediction engine.
-   Product recommendation engine for artisans.

## AI Scam Protection

Removed from current scope.

No:

-   Scam scoring.
-   Fraud detection.
-   Buyer risk scoring.

## Artisan Collaboration Network

Removed from current scope.

No:

-   Artisan-to-artisan matching.
-   Shared production network.
-   Collaborative fulfilment.

## AI Negotiation

Removed from current scope.

No autonomous negotiation agent.

Bulk-order requests use a normal review/response workflow.

## Hybrid Pricing

Removed.

The current pricing feature is a **market-analysis-based pricing
assistant**, not a complex hybrid ML/rule-based pricing system.

## Blockchain

Not part of the MVP.

## IoT / Wearables

Not part of the MVP.

## Healthcare

Not part of the MVP.

## Full Offline LLM

Not required.

## Full Social Network

Not part of the MVP.

## Autonomous Social Media Agent

The system must not autonomously post without user authorization.

## Full Marketplace Replacement

The product is not intended to replace established commerce platforms.

------------------------------------------------------------------------

# 48. Feature Priority

## P0 --- Must Work

1.  Authentication.
2.  Artisan profile.
3.  Artisan voice story.
4.  Product creation.
5.  Voice cataloging.
6.  Product image upload.
7.  AI image enhancement.
8.  Fixed product listing.
9.  Multilingual support.
10. Product pricing assistance.
11. Product publication.
12. Inventory.
13. Customer product discovery.
14. Artisan profile discovery.
15. Customer enquiry.
16. Business bulk-order request.
17. Artisan bulk-order dashboard.
18. Bulk-order confirmation.
19. Basic offline drafts/sync.

## P1 --- Important

20. AI social-media content generation.
21. Optional Instagram publishing.
22. iShots.
23. Advanced search/filtering.
24. Improved offline capabilities.
25. AI job monitoring/retry.

## P2 --- Future

26. Payment.
27. Delivery.
28. Marketplace integrations.
29. Additional languages.
30. Advanced market intelligence.
31. Demand forecasting.
32. Advanced B2B procurement.
33. Advanced analytics.

------------------------------------------------------------------------

# 49. Definition of Done --- Core Artisan Workflow

The MVP core workflow is considered complete when an artisan can:

1.  Create an account.
2.  Select a language.
3.  Create an artisan profile.
4.  Record their story through voice.
5.  Review/edit the generated story.
6.  Create a product.
7.  Describe the product using voice.
8.  Have the system generate structured product information.
9.  Fill missing required fields.
10. Upload/take product photos.
11. Generate enhanced product images.
12. Review the product listing.
13. Receive a market-based price suggestion when market data is
    available.
14. Set the final price.
15. Set stock/availability.
16. Publish the product.
17. View the published product.
18. Generate social-media marketing content.
19. Save work while offline.
20. Synchronize pending work after reconnecting.

------------------------------------------------------------------------

# 50. Definition of Done --- Customer Workflow

A customer can:

1.  Register/login where required.
2.  Browse products.
3.  Search/filter products.
4.  Open a product.
5.  View images.
6.  View product details.
7.  View artisan profile/story.
8.  See price and availability.
9.  Submit an enquiry/contact request.

No payment or delivery is required for MVP completion.

------------------------------------------------------------------------

# 51. Definition of Done --- Business Workflow

A business buyer can:

1.  Register/login.
2.  Browse products.
3.  Identify a product for bulk procurement.
4.  Enter quantity.
5.  Add requirements.
6.  Submit a bulk request.
7.  Artisan receives the request.
8.  Artisan reviews it.
9.  Artisan accepts/rejects/requests clarification.
10. Business receives the response.
11. Both sides can finalize the request.
12. The system creates a confirmed bulk order record.

No payment or delivery integration is required.

------------------------------------------------------------------------

# 52. Definition of Done --- Social Media

The MVP social-media workflow is complete when:

1.  An artisan selects a published product.
2.  The system generates promotional content.
3.  The system generates a caption.
4.  The system generates hashtags.
5.  The system creates a suitable marketing creative.
6.  Artisan previews the content.
7.  Artisan can save/share/download it.
8.  If direct Instagram integration is implemented, the artisan
    explicitly authorizes the account and approves publishing.

------------------------------------------------------------------------

# 53. Definition of Done --- iShots

The iShots feature is complete when:

1.  News/content can be retrieved from the selected provider.
2.  Relevant content can be identified using artisan/product context.
3.  Irrelevant content is filtered.
4.  The system produces a short summary.
5.  The original source is retained.
6.  The artisan can view the iShot.
7.  The feature handles API failure gracefully.

The news provider remains a pending decision.

------------------------------------------------------------------------

# 54. Pending Product Decisions

These decisions are intentionally NOT finalized and must not be silently
assumed by developers.

## PENDING-001 --- Market Data Provider

**Question:**

Where will the pricing assistant obtain comparable/current market
information?

Potential options:

-   External marketplace/API data.
-   Licensed market-data source.
-   Curated dataset.
-   Government/open dataset.
-   Manually seeded prototype dataset.
-   Other approved source.

### Current status

**ON HOLD**

The pricing implementation must remain provider-agnostic.

------------------------------------------------------------------------

## PENDING-002 --- News API Provider

The iShots feature requires a news/content provider.

### Current status

**ON HOLD**

Create an abstraction so the provider can be selected later.

------------------------------------------------------------------------

## PENDING-003 --- Direct Instagram Publishing

The core social-media feature must work without direct publishing.

Direct publishing is optional and depends on platform permissions/API
requirements.

------------------------------------------------------------------------

# 55. Product Design Principles

## Principle 1 --- Voice First

The artisan should not need to type large amounts of information.

## Principle 2 --- AI Assists, Artisan Decides

AI creates drafts and recommendations.

The artisan remains in control.

## Principle 3 --- Simple Language

Avoid technical terminology in the artisan interface.

Instead of:

> "Run AI attribute extraction"

use:

> "Create my product"

## Principle 4 --- Save Before Processing

User data should be saved before expensive AI processing wherever
practical.

## Principle 5 --- Never Lose Work

Offline mode, provider failures, and network interruptions must not
erase drafts.

## Principle 6 --- One Workflow

Features should connect to the product lifecycle.

Avoid isolated AI demos.

## Principle 7 --- Scalable Backend

The prototype should not require a complete rewrite to support many
artisans.

------------------------------------------------------------------------

# 56. Recommended End-to-End System

At a high level:

``` text
                  ARTISAN MOBILE APP
                         |
        +----------------+----------------+
        |                |                |
     Profile          Products         Inventory
        |                |                |
      Story       Voice + Photos       Stock
        |                |                |
        +----------------+----------------+
                         |
                      Backend
                         |
       +-----------------+-----------------+
       |                 |                 |
    Database          Storage          AI Services
       |                 |                 |
   PostgreSQL       Product Images     Cataloging
                                      Image AI
                                      Translation
                                      Pricing
                                      Marketing
                         |
              +----------+----------+
              |                     |
       CUSTOMER WEBSITE       BUSINESS WEBSITE
              |                     |
        Browse/Enquiry        Bulk Requests
                                    |
                              Order Confirmation
```

------------------------------------------------------------------------

# 57. High-Level Data Flow

## Product

``` text
Voice + Photo
     ↓
Product Draft
     ↓
AI Processing
     ↓
Structured Product
     ↓
Enhanced Images
     ↓
Pricing
     ↓
Artisan Review
     ↓
Published Product
```

## Customer

``` text
Published Product
     ↓
Customer Discovery
     ↓
Product / Artisan Profile
     ↓
Enquiry
```

## Business

``` text
Published Product
     ↓
Business Discovery
     ↓
Bulk Request
     ↓
Artisan Review
     ↓
Clarification
     ↓
Acceptance
     ↓
Confirmation
```

------------------------------------------------------------------------

# 58. Expected Technical Architecture

The system should be organized into logical layers.

## Client layer

-   Artisan mobile application.
-   Customer web application.
-   Business web application.

## Application layer

-   Authentication.
-   Artisan profile service.
-   Product service.
-   Inventory service.
-   Enquiry service.
-   Bulk-order service.
-   Marketing service.
-   iShot service.
-   AI orchestration service.

## Data layer

-   PostgreSQL.
-   Object storage.
-   Optional caching.
-   Job/status storage.

## AI layer

-   Speech-to-text.
-   Language processing.
-   Structured extraction.
-   Translation.
-   Image enhancement.
-   Pricing analysis.
-   Marketing generation.
-   News relevance/summarization.

## Integration layer

-   AI providers.
-   News provider.
-   Instagram/Meta API where enabled.
-   Future marketplace APIs.

------------------------------------------------------------------------

# 59. Cost-Aware Architecture

The application should be designed around the principle:

> **Use expensive AI only when it adds meaningful value.**

Examples:

### Voice cataloging

Do not send the same audio repeatedly to multiple models.

``` text
Audio
 ↓
STT
 ↓
Structured extraction
 ↓
Store result
```

If the artisan only edits the product name, do not regenerate the entire
product description unnecessarily.

### Image enhancement

Do not process an image every time the product page is opened.

Process once and store the result.

### iShots

Do not call an LLM separately for every artisan for every article.

Prefer:

``` text
News ingestion
 ↓
Relevance/filtering
 ↓
Batch summarization where practical
 ↓
Store iShot
 ↓
Personalized delivery
```

### Marketing

Generate content on demand and cache the result.

------------------------------------------------------------------------

# 60. Observability

The backend should track:

-   API errors.
-   AI job failures.
-   Processing time.
-   Request volume.
-   Storage usage.
-   AI usage.
-   Estimated AI cost.
-   Failed synchronizations.
-   Third-party integration failures.

This is necessary both for scaling and for controlling costs.

------------------------------------------------------------------------

# 61. Analytics

Basic operational analytics may include:

-   Number of artisans.
-   Number of published products.
-   Number of product drafts.
-   Product creation completion rate.
-   AI processing success rate.
-   Number of customer enquiries.
-   Number of bulk requests.
-   Bulk request confirmation rate.
-   Inventory status.
-   Social-media content generated.

Advanced AI demand analytics are explicitly out of scope.

------------------------------------------------------------------------

# 62. Future Expansion

The architecture should allow later addition of:

-   Payments.
-   Logistics.
-   Government marketplace integrations.
-   ONDC integrations.
-   More languages.
-   Advanced market intelligence.
-   Demand forecasting.
-   B2B procurement.
-   Buyer recommendations.
-   Artisan collaboration.
-   Scam protection.
-   AI negotiation.

These features should be added as modular services rather than being
deeply coupled to the MVP.

------------------------------------------------------------------------

# 63. Final Scope Lock

The current product is fundamentally:

> **An AI-powered digital business assistant that helps artisans create
> professional product listings from voice and photos, understand
> market-based pricing, build their digital identity, market products
> through social media, manage availability, and connect with individual
> customers and bulk buyers.**

The primary transformation is:

``` text
Traditional Physical Product
          ↓
AI-Assisted Digitization
          ↓
Professional Digital Listing
          ↓
Market Visibility
          ↓
Customer / B2B Connection
```

The MVP does **not** attempt to solve the entire commerce lifecycle.

The MVP ends at:

-   Product publication.
-   Customer enquiry/connection.
-   Confirmed bulk-order request.

**Payment and delivery remain future scope.**

------------------------------------------------------------------------

# 64. Source-of-Truth Rule for Development

Before implementing a new feature, ask:

1.  Is it explicitly in this PRD?
2.  Does it directly support an in-scope workflow?
3.  Is it required by the original problem statement?
4.  Is it required for security, reliability, scalability, or
    infrastructure?
5.  Is it listed as future scope?

If the answer is no, **do not add the feature to the MVP without
updating this PRD first.**

This rule exists to prevent scope creep.

------------------------------------------------------------------------

# 65. Next Engineering Deliverable

After this PRD is approved and frozen, the next document should be:

**Database Specification**

It should derive the database strictly from the workflows and entities
defined here.

The database design should identify and define, as required:

-   Users.
-   Roles.
-   Artisan profiles.
-   Artisan stories.
-   Products.
-   Product images.
-   Product categories.
-   Inventory.
-   Pricing records.
-   Market-data records.
-   Customer enquiries.
-   Business buyers.
-   Bulk-order requests.
-   Confirmed orders.
-   Social accounts.
-   Marketing content.
-   iShots.
-   AI jobs.
-   AI usage/cost records.
-   Synchronization records where necessary.
-   Audit/security records where necessary.

Only after those entities and relationships are reviewed should the team
generate the Supabase/PostgreSQL SQL schema.

------------------------------------------------------------------------

# 66. Final Product Workflow Summary

``` text
                         ARTISAN
                            |
                         LOGIN
                            |
                  ARTISAN PROFILE + STORY
                            |
                     CREATE PRODUCT
                            |
                    VOICE CATALOGING
                            |
                  AI STRUCTURED LISTING
                            |
                   PRODUCT PHOTOGRAPHY
                            |
                   AI IMAGE ENHANCEMENT
                            |
                     REVIEW LISTING
                            |
                    MARKET PRICE GUIDE
                            |
                     SET FINAL PRICE
                            |
                    SET INVENTORY/STOCK
                            |
                       PUBLISH
                            |
              +-------------+-------------+
              |                           |
       SOCIAL MARKETING              MARKET LINKAGE
              |                           |
       AI POST/CAPTION             CUSTOMER WEBSITE
              |                           |
       Optional Instagram             ENQUIRY
                                      |
                               BUSINESS WEBSITE
                                      |
                                 BULK REQUEST
                                      |
                               ARTISAN REVIEWS
                                      |
                               ACCEPT / CLARIFY
                                      |
                                CONFIRM ORDER

                         +
                         |
                       iShots
                         |
             Relevant craft/business/news
```

**This workflow is the MVP boundary.**
