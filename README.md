# StyleSense: Autonomous On-Device Biometric Colorimetry and Explainable Wardrobe Personalization

**BSc (Hons) Computer Science — Final Year Project (6COSC023C)**  
*School of Computer Science and Engineering, University of Westminster (UK)*  
*In collaboration with Informatics Institute of Technology (IIT), Sri Lanka*

* **Candidate Name:** Hasara Poornima
* **Student ID (IIT / UoW):** 20241431 / w2153620
* **Project Supervisor:** Ms. Methma Rathnayake

---

## 📌 Project Overview

StyleSense is an edge-native, privacy-preserving mobile computer vision application for Android that provides real-time biophysical skin tone analysis, 12-season personal color palette classification, and combinatorial capsule wardrobe personalization entirely on the user's device.

### Core Scientific & Engineering Features
* **Illuminant Normalization (CIE D65):** Planckian chromatic adaptation coupled with bilateral filtering to decouple domestic lighting variations (2700K–6500K) to achieve $\text{Mean } \Delta E_{00} < 2.5$.
* **Biophysical Multi-ROI Colorimetry:** Closed-form spectrophotometric calculation of Individual Typology Angle ($\text{ITA}^\circ$), Melanin Index ($\text{MI}$), and Erythema Index ($\text{EI}$) isolating cheeks, forehead, and infraorbital periorbital tissue.
* **12-Season Personal Color Flow Theory:** Continuous multi-attribute classification across hue, value, and chroma coordinates.
* **Combinatorial Greedy Submodular Capsule Wardrobe Engine (Layer 2):** Curates a personalized 10-piece capsule wardrobe from a curated $N=60$ catalogue maximizing:
  $$F(S) = 0.45 \cdot H_{\text{Matsuda}}(S) + 0.35 \cdot C_{\text{Silhouette}}(S) + 0.20 \cdot V_{\text{Versatility}}(S)$$
  with pre-computed relational compatibility lookup matrices (Appendix O Table O.1).
* **Two-Garment Mix & Match Pairing:** Real-time mutual Matsuda color harmony analysis between blouse/top and skirt/trousers with curated jewelry metal recommendations.
* **Zero Biometric Persistence (UK GDPR Article 9):** Camera preview frames and pixel arrays are processed strictly in volatile RAM buffers, zero-filled and recycled immediately following feature extraction. Zero image or biometric data are persisted to disk or transmitted over networks.

---

## 🚀 Getting Started

### Prerequisites
* Android Studio Iguana / Ladybug or newer
* Android SDK (API 31+ / Android 12+)
* Java / JDK 17+ or OpenJDK 25 (bundled in Android Studio JBR)

### Build & Run
1. Open **Android Studio**.
2. Select **Open** and select this directory (`stylesense`).
3. Allow Gradle to sync dependencies.
4. Select your connected Android device or Android Virtual Device (e.g., Pixel 7 / Redmi Note 10 with Android 12).
5. Click **Run (▶)** or press `Shift + F10`.

Alternatively, build the debug APK via terminal:
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
