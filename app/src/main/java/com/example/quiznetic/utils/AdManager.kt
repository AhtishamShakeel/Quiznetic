package com.example.quiznetic.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

class AdManager {
    companion object {
        private const val TAG = "AdManager"
        
        // AdMob test IDs
        private const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Unity Ads test IDs
        private const val UNITY_GAME_ID = "12345"
        private const val UNITY_BANNER_ID = "Banner_Android"
        private const val UNITY_INTERSTITIAL_ID = "Interstitial_Android"
        
        private const val TEST_MODE = true
        
        private var admobInterstitialAd: InterstitialAd? = null
        private var isUnityAdsInitialized = false
        
        /**
         * Initialize ad SDKs
         */
        fun initialize(context: Context) {
            // Initialize AdMob
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialization complete: $initializationStatus")
            }
            
            // Initialize Unity Ads
            UnityAds.initialize(context, UNITY_GAME_ID, TEST_MODE, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialization complete")
                    isUnityAdsInitialized = true
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError, message: String) {
                    Log.e(TAG, "Unity Ads initialization failed: $error - $message")
                }
            })
        }
        
        /**
         * Load and show a banner ad in the provided container
         */
        fun loadBannerAd(activity: Activity, adContainer: ViewGroup) {
            // First try AdMob - pass AdSize in constructor
            val adView = AdView(activity).apply {
                adUnitId = ADMOB_BANNER_ID
                setAdSize(AdSize.BANNER)
            }
            
            val adRequest = AdRequest.Builder().build()
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "AdMob banner loaded successfully")
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "AdMob banner failed to load: ${adError.message}. Trying Unity Ads...")
                    adContainer.removeAllViews()
                    
                    // Fallback to Unity Ads
                    if (isUnityAdsInitialized) {
                        val unityBanner = BannerView(activity, UNITY_BANNER_ID, UnityBannerSize(320, 50))
                        adContainer.addView(unityBanner)
                        unityBanner.load()
                    }
                }
            }
            
            adContainer.removeAllViews()
            adContainer.addView(adView)
            adView.loadAd(adRequest)
        }
        
        /**
         * Preload an interstitial ad
         */
        fun preloadInterstitialAd(context: Context) {
            // Try loading AdMob interstitial
            InterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "AdMob interstitial ad loaded")
                        admobInterstitialAd = interstitialAd
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "AdMob interstitial ad failed to load: ${loadAdError.message}")
                        admobInterstitialAd = null
                        // Unity Ads are loaded on-demand when showing
                    }
                })
        }
        
        /**
         * Show an interstitial ad
         */
        fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
            // First try AdMob
            if (admobInterstitialAd != null) {
                admobInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "AdMob interstitial ad dismissed")
                        admobInterstitialAd = null
                        // Preload next ad
                        preloadInterstitialAd(activity)
                        onAdClosed()
                    }
                    
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "AdMob interstitial ad failed to show: ${adError.message}")
                        admobInterstitialAd = null
                        // Fallback to Unity Ads
                        showUnityInterstitial(activity, onAdClosed)
                    }
                }
                admobInterstitialAd?.show(activity)
            } else {
                // Fallback to Unity Ads
                showUnityInterstitial(activity, onAdClosed)
            }
        }
        
        private fun showUnityInterstitial(activity: Activity, onAdClosed: () -> Unit) {
            if (isUnityAdsInitialized) {
                val loadListener = object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        UnityAds.show(activity, UNITY_INTERSTITIAL_ID, object : IUnityAdsShowListener {
                            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                                Log.e(TAG, "Unity interstitial ad failed to show: $error - $message")
                                onAdClosed()
                            }
                            
                            override fun onUnityAdsShowStart(placementId: String) {
                                Log.d(TAG, "Unity interstitial ad started")
                            }
                            
                            override fun onUnityAdsShowClick(placementId: String) {
                                Log.d(TAG, "Unity interstitial ad clicked")
                            }
                            
                            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                                Log.d(TAG, "Unity interstitial ad completed")
                                onAdClosed()
                            }
                        })
                    }
                    
                    override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                        Log.e(TAG, "Unity interstitial ad failed to load: $error - $message")
                        onAdClosed()
                    }
                }
                
                UnityAds.load(UNITY_INTERSTITIAL_ID, loadListener)
            } else {
                // No ads available
                onAdClosed()
            }
        }
    }
} 