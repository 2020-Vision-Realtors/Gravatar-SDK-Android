package com.gravatar.quickeditor.ui.alttext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gravatar.quickeditor.QuickEditorContainer
import com.gravatar.quickeditor.data.repository.AvatarRepository
import com.gravatar.services.GravatarResult
import com.gravatar.types.Email
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AltTextViewModel(
    private val email: String,
    private val avatarId: String,
    private val avatarRepository: AvatarRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            AltTextUiState(isSaveButtonEnabled = false, isUpdating = false),
        )
    val uiState: StateFlow<AltTextUiState> = _uiState.asStateFlow()
    private val _actions = Channel<AltTextAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    private lateinit var originalAltText: String

    init {
        getAvatarData()
    }

    private fun getAvatarData() {
        viewModelScope.launch {
            val avatar = avatarRepository.getAvatar(Email(email), avatarId)
            if (avatar != null) {
                originalAltText = avatar.altText

                _uiState.update {
                    it.copy(
                        avatarUrl = avatar.imageUrl,
                        altText = avatar.altText,
                    )
                }
            } else {
                _actions.send(AltTextAction.AvatarCantBeLoaded)
            }
        }
    }

    fun onEvent(event: AltTextEvent) {
        when (event) {
            is AltTextEvent.AvatarAltTextSaveTapped -> updateAltText()
            is AltTextEvent.AvatarAltTextChange -> updateUiStateWithNewAltText(event.newAltText)
        }
    }

    private fun updateAltText() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isSaveButtonEnabled = false,
                    isUpdating = true,
                )
            }
            when (avatarRepository.updateAvatar(Email(email), avatarId, altText = uiState.value.altText)) {
                is GravatarResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isSaveButtonEnabled = true,
                            isUpdating = false,
                        )
                    }
                    _actions.send(AltTextAction.AltTextUpdated)
                }

                is GravatarResult.Failure -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isSaveButtonEnabled = true,
                            isUpdating = false,
                        )
                    }
                    _actions.send(AltTextAction.AltTextUpdateFailed)
                }
            }
        }
    }

    private fun updateUiStateWithNewAltText(newAltText: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    altText = newAltText,
                    isSaveButtonEnabled = newAltText != originalAltText,
                )
            }
        }
    }
}

internal class AltTextViewModelFactory(
    private val email: String,
    private val avatarId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return AltTextViewModel(
            email = email,
            avatarId = avatarId,
            avatarRepository = QuickEditorContainer.getInstance().avatarRepository,
        ) as T
    }
}
