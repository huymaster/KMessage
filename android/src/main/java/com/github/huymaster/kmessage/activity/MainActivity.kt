package com.github.huymaster.kmessage.activity

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.github.huymaster.kmessage.core.data.repository.AuthRepository
import com.github.huymaster.kmessage.core.data.repository.AuthState
import com.github.huymaster.kmessage.core.utils.SERVER_HOST
import com.github.huymaster.server.api.constants.Endpoints
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class MainActivity : BaseActivity() {
    @Composable
    override fun Content() {
    }
}