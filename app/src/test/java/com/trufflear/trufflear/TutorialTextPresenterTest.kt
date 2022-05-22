package com.trufflear.trufflear

import android.content.res.Resources

import android.view.ViewPropertyAnimator
import android.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class TutorialTextPresenterTest {

    private val animator: ViewPropertyAnimator = mock()

    @Mock
    private lateinit var resources: Resources

    @Mock
    private lateinit var textView: TextView

    @Mock
    private lateinit var countDownTimer: TutorialTextPresenter.TutorialCountDownTimer


    private lateinit var presenter: TutorialTextPresenter

    @Before
    fun setUp() {
        whenever(animator.alpha(any())).thenReturn(animator)
        whenever(animator.setDuration(any())).thenReturn(animator)
        whenever(textView.animate()).thenReturn(animator)
        whenever(textView.background).thenReturn(mock())


        presenter = TutorialTextPresenter(textView, countDownTimer, resources)
    }

    @Test
    fun `showMessage display message`() {
        // ARRANGE
        val testMessage = "Testing"
        // ACT
        presenter.showMessage(testMessage, true)

        // ASSERT
        verify(textView, times(1)).animate()
        verify(textView, times(1)).text = testMessage

        verify(countDownTimer, times(1)).start(any())
    }

    @Test
    fun `showMessage display empty message`() {
        // ARRANGE

        // ACT
        presenter.showMessage(null)

        // ASSERT
        verify(textView, times(1)).animate()
        verify(textView, times(1)).text = null

        verify(countDownTimer, times(1)).start(any())
    }

    @Test
    fun `showMessage display initial message`() {
        // ARRANGE
        val tutorialMessage1 = "tutorial message 1"
        val tutorialMessage2 = "tutorial message 2"
        whenever(resources.getString(R.string.initial_tutorial_message)).thenReturn(tutorialMessage1)
        whenever(resources.getString(R.string.placement_tutorial_message)).thenReturn(tutorialMessage2)

        // ACT
        presenter.showDefaultTutorialMessages()

        // ASSERT
        verify(textView, times(1)).animate()
        verify(textView, times(1)).text = tutorialMessage1

        verify(countDownTimer, times(1)).start(any())
    }

}