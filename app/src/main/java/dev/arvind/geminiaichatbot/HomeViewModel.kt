package dev.arvind.geminiaichatbot

import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import android.graphics.Bitmap

class HomeViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private var generativeModel: GenerativeModel

    init {

        val config = generationConfig {
            temperature = 0.70f // value lies between 0 to 1
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = BuildConfig.apiKey,
            generationConfig = config
        )
    }

    fun questioning(userInput: String, selectedImages: List<Bitmap>) {
        _uiState.value = HomeUiState.Loading
        val prompt = "Take a look at the images, and then answer the following questions: $userInput"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var output = ""
                generativeModel.generateContentStream(content).collect {
                    output += it.text
                    _uiState.value = HomeUiState.Success(output)
                }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error in Generating content")
            }
        }
    }
}


sealed interface HomeUiState {
    object Initial: HomeUiState
    object Loading: HomeUiState
    data class Success(
        val outputText: String
    ) : HomeUiState
    data class Error(val error: String): HomeUiState
}