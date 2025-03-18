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
import com.unity3d.services.banners.BannerErrorInfo

class AdManager {
    companion object {
        private const val TAG = "AdManager"
        
        // AdMob test IDs
        private const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Unity Ads test IDs - Using proper Unity test game ID
        private const val UNITY_GAME_ID = "5816684" // Unity test Game ID
        private const val UNITY_BANNER_ID = "Banner_Android"
        private const val UNITY_INTERSTITIAL_ID = "Interstitial_Android"
        
        private const val TEST_MODE = false
        
        private var admobInterstitialAd: InterstitialAd? = null
        private var isUnityAdsInitialized = false
        
        /**
         * Initialize ad SDKs
         */
        fun initialize(context: Context) {
            // Initialize Unity Ads first
            Log.d(TAG, "Initializing Unity Ads with game ID: $UNITY_GAME_ID")
            UnityAds.initialize(context, UNITY_GAME_ID, TEST_MODE, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialization complete")
                    isUnityAdsInitialized = true
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError, message: String) {
                    Log.e(TAG, "Unity Ads initialization failed: $error - $message")
                }
            })
            
            // Initialize AdMob
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialization complete: $initializationStatus")
            }
        }
        
        /**
         * Load and show a banner ad in the provided container
         */
        fun loadBannerAd(activity: Activity, adContainer: ViewGroup) {
            // First try Unity Ads
            if (isUnityAdsInitialized) {
                Log.d(TAG, "Loading Unity banner ad")
                val unityBanner = BannerView(activity, UNITY_BANNER_ID, UnityBannerSize(320, 50))
                adContainer.removeAllViews()
                adContainer.addView(unityBanner)
                
                // Set a
                // listener to detect failure and load AdMob as fallback
                unityBanner.setListener(object : BannerView.IListener {
                    override fun onBannerLoaded(bannerAdView: BannerView) {
                        Log.d(TAG, "Unity banner loaded successfully")
                    }
                    
                    override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
                        Log.d(TAG, "Unity banner failed to load: ${errorInfo.errorMessage}. Trying AdMob...")
                        loadAdMobBanner(activity, adContainer)
                    }
                    
                    override fun onBannerClick(bannerAdView: BannerView) {
                        Log.d(TAG, "Unity banner clicked")
                    }
                    
                    override fun onBannerLeftApplication(bannerAdView: BannerView) {
                        Log.d(TAG, "Unity banner left application")
                    }
                    
                    override fun onBannerShown(bannerAdView: BannerView) {
                        Log.d(TAG, "Unity banner shown")
                    }
                })
                
                unityBanner.load()
            } else {
                Log.d(TAG, "Unity Ads not initialized, falling back to AdMob")
                // Fall back to AdMob if Unity isn't initialized
                loadAdMobBanner(activity, adContainer)
            }
        }
        
        private fun loadAdMobBanner(activity: Activity, adContainer: ViewGroup) {
            // Load AdMob as fallback
            Log.d(TAG, "Loading AdMob banner ad")
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
                    Log.d(TAG, "AdMob banner also failed to load: ${adError.message}")
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
            // Unity Ads are loaded on-demand when showing
            Log.d(TAG, "Preloading AdMob interstitial ad")
            
            // Also try loading AdMob interstitial as fallback
            InterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "AdMob interstitial ad loaded")
                        admobInterstitialAd = interstitialAd
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "AdMob interstitial ad failed to load: ${loadAdError.message}")
                        admobInterstitialAd = null
                    }
                })
        }
        
        /**
         * Show an interstitial ad
         */
        fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
            // First try Unity Ads
            if (isUnityAdsInitialized) {
                Log.d(TAG, "Showing Unity interstitial ad")
                showUnityInterstitial(activity) { success ->
                    if (success) {
                        onAdClosed()
                    } else {
                        // Fallback to AdMob if Unity fails
                        Log.d(TAG, "Unity interstitial failed, falling back to AdMob")
                        showAdMobInterstitial(activity, onAdClosed)
                    }
                }
            } else {
                Log.d(TAG, "Unity Ads not initialized, falling back to AdMob")
                // Fallback to AdMob if Unity isn't initialized
                showAdMobInterstitial(activity, onAdClosed)
            }
        }
        
        private fun showUnityInterstitial(activity: Activity, onComplete: (Boolean) -> Unit) {
            val loadListener = object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    Log.d(TAG, "Unity interstitial ad loaded, showing now")
                    UnityAds.show(activity, UNITY_INTERSTITIAL_ID, object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                            Log.e(TAG, "Unity interstitial ad failed to show: $error - $message")
                            onComplete(false)
                        }
                        
                        override fun onUnityAdsShowStart(placementId: String) {
                            Log.d(TAG, "Unity interstitial ad started")
                        }
                        
                        override fun onUnityAdsShowClick(placementId: String) {
                            Log.d(TAG, "Unity interstitial ad clicked")
                        }
                        
                        override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                            Log.d(TAG, "Unity interstitial ad completed with state: $state")
                            onComplete(true)
                        }
                    })
                }
                
                override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                    Log.e(TAG, "Unity interstitial ad failed to load: $error - $message")
                    onComplete(false)
                }
            }
            
            Log.d(TAG, "Loading Unity interstitial ad")
            UnityAds.load(UNITY_INTERSTITIAL_ID, loadListener)
        }
        
        private fun showAdMobInterstitial(activity: Activity, onAdClosed: () -> Unit) {
            if (admobInterstitialAd != null) {
                Log.d(TAG, "Showing AdMob interstitial ad")
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
                        onAdClosed()
                    }
                }
                admobInterstitialAd?.show(activity)
            } else {
                Log.d(TAG, "No AdMob interstitial ad available")
                // No ads available
                onAdClosed()
            }
        }
    }
} 