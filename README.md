# SilentSkip 🎙️

SilentSkip is a modern, lightweight Android application built with Kotlin and Jetpack Compose, designed for efficient background audio recording with sound detection capabilities.

## 🚀 Features

- **Background Audio Recording**: Continuous or sound-activated recording via a persistent Foreground Service with WakeLock.
- **Sound Detection / Silence Skipping**: Automatically detects sound levels to record only relevant audio segments.
- **Monetization & Ads**: Real Google Mobile Ads (AdMob) integration supporting adaptive banners and preloaded interstitials.
- **Subscriptions**: Google Play Billing Library integration supporting monthly and annual recurring subscriptions.
- **Multi-language Support**: Fully localized in English, Portuguese, and Spanish.

## 🏛️ Architecture

The project strictly follows **Clean Architecture** principles, separating responsibilities into three distinct layers:
- **Domain Layer**: Contains business logic, Interactors/UseCases, contracts, and immutable data models.
- **Data Layer**: Implements repositories, local data sources (SharedPreferences, local file storage), and billing clients.
- **Presentation Layer**: Built with **Jetpack Compose** and **Material 3**, utilizing ViewModels with unidirectional data flow (UDF) powered by Kotlin Coroutines and `StateFlow`.
- **Dependency Injection**: Fully wired using **Dagger Hilt**.

## 💡 Jetpack Compose Key Learnings & Best Practices

1. **Lifecycle-Aware State Collection**: Utilizing `collectAsStateWithLifecycle()` ensures that Compose UI safely collects flows respecting the lifecycle of the Composable, preventing memory leaks and unnecessary background emissions when the app goes to the background.
2. **Reusable & Modular UI Components**: Building decoupled, modular UI components (such as custom bottom bars, banner ad views, and interactive dialogs). In larger, enterprise-scale multi-module projects, these reusable components can easily be extracted into a **separate design system / component module** to promote reusability and maintainability across feature teams.

## 📱 Availability

⏳ **Coming Soon on the Google Play Store!** Stay tuned for the official release.

---

## 🛠️ Tech Stack

- **Kotlin** & **Coroutines / Flow**
- **Jetpack Compose** & **Material 3**
- **Dagger Hilt**
- **Google Play Billing Library**
- **Google Mobile Ads (AdMob)**
- **JUnit 5 & MockK** for Unit Testing
