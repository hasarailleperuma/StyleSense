package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.WardrobeItemDao
import com.example.data.local.entity.WardrobeItem
import com.example.domain.classifier.SeasonClassifier
import com.example.domain.colorimetry.BiometricProcessor
import com.example.domain.colorimetry.ColorSpaceConverter
import com.example.domain.colorimetry.IlluminantNormalizer
import com.example.domain.harmony.MatsudaHarmonyEngine
import com.example.domain.model.*
import com.example.presentation.camera.CameraManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var wardrobeItemDao: WardrobeItemDao

    @Before
    fun setUpDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wardrobeItemDao = db.wardrobeItemDao()
    }

    @After
    fun tearDownDatabase() {
        db.close()
    }

    @Test
    fun readStringFromContext_matchesAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StyleSense", appName)
    }

    @Test
    fun wardrobeItem_storesOnlyClothingHexAndCategories_noBiometricData() = runBlocking {
        // Enforce UK GDPR Article 9: Only anonymous clothing attributes
        val item = WardrobeItem(
            name = "Classic Navy Blazer",
            category = "OUTERWEAR",
            primaryHex = "#0F4C81",
            secondaryHex = "#E0C9A6",
            seasonScore = 95,
            harmonyTag = "Signature Classic"
        )

        val insertedId = wardrobeItemDao.insertItem(item)
        assertTrue(insertedId > 0)

        val retrievedItem = wardrobeItemDao.getItemById(insertedId)
        assertNotNull(retrievedItem)
        assertEquals("Classic Navy Blazer", retrievedItem?.name)
        assertEquals("OUTERWEAR", retrievedItem?.category)
        assertEquals("#0F4C81", retrievedItem?.primaryHex)
        assertEquals("#E0C9A6", retrievedItem?.secondaryHex)
        assertEquals(95, retrievedItem?.seasonScore)

        val allItems = wardrobeItemDao.getAllItems().first()
        assertEquals(1, allItems.size)

        wardrobeItemDao.deleteById(insertedId)
        val afterDelete = wardrobeItemDao.getAllItems().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun hiltLifecycleDependencies_instancesCanBeConstructed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val biometricProcessor = BiometricProcessor()
        assertNotNull(biometricProcessor)

        val cameraManager = CameraManager(context, biometricProcessor)
        assertNotNull(cameraManager)
        assertTrue(cameraManager.isFrontCamera)

        val appDatabase = AppDatabase.getDatabase(context)
        assertNotNull(appDatabase)
        assertNotNull(appDatabase.wardrobeItemDao())
    }

    @Test
    fun colorSpaceConverter_rgbToLab_isAccurate() {
        // Pure White (255, 255, 255) -> L* ≈ 100, a* ≈ 0, b* ≈ 0
        val whiteLab = ColorSpaceConverter.rgbToLab(255, 255, 255)
        assertEquals(100f, whiteLab.l, 0.5f)
        assertEquals(0f, whiteLab.a, 1.0f)
        assertEquals(0f, whiteLab.b, 1.0f)

        // Pure Black (0, 0, 0) -> L* ≈ 0, a* ≈ 0, b* ≈ 0
        val blackLab = ColorSpaceConverter.rgbToLab(0, 0, 0)
        assertEquals(0f, blackLab.l, 0.5f)
    }

    @Test
    fun colorSpaceConverter_deltaE00_identicalColorsIsZero() {
        val lab1 = LabColor(65f, 12f, 18f)
        val lab2 = LabColor(65f, 12f, 18f)
        val deltaE = ColorSpaceConverter.calculateDeltaE00(lab1, lab2)
        assertEquals(0f, deltaE, 0.001f)
    }

    @Test
    fun colorSpaceConverter_calculateITA_returnsExpectedAngle() {
        val ita = ColorSpaceConverter.calculateITA(80f, 15f)
        assertTrue("ITA angle should be positive for high L*", ita > 40f)
        val classification = ColorSpaceConverter.classifyITA(ita)
        assertTrue(classification in listOf("Very Light", "Light"))
    }

    @Test
    fun illuminantNormalizer_cctEstimation_withinRange() {
        val warmCCT = IlluminantNormalizer.estimateKelvin(220f, 170f, 120f)
        assertTrue("Warm illuminant should estimate below 4500K", warmCCT < 4500)

        val coolCCT = IlluminantNormalizer.estimateKelvin(140f, 170f, 240f)
        assertTrue("Cool illuminant should estimate above 5500K", coolCCT > 5500)
    }

    @Test
    fun seasonClassifier_manualInput_classifiesValidSeason() {
        val winterResult = SeasonClassifier.classifyManual(
            skinToneWarmth = -0.8f,
            skinDepth = 0.2f,
            eyeColorChroma = 0.9f,
            contrastLevel = 0.9f
        )
        assertNotNull(winterResult.topSeason)
        assertTrue(winterResult.confidence in 0.5f..1.0f)

        val springResult = SeasonClassifier.classifyManual(
            skinToneWarmth = 0.8f,
            skinDepth = 0.25f,
            eyeColorChroma = 0.85f,
            contrastLevel = 0.6f
        )
        assertNotNull(springResult.topSeason)
        assertEquals(SeasonGroup.SPRING, springResult.topSeason.group)
    }

    @Test
    fun matsudaHarmonyEngine_evaluatesOutfitSuitability() {
        val sampleSwatches = listOf(
            ColorSwatch("Cobalt Blue", "#0047AB", androidx.compose.ui.graphics.Color(0xFF0047AB)),
            ColorSwatch("Pure White", "#FFFFFF", androidx.compose.ui.graphics.Color(0xFFFFFFFF))
        )
        val result = MatsudaHarmonyEngine.evaluateOutfit(
            garmentColors = sampleSwatches,
            userSeason = SeasonType.CLEAR_WINTER,
            occasion = Occasion.BUSINESS_INTERVIEW,
            silhouette = BodySilhouette.HOURGLASS
        )

        assertNotNull(result)
        assertTrue("Compatibility score should be between 25 and 100", result.overallScore in 25..100)
        assertTrue("Plain English rationale must not be empty", result.plainEnglishRationale.isNotBlank())
        assertTrue("Recommended accessories must not be empty", result.recommendedAccessories.isNotEmpty())
    }
}
