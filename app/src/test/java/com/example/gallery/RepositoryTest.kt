package com.example.gallery

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.provider.MediaStore
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class RepositoryTest {

    private lateinit var imagesCursor: MatrixCursor
    private lateinit var contentResolver: ContentResolver
    private lateinit var ctx: Context
    private lateinit var repo: GalleryRepository

    @Before
    fun setUp() {
        val imageColumns = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
        )

        imagesCursor = MatrixCursor(imageColumns)

        contentResolver = mock {

            on {
                query(
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull()
                )
            } doReturn imagesCursor
        }

        ctx = mock {
            on {
                contentResolver
            } doReturn contentResolver
        }

        repo = GalleryRepository(ctx)
    }

    @InternalCoroutinesApi
    @Test
    fun `get folders with images`() {

        val imageData = arrayOf(
            "44",
            "download (1)",
            "191",
            "119",
            "Downloads",
            "content://media/external/images/media/44)"
        )

        val imageData2 = arrayOf(
            "46", "download", "192", "111", "Downloads", "content://media/external/images/media/46)"
        )

        val imageData3 = arrayOf(
            "47", "download123", "192", "111", "Camera", "content://media/external/images/media/47)"
        )

        val imageData4 = arrayOf(
            "48",
            "downloadasdasd",
            "192",
            "111",
            "Camera",
            "content://media/external/images/media/48)"
        )

        val imageData5 = arrayOf(
            "49", "downloadqqq", "192", "111", "Camera", "content://media/external/images/media/49)"
        )

        val result = listOf(
            Folder(
                "Downloads", listOf(
                    Image(
                        44,
                        "download (1)",
                        191,
                        119,
                        "content://media/external/images/media/44".toUri()
                    ),
                    Image(
                        46,
                        "download",
                        192,
                        111,
                        "content://media/external/images/media/46".toUri()
                    ),
                )
            ),
            Folder(
                "Camera", listOf(
                    Image(
                        47,
                        "download123",
                        192,
                        111,
                        "content://media/external/images/media/47".toUri()
                    ),
                    Image(
                        48,
                        "downloadasdasd",
                        192,
                        111,
                        "content://media/external/images/media/48".toUri()
                    ),
                    Image(
                        49,
                        "downloadqqq",
                        192,
                        111,
                        "content://media/external/images/media/49".toUri()
                    ),
                )

            )
        )
        imagesCursor.addRow(imageData)
        imagesCursor.addRow(imageData2)
        imagesCursor.addRow(imageData3)
        imagesCursor.addRow(imageData4)
        imagesCursor.addRow(imageData5)



        runBlocking {

            repo.getContent().collect { content ->

                assert(result.containsAll(content.folders))
            }
        }

    }
}