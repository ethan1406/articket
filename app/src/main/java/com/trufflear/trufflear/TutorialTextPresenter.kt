package com.trufflear.trufflear

import android.content.res.Resources
import android.widget.TextView
import javax.inject.Inject

private const val DEFAULT_TUTORIAL_TEXT_ALPHA = 185
private const val ANIMATION_DURATION_MS = 1000L

class TutorialTextPresenter @Inject constructor(
    private val tutorialTextView: TextView,
    private val countDownTimer: TutorialCountDownTimer,
    private val resources: Resources
) {

    private val messageQueue = mutableListOf<String>()

    init {
        tutorialTextView.background.alpha = DEFAULT_TUTORIAL_TEXT_ALPHA
        tutorialTextView.alpha = 0.0f
    }

    private fun clearMessageQueue() = messageQueue.clear()

    fun showDefaultTutorialMessages() {
        clearMessageQueue()
        messageQueue.add(resources.getString(R.string.initial_tutorial_message))
        messageQueue.add(resources.getString(R.string.placement_tutorial_message))
        showMessage()
    }

    fun showMessage(text: String? = null, autoHide: Boolean = true) {
        displayMessage(true)

        if (text != null) {
            clearMessageQueue()
        }

        val textToShow = if (text == null && messageQueue.isNotEmpty()) {
            messageQueue[0]
        } else {
            text
        }

        tutorialTextView.text = textToShow

        countDownTimer.start {
            messageQueue.remove(textToShow)
            if (messageQueue.isEmpty()) {
                if (autoHide) displayMessage(false)
            } else {
                showMessage(autoHide = autoHide)
            }
        }
    }

    private fun displayMessage(shouldShowMessage: Boolean) {
        tutorialTextView.animate().alpha(
            if (shouldShowMessage) {
                1.0f
            } else {
                0.0f
            }
        ).duration = ANIMATION_DURATION_MS
    }

    interface TutorialCountDownTimer {
        fun start(onCountDownFinish: () -> Unit)
    }
}