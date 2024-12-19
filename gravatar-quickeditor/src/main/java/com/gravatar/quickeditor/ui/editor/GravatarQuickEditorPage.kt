package com.gravatar.quickeditor.ui.editor

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gravatar.quickeditor.QuickEditorContainer
import com.gravatar.quickeditor.ui.alttext.AltTextSection
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPicker
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPickerViewModel
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPickerViewModelFactory
import com.gravatar.quickeditor.ui.navigation.EditorNavDestinations
import com.gravatar.quickeditor.ui.navigation.QuickEditorPage
import com.gravatar.quickeditor.ui.oauth.OAuthPage
import com.gravatar.quickeditor.ui.oauth.OAuthParams
import com.gravatar.quickeditor.ui.splash.SplashPage

/**
 * Raw composable component for the Quick Editor.
 * This can be used to show the Quick Editor in Activity, Fragment or BottomSheet.
 *
 * @param gravatarQuickEditorParams The Quick Editor parameters.
 * @param oAuthParams The OAuth parameters.
 * @param onAvatarSelected The callback for the avatar update.
 *                       Can be invoked multiple times while the Quick Editor is open
 * @param onDismiss The callback for the dismiss action.
 *                  [GravatarQuickEditorError] will be non-null if the dismiss was caused by an error.
 */
@Composable
internal fun GravatarQuickEditorPage(
    gravatarQuickEditorParams: GravatarQuickEditorParams,
    oAuthParams: OAuthParams,
    onAvatarSelected: () -> Unit,
    onDismiss: (dismissReason: GravatarQuickEditorDismissReason) -> Unit = {},
) {
    val navController = rememberNavController()
    val editorViewModel: AvatarPickerViewModel = viewModel(
        factory = AvatarPickerViewModelFactory(
            gravatarQuickEditorParams = gravatarQuickEditorParams,
            handleExpiredSession = true,
        ),
    )

    NavHost(
        navController,
        startDestination = QuickEditorPage.SPLASH.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(route = QuickEditorPage.SPLASH.name) {
            SplashPage(email = gravatarQuickEditorParams.email) { isAuthorized ->
                if (isAuthorized) {
                    navController.navigate(QuickEditorPage.EDITOR.name)
                } else {
                    navController.navigate(QuickEditorPage.OAUTH.name)
                }
            }
        }
        composable(route = QuickEditorPage.OAUTH.name, enterTransition = { EnterTransition.None }) {
            OAuthPage(
                oAuthParams = oAuthParams,
                email = gravatarQuickEditorParams.email,
                onAuthError = { onDismiss(GravatarQuickEditorDismissReason.OauthFailed) },
                onAuthSuccess = { navController.navigate(QuickEditorPage.EDITOR.name) },
            )
        }
        navigation(
            route = QuickEditorPage.EDITOR.name,
            startDestination = EditorNavDestinations.AVATAR_SELECTION.name,
        ) {
            composable(route = EditorNavDestinations.AVATAR_SELECTION.name) {
                AvatarPicker(
                    onAvatarSelected = onAvatarSelected,
                    onSessionExpired = { navController.navigate(QuickEditorPage.OAUTH.name) },
                    onAltTextTapped = { avatarId ->
                        navController.navigate(
                            route = "${EditorNavDestinations.ALT_TEXT.name}/$avatarId",
                            navOptions = navOptions {
                                popUpTo(EditorNavDestinations.AVATAR_SELECTION.name) { saveState = true }
                            },
                        )
                    },
                    viewModel = editorViewModel,
                )
            }
            composable(
                route = "${EditorNavDestinations.ALT_TEXT.name}/{avatarId}",
                arguments = listOf(navArgument("avatarId") { type = NavType.StringType }),
            ) {
                val avatarId = requireNotNull(it.arguments?.getString("avatarId"))
                AltTextSection(
                    onBackPressed = { navController.popBackStack() },
                    avatarId = avatarId,
                    viewModel = editorViewModel,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

/**
 * Raw composable component for the Quick Editor.
 * This can be used to show the Quick Editor in Activity, Fragment or BottomSheet.
 *
 * @param gravatarQuickEditorParams The Quick Editor parameters.
 * @param authToken The authentication token.
 * @param onAvatarSelected The callback for the avatar update.
 *                       Can be invoked multiple times while the Quick Editor is open
 * @param onDismiss The callback for the dismiss action.
 *                  [GravatarQuickEditorError] will be non-null if the dismiss was caused by an error.
 */
@Composable
internal fun GravatarQuickEditorPage(
    gravatarQuickEditorParams: GravatarQuickEditorParams,
    authToken: String,
    onAvatarSelected: () -> Unit,
    onDismiss: (dismissReason: GravatarQuickEditorDismissReason) -> Unit = {},
) {
    val navController = rememberNavController()

    val editorViewModel: AvatarPickerViewModel = viewModel(
        factory = AvatarPickerViewModelFactory(gravatarQuickEditorParams, false),
    )

    DisposableEffect(authToken) {
        QuickEditorContainer.getInstance().useInMemoryTokenStorage()

        onDispose {
            QuickEditorContainer.getInstance().resetUseInMemoryTokenStorage()
        }
    }

    NavHost(
        navController,
        startDestination = QuickEditorPage.SPLASH.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(QuickEditorPage.SPLASH.name) {
            SplashPage(
                email = gravatarQuickEditorParams.email,
                token = authToken,
            ) {
                navController.navigate(QuickEditorPage.EDITOR.name)
            }
        }
        navigation(
            route = QuickEditorPage.EDITOR.name,
            startDestination = EditorNavDestinations.AVATAR_SELECTION.name,
        ) {
            composable(route = EditorNavDestinations.AVATAR_SELECTION.name) {
                AvatarPicker(
                    onAvatarSelected = onAvatarSelected,
                    onSessionExpired = { onDismiss(GravatarQuickEditorDismissReason.InvalidToken) },
                    onAltTextTapped = { avatarId ->
                        navController.navigate(
                            route = "${EditorNavDestinations.ALT_TEXT.name}/$avatarId",
                            navOptions = navOptions {
                                popUpTo(EditorNavDestinations.AVATAR_SELECTION.name) { saveState = true }
                            },
                        )
                    },
                    viewModel = editorViewModel,
                )
            }
            composable(
                route = "${EditorNavDestinations.ALT_TEXT.name}/{avatarId}",
                arguments = listOf(navArgument("avatarId") { type = NavType.StringType }),
            ) {
                val avatarId = requireNotNull(it.arguments?.getString("avatarId"))
                AltTextSection(
                    onBackPressed = { navController.popBackStack() },
                    avatarId = avatarId,
                    viewModel = editorViewModel,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
