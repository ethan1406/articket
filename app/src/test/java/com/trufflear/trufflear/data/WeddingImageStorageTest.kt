package com.trufflear.trufflear.data




class WeddingImageStorageTest {

//    @ExperimentalCoroutinesApi
//    private val testDispatcher = StandardTestDispatcher()
//
//
//    private lateinit var imageDatabase: WeddingImageStorage
//
//    @ExperimentalCoroutinesApi
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//    }
//
//    @ExperimentalCoroutinesApi
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @ExperimentalCoroutinesApi
//    @Test
//    fun `getConfigWithImageDatabase should return config with database`() = runTest {
//        // ARRANGE
//        imageDatabase = WeddingImageStorage(testDispatcher)
//        val config: Config = mock()
//        val database: ImageDatabase = mock {
//            on { initiateDatabase() } doReturn Unit
//        }
//
//        val weddingImages = listOf(
//            AugmentedImageBitmapModel(
//                videoRes = 3,
//                imageWidthMeters = 0.03f,
//                image = mock()
//            )
//        )
//
//        // ACT
//        val testConfig = imageDatabase.getConfigWithImageDatabase(config, database, weddingImages)
//
//        // ASSERT
//        assertEquals(testConfig.augmentedImageDatabase.numImages, 1)
//   }
}