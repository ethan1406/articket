package com.example.arimage

import android.os.CountDownTimer

private const val DISPLAY_DURATION_MS = 6000L

class DefaultCountDownTimer: TutorialTextPresenter.TutorialCountDownTimer {
    override fun start(onCountDownFinish: () -> Unit) {
        object : CountDownTimer(DISPLAY_DURATION_MS, DISPLAY_DURATION_MS) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                onCountDownFinish()
            }
        }.start()
    }
}