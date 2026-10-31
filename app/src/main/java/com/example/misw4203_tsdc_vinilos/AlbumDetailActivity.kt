package com.example.misw4203_tsdc_vinilos

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail.AlbumDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** Activity que presenta el detalle de un álbum (HU02). */
class AlbumDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val albumId = intent.getIntExtra(EXTRA_ALBUM_ID, DEFAULT_ALBUM_ID)

        setContent {
            MISW4203TSDCVinilosTheme {
                AlbumDetailScreen(
                    albumId = albumId,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ALBUM_ID = "extra_album_id"
        private const val DEFAULT_ALBUM_ID = 100

        fun newIntent(context: Context, albumId: Int): Intent =
            Intent(context, AlbumDetailActivity::class.java).apply {
                putExtra(EXTRA_ALBUM_ID, albumId)
            }
    }
}
