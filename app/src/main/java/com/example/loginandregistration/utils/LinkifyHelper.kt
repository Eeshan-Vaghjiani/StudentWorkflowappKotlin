package com.example.loginandregistration.utils

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R
import java.util.regex.Pattern

/** Helper class for detecting and making URLs clickable in TextViews */
object LinkifyHelper {

    // Regex pattern to detect various URL formats
    private val URL_PATTERN =
            Pattern.compile(
                    "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)" +
                            "(([\\w\\-]+\\.)+([\\w\\-]+)?)" +
                            "(/([\\w\\-._~:/?#\\[\\]@!$&'()*+,;=.]+)?)?",
                    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
            )

    /** Data class representing a detected link in text */
    data class LinkSpan(val url: String, val start: Int, val end: Int)

    /**
     * Detect all URLs in the given text
     * @param text The text to search for URLs
     * @return List of LinkSpan objects representing detected URLs
     */
    fun detectLinks(text: String): List<LinkSpan> {
        val links = mutableListOf<LinkSpan>()
        val matcher = URL_PATTERN.matcher(text)

        while (matcher.find()) {
            val url = matcher.group().trim()
            val start = matcher.start()
            val end = matcher.end()

            // Ensure we have a valid URL
            if (url.isNotEmpty()) {
                links.add(LinkSpan(url, start, end))
            }
        }

        return links
    }

    /**
     * Make URLs in the TextView clickable
     * @param textView The TextView to apply clickable links to
     * @param text The text containing URLs
     * @param linkColor Optional custom color for links (defaults to blue)
     */
    fun makeLinksClickable(textView: TextView, text: String, linkColor: Int? = null) {
        val links = detectLinks(text)

        if (links.isEmpty()) {
            // No links found, just set the text normally
            textView.text = text
            return
        }

        val spannableString = SpannableString(text)
        val color = linkColor ?: ContextCompat.getColor(textView.context, R.color.primary_color)

        links.forEach { link ->
            val clickableSpan =
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            openUrl(widget, link.url)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = color
                            ds.isUnderlineText = true
                        }
                    }

            spannableString.setSpan(
                    clickableSpan,
                    link.start,
                    link.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        // Remove the default highlight color when clicking links
        textView.highlightColor = Color.TRANSPARENT
    }

    /**
     * Open a URL in the device's default browser
     * @param view The view context
     * @param url The URL to open
     */
    private fun openUrl(view: View, url: String) {
        try {
            // Ensure URL has a proper scheme
            val formattedUrl =
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        "https://$url"
                    } else {
                        url
                    }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
            view.context.startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no browser is available or URL is invalid
            e.printStackTrace()
        }
    }

    /**
     * Check if text contains any URLs
     * @param text The text to check
     * @return true if text contains at least one URL
     */
    fun containsUrl(text: String): Boolean {
        return URL_PATTERN.matcher(text).find()
    }

    /**
     * Extract all URLs from text as plain strings
     * @param text The text to extract URLs from
     * @return List of URL strings
     */
    fun extractUrls(text: String): List<String> {
        return detectLinks(text).map { it.url }
    }
}
