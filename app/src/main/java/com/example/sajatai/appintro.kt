package com.example.sajatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import androidx.fragment.app.Fragment


class appintro : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.createInstance(
            title = "TITLE1",
            description = "DESCRIPTION1",
            backgroundColorRes = R.color.purple_200
        ))

        addSlide(AppIntroFragment.createInstance(
            title = "TITLE2",
            description = "DESCRIPTION2",
            backgroundColorRes = R.color.black,
        ))

        setTransformer(AppIntroPageTransformerType.Zoom)
        isColorTransitionsEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        //finish()
        val intent = Intent(this, loginpage_activity::class.java)
        startActivity(intent)
    }
}