package com.github.huymaster.kmessage.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.huymaster.kmessage.core.theme.KMessageTheme
import org.koin.core.component.KoinComponent

data class ActivitySavedState(
    val savedInstanceState: Bundle? = null,
    val persistentState: PersistableBundle? = null
)

val LocalActivitySavedState = staticCompositionLocalOf<ActivitySavedState> {
    error("ActivitySavedState not provided!")
}

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass not provided!")
}

abstract class BaseActivity : ComponentActivity(), KoinComponent {
    protected var savedState: ActivitySavedState = ActivitySavedState()
        private set

    protected open val floatingActionButtonPosition: FabPosition
        get() = FabPosition.End

    protected open val containerColor: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.background }

    protected open val contentColor: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.onBackground }

    protected open val contentWindowInsets: @Composable () -> WindowInsets
        get() = { ScaffoldDefaults.contentWindowInsets }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = ActivitySavedState(savedInstanceState)
        configure()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        savedState = ActivitySavedState(savedInstanceState, persistentState)
        configure()
    }

    @Composable
    protected open fun TopBar() {
        // No-op
    }

    @Composable
    protected open fun BottomBar() {
        // No-op
    }

    @Composable
    protected open fun SnackbarHost() {
        // No-op
    }

    @Composable
    protected open fun FloatingActionButton() {
        // No-op
    }

    @Composable
    protected abstract fun Content()


    private fun configure() {
        enableEdgeToEdge()
        setContent {
            @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
            val size = calculateWindowSizeClass(this)
            KMessageTheme {
                CompositionLocalProvider(
                    LocalActivitySavedState provides savedState,
                    LocalWindowSizeClass provides size
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = ::TopBar,
                        bottomBar = ::BottomBar,
                        snackbarHost = ::SnackbarHost,
                        floatingActionButton = ::FloatingActionButton,
                        floatingActionButtonPosition = floatingActionButtonPosition,
                        containerColor = containerColor(),
                        contentColor = contentColor(),
                        contentWindowInsets = contentWindowInsets()
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier.padding(innerPadding),
                            content = ::Content
                        )
                    }
                }
            }
        }
    }
}