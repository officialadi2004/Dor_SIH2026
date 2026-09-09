# डोर (Dor) app

डोर is an Android application built for SIH2026. It is designed to help artisans manage their profiles, business catalogs, products, and marketing efforts, integrating with Supabase for backend services and authentication.

## Main Features
* **Artisan Profiles:** Allows artisans to register, manage their profiles, and showcase their craft.
* **Product Management:** Add, edit, and manage products.
* **Marketing Hub:** Automated marketing generation and publishing workflows.
* **Authentication:** Secure login and registration using Supabase Auth.
* **Image Handling:** Supports image uploads and enhancements.

## Technology Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow
* **Backend as a Service:** Supabase (Auth, Postgrest, Storage)
* **Networking:** Ktor Client
* **Image Loading:** Coil
* **Navigation:** Jetpack Navigation Compose
* **Workflow Automation:** n8n webhooks for marketing

## Architecture & Components
* **`ui/screens/`**: Contains all Jetpack Compose screens (Login, Profile, AddProduct, Marketing, etc.).
* **`data/remote/`**: Providers and clients for remote services like Supabase.
* **`data/repository/`**: Repository layer connecting UI with data sources.
* **`ui/navigation/`**: Navigation graphs and routing.

## How to Run the Project
1. Clone this repository.
2. Open the project in Android Studio (Jellyfish or newer recommended).
3. Create a `local.properties` file in the project root (if not present).
4. Add your required API keys to `local.properties` (see below).
5. Sync the project with Gradle files.
6. Run the `app` configuration on an emulator or physical device.

## Required Configuration
Before running the app, you need to configure your environment variables. 
Create or edit `local.properties` in the project root and add the following keys:

```properties
# Supabase Configuration
SUPABASE_URL="YOUR_SUPABASE_URL"
SUPABASE_ANON_KEY="YOUR_SUPABASE_ANON_KEY"

# n8n Webhooks Configuration
N8N_MARKETING_GENERATE_URL="YOUR_N8N_WEBHOOK_URL_FOR_GENERATE"
N8N_MARKETING_PUBLISH_URL="YOUR_N8N_WEBHOOK_URL_FOR_PUBLISH"
```
*(Make sure to update `app/build.gradle.kts` to load these properties instead of hardcoding them!)*

## Screenshots
> *(Screenshots placeholder: Add screenshots of the Login, Profile, and Marketing screens here)*

## Future Improvements
* Improve offline caching using Room Database.
* Add comprehensive Unit and UI tests.
* Enhance accessibility support across all Compose screens.
* Add CI/CD pipeline (e.g., GitHub Actions) for automated testing and builds.
