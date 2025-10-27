package com.example.loginandregistration.testing

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.R
import kotlinx.coroutines.launch

/**
 * Testing Activity for running comprehensive app tests This activity can be accessed from the
 * developer menu or debug builds
 */
class TestingActivity : AppCompatActivity() {

    private lateinit var testingHelper: AppTestingHelper
    private lateinit var btnRunTests: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResults: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        testingHelper = AppTestingHelper(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnRunTests = findViewById(R.id.btnRunTests)
        progressBar = findViewById(R.id.progressBar)
        tvResults = findViewById(R.id.tvResults)
        scrollView = findViewById(R.id.scrollView)
    }

    private fun setupListeners() {
        btnRunTests.setOnClickListener { runTests() }
    }

    private fun runTests() {
        btnRunTests.isEnabled = false
        progressBar.visibility = View.VISIBLE
        tvResults.text = "Running tests...\n\n"

        lifecycleScope.launch {
            try {
                val results = testingHelper.runAllTests()
                val report = testingHelper.generateReport()

                tvResults.text = report
                scrollView.post { scrollView.fullScroll(View.FOCUS_UP) }
            } catch (e: Exception) {
                tvResults.text = "Error running tests: ${e.message}\n\n${e.stackTraceToString()}"
            } finally {
                progressBar.visibility = View.GONE
                btnRunTests.isEnabled = true
            }
        }
    }
}
