/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified from the Official Jetpack Compose samples, provided by the Android Open Source Project
// Original source: https://github.com/android/compose-samples
// Changes:

package net.sharksystem.sharknetmessengerandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import net.sharksystem.sharknetmessengerandroid.databinding.ContentMainBinding
import kotlinx.coroutines.launch
import net.sharksystem.sharknetmessengerandroid.ui.MainViewModel
import net.sharksystem.sharknetmessengerandroid.ui.components.Drawer
import net.sharksystem.sharknetmessengerandroid.utils.Networking
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        // Example: Check SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isUsernameSet = prefs.getBoolean("peer_name_set", false)

        if (!isUsernameSet) {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
                    val drawerState = rememberDrawerState(initialValue = Closed)
                    val drawerOpen by viewModel.drawerShouldBeOpened
                        .collectAsStateWithLifecycle()

                    var selectedMenu by remember { mutableStateOf("composers") }
                    if (drawerOpen) {
                        // Open drawer and reset state in VM.
                        LaunchedEffect(Unit) {
                            // wrap in try-finally to handle interruption whiles opening drawer
                            try {
                                drawerState.open()
                            } finally {
                                viewModel.resetOpenDrawerAction()
                            }
                        }
                    }

                    val scope = rememberCoroutineScope()

                    Drawer(
                        drawerState = drawerState,
                        selectedMenu = selectedMenu,
                        onChatClicked = {
                            findNavController().popBackStack(R.id.nav_home, false)
                            scope.launch {
                                drawerState.close()
                            }
                            selectedMenu = it
                        },
                        onProfileClicked = {
                            val bundle = bundleOf("userId" to it)
                            findNavController().navigate(R.id.nav_profile, bundle)
                            scope.launch {
                                drawerState.close()
                            }
                            selectedMenu = it
                        }
                    ) {
                        AndroidViewBinding(ContentMainBinding::inflate)
                    }
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}
