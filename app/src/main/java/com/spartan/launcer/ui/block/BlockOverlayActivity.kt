package com.spartan.launcer.ui.block

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spartan.launcer.R
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.domain.blocking.BlockReason
import kotlinx.coroutines.delay

/**
 * Full-screen cover shown over a blocked app. Consumes back presses and gives
 * the user two honest exits: a short grace period ("use anyway") or going home.
 */
class BlockOverlayActivity : ComponentActivity() {

    private val container get() = (application as SpartanLauncherApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val label = intent.getStringExtra(EXTRA_LABEL) ?: packageName
        val reason = intent.getStringExtra(EXTRA_REASON)
            ?.let { name -> runCatching { BlockReason.valueOf(name) }.getOrNull() }
            ?: BlockReason.MANUAL
        container.appBlocker.onOverlayShown()

        setContent {
            var countdown by remember { mutableIntStateOf(COUNTDOWN_SECONDS) }
            BackHandler { }
            LaunchedEffect(Unit) {
                while (countdown > 0) {
                    delay(1_000)
                    countdown--
                }
                // Countdown ending is an implicit "use anyway".
                container.appBlocker.grantTemporaryAccess(packageName, GRACE_MINUTES)
                finish()
            }
            BlockOverlayContent(
                label = label,
                reason = reason,
                countdown = countdown,
                onUseAnyway = {
                    container.appBlocker.grantTemporaryAccess(packageName, GRACE_MINUTES)
                    finish()
                },
                onGoHome = {
                    container.appBlocker.onOverlayClosed()
                    startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        container.appBlocker.onOverlayClosed()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"
        private const val EXTRA_LABEL = "label"
        private const val EXTRA_REASON = "reason"
        private const val COUNTDOWN_SECONDS = 30
        private const val GRACE_MINUTES = 2

        fun newIntent(
            context: Context,
            packageName: String,
            label: String,
            reason: BlockReason
        ): Intent = Intent(context, BlockOverlayActivity::class.java)
            .putExtra(EXTRA_PACKAGE, packageName)
            .putExtra(EXTRA_LABEL, label)
            .putExtra(EXTRA_REASON, reason.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}

@androidx.compose.runtime.Composable
private fun BlockOverlayContent(
    label: String,
    reason: BlockReason,
    countdown: Int,
    onUseAnyway: () -> Unit,
    onGoHome: () -> Unit
) {
    val surface = Color(0xFF100F0F)
    val onSurface = Color(0xFFF1EFEA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.block_overlay_title),
            style = MaterialTheme.typography.titleLarge,
            color = onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.displaySmall,
            color = onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(reasonResId(reason)),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFB9B6AF),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onUseAnyway,
            colors = ButtonDefaults.buttonColors(
                containerColor = onSurface,
                contentColor = surface
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(stringResource(R.string.block_overlay_use_anyway))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.block_overlay_countdown, countdown),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8C8981)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoHome,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(stringResource(R.string.block_overlay_home))
        }
    }
}

private fun reasonResId(reason: BlockReason): Int = when (reason) {
    BlockReason.MANUAL -> R.string.block_overlay_reason_manual
    BlockReason.LIMIT -> R.string.block_overlay_reason_limit
    BlockReason.SCHEDULE -> R.string.block_overlay_reason_schedule
    BlockReason.FOCUS -> R.string.block_overlay_reason_focus
}