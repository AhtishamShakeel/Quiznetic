package com.example.quiznetic

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    
    // Use this URL to load from GitHub Pages
    private val privacyPolicyUrl = "https://sites.google.com/view/quiznetic-privacy-policy/home?authuser=1"
    
    // Alternative: hardcoded HTML content
    private val hardcodedPrivacyPolicy = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Privacy Policy - Quiznetic</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    line-height: 1.6;
                    margin: 0;
                    padding: 20px;
                    max-width: 800px;
                    margin: 0 auto;
                    color: #333;
                }
                h1 {
                    text-align: center;
                    padding-bottom: 10px;
                    border-bottom: 1px solid #eee;
                    color: #2c3e50;
                }
                h2 {
                    color: #3498db;
                    margin-top: 25px;
                }
                p {
                    margin-bottom: 15px;
                }
                ul {
                    margin-bottom: 15px;
                }
                .footer {
                    margin-top: 40px;
                    text-align: center;
                    font-size: 0.9em;
                    color: #7f8c8d;
                }
            </style>
        </head>
        <body>
            <h1>Privacy Policy for Quiznetic</h1>
            <p><strong>Last Updated:</strong> 19-March-2025</p>

            <h2>1. Introduction</h2>
            <p>Welcome to <strong>Quiznetic</strong>. We respect your privacy and are committed to protecting your personal information. This Privacy Policy explains how we collect, use, and share data when you use our mobile application.</p>
            <p>By using our app, you agree to the collection and use of information as described in this policy.</p>

            <h2>2. Information We Collect</h2>
            
            <h3>a) Information You Provide</h3>
            <p>We do not require you to create an account or provide personal information to use our app.</p>

            <h3>b) Automatically Collected Information</h3>
            <ul>
                <li><strong>Device Information:</strong> Device model, operating system, unique identifiers (such as Advertising ID).</li>
                <li><strong>Usage Data:</strong> Interactions with the app (such as quiz completion statistics).</li>
                <li><strong>Advertising Data:</strong> Our advertising partners (Google AdMob & Unity Ads) may collect data such as your IP address, device type, and ad interactions to serve relevant ads.</li>
            </ul>

            <h3>c) Information from External Sources (GitHub)</h3>
            <p>Our app loads quiz data from a <strong>publicly accessible GitHub repository</strong>. This data does <strong>not</strong> contain any personal information and is solely used for providing quiz content.</p>
            <p>We do <strong>not</strong> store, modify, or track user activity related to GitHub content.</p>

            <h2>3. How We Use Your Information</h2>
            <ul>
                <li>To provide and maintain our service</li>
                <li>To improve and optimize our app</li>
                <li>To display advertisements</li>
                <li>To comply with legal obligations</li>
            </ul>

            <h2>4. Advertising and Data Sharing</h2>
            <p>We use third-party services for advertising:</p>
            <ul>
                <li><strong>Google AdMob</strong>: <a href="https://policies.google.com/privacy" target="_blank">Privacy Policy</a></li>
                <li><strong>Unity Ads</strong>: <a href="https://unity.com/legal/privacy-policy" target="_blank">Privacy Policy</a></li>
            </ul>
            <p>These services may collect and use data for targeted advertising. You can opt-out through your device settings.</p>

            <h2>5. Your Choices and Rights</h2>
            <ul>
                <li>You can reset your advertising ID in your device settings.</li>
                <li>You can opt out of personalized ads through Google’s ad settings.</li>
                <li>If you are an EU or California resident, you have the right to request access to or deletion of your data.</li>
            </ul>

            <h2>6. Children's Privacy</h2>
            <p>This app is <strong>not targeted at children under 13</strong>. We do not knowingly collect personal data from children. If you believe we have unintentionally collected such data, please contact us.</p>

            <h2>7. Changes to This Privacy Policy</h2>
            <p>We may update this policy from time to time. Any changes will be posted on this page with an updated "Last Updated" date.</p>

            <h2>8. Contact Us</h2>
            <p>If you have any questions, please contact us at: <strong>Aunixbrand@gmail.com</strong></p>

            <div class="footer">
                &copy; 2025 Quiznetic. All rights reserved.
            </div>
        </body>
        </html>
    """.trimIndent()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"
        
        val webView = findViewById<WebView>(R.id.privacyPolicyWebView)
        webView.webViewClient = WebViewClient() // Handle links within the WebView
        
        // Enable JavaScript for better formatting
        webView.settings.javaScriptEnabled = true
        
        // OPTION 1: Load from URL (recommended)
        webView.loadUrl(privacyPolicyUrl)
        
        // OPTION 2: Load from hardcoded HTML string 
        // Uncomment this and comment the loadUrl above if you prefer hardcoded approach
        // webView.loadData(hardcodedPrivacyPolicy, "text/html", "UTF-8")
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() 
        return true
    }
} 