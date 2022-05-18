package com.trufflear.trufflear

import android.content.res.Resources
import android.widget.TextView


private const val DEFAULT_TUTORIAL_TEXT_ALPHA = 185
private const val ANIMATION_DURATION_MS = 1000L

class TutorialTextPresenter(
    private val tutorialTextView: TextView,
    private val countDownTimer: TutorialCountDownTimer,
    resources: Resources
) {

    private val messageQueue = mutableListOf(
        resources.getString(R.string.initial_tutorial_message),
        resources.getString(R.string.placement_tutorial_message)
    )

    init {
        tutorialTextView.background.alpha = DEFAULT_TUTORIAL_TEXT_ALPHA
        tutorialTextView.alpha = 0.0f
        showInitialMessage()
    }


    private fun showInitialMessage() {
        showMessage()
    }

    private fun clearMessageQueue() = messageQueue.clear()


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

        if (autoHide) {
            countDownTimer.start {
                messageQueue.remove(textToShow)
                if (messageQueue.isEmpty()) {
                    displayMessage(false)
                } else {
                    showMessage()
                }
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