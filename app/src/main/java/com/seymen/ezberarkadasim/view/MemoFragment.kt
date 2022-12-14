package com.seymen.ezberarkadasim.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.seymen.ezberarkadasim.Helpers.CustomEnums
import com.seymen.ezberarkadasim.Helpers.SQLiteHelper
import com.seymen.ezberarkadasim.R
import java.util.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.seymen.ezberarkadasim.databinding.FragmentMemoBinding
import com.seymen.ezberarkadasim.viewmodel.MemoViewModel

class MemoFragment : Fragment() {
    //Variables defined
    private lateinit var readList : ArrayList<String>
    private lateinit var sqliteHelper : SQLiteHelper
    private lateinit var control : String
    private var whchLang : Int? = null
    private lateinit var binding : FragmentMemoBinding
    private lateinit var mAdView : AdView
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var viewModel: MemoViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        apply {
            whchLang = arguments?.getInt("LANGKEY")
        }
        binding = FragmentMemoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Defined variables are initialized
        viewModel = ViewModelProvider(this)[MemoViewModel::class.java]
        activity?.let { viewModel.checkNetAndClose(requireContext(), it) }

        sqliteHelper = SQLiteHelper(requireContext())

        readList = sqliteHelper.getOnlyWord()
        init()

    }

    private fun init() {

        binding.apply {
            textViewToplamKelime.text= readList.size.toString()
            textViewSayac.text= readList.size.toString()

            MobileAds.initialize(requireContext())
            mAdView = adView
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)

            addInterstitialAd()
            ezbBaslaMCardView.setOnClickListener {
                //check permission and action
                if(ContextCompat.checkSelfPermission (requireContext(), Manifest.permission. RECORD_AUDIO ) != PackageManager. PERMISSION_GRANTED ){
                    checkPermission()
                } else {
                    if(textViewSayac.text.toString().toInt() > 0){
                        getSpeechInput()
                    }
                    else{
                        Toast.makeText(requireContext(),R.string.tst_thereIsNoMemoWord,Toast.LENGTH_LONG).show()
                    }
                }
            }
            geriBtnEzberle.setOnClickListener {
                activity?.onBackPressed()
            }
            btnSuggestion.setOnClickListener {
                openChromeTab(requireContext(),"https://docs.google.com/forms/d/e/1FAIpQLSfo0TyglibE_8t6ZVIF_ZF6L5eP2O3NcgrEpxv5YCjek5Yihg/viewform?usp=sf_link")
            }
            //Actions to be taken when clicking the reset button
            btnRepeat.setOnClickListener {
                if(textViewToplamKelime.text.toString().toInt() != textViewSayac.text.toString().toInt()){
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(R.string.alrt_repeatSure)
                    builder.setCancelable(true)
                    builder.setTitle(R.string.alrt_warn)
                    builder.setIcon(R.drawable.warning)
                    builder.setPositiveButton(R.string.alrt_yes){ dialog, _->
                        textViewSayac.text = readList.size.toString()
                        dialog.dismiss()
                        if (mInterstitialAd != null) {
                            mInterstitialAd?.show(requireActivity())
                            addInterstitialAd()
                        }
                    }
                    builder.setNegativeButton(R.string.alrt_no){ dialog, _->
                        dialog.dismiss()
                    }
                    val alert = builder.create()
                    alert.show()
                }else{
                    Toast.makeText(requireContext(), R.string.tst_notRepeat, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun openChromeTab(context: Context, urlD:String){
        val builder = CustomTabsIntent.Builder()
        builder.setStartAnimations(context, R.anim.slide_in, R.anim.slide_out);
        builder.setExitAnimations(context, R.anim.fade_in, R.anim.fade_out);

        builder.setToolbarColor(ContextCompat.getColor(context,R.color.primary))
        builder.addDefaultShareMenuItem()
        builder.setShowTitle(true)
        builder.enableUrlBarHiding()

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(urlD))
    }
    //add advertisement method
    private fun addInterstitialAd(){
        val adRequest2 = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(),"ca-app-pub-5522977588824109/4905883063", adRequest2, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }
    //check permission method
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET)
            ActivityCompat.requestPermissions(requireActivity(), permissions,0)
        }
    }
    //method of listening and validating the user
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        sqliteHelper = SQLiteHelper(requireContext())
        readList = sqliteHelper.getOnlyWord()
        //check list
        if (readList.isNotEmpty()){
            when (requestCode) {
                20 -> if (resultCode == Activity.RESULT_OK && resultCode == Activity.RESULT_OK)
                {
                    val result =data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    //format data
                    control = result!![0].lowercase().trim().replace("\\s+".toRegex(), " ")
                    val re = Regex("[^a-zA-Z0-9???????????????????????? \\p{InArabic}]")
                    control = re.replace(control, "")

                    if(readList.contains(control)){
                        val dialogBuilder = AlertDialog.Builder(requireContext())
                        val layoutView = layoutInflater.inflate(R.layout.dialog_success_layout, null)
                        val dialogButtonContinue = layoutView.findViewById(R.id.btnDialogArt??Devam) as Button // ikincisi olu??turuldu...
                        val dialogButtonStop = layoutView.findViewById(R.id.btnDialogArt??Dur) as Button
                        dialogBuilder.setView(layoutView)
                        val alertDialog = dialogBuilder.create()
                        alertDialog.setCanceledOnTouchOutside(false)
                        alertDialog.show()
                        var sayac = binding.textViewSayac.text.toString().toInt()

                        dialogButtonContinue.setOnClickListener {
                            var sayac2 = binding.textViewSayac.text.toString().toInt()
                            if (sayac2 > 1){
                                sayac2 -= 1
                                binding.textViewSayac.text = sayac2.toString()
                                getSpeechInput()
                            } else{
                                binding.textViewSayac.text = 0.toString()
                                Toast.makeText(requireContext(),R.string.tst_thereIsNoMemoWord,Toast.LENGTH_LONG).show()
                            }
                            alertDialog.dismiss()
                        }
                        dialogButtonStop.setOnClickListener {
                                sayac -= 1
                            binding.textViewSayac.text = sayac.toString()
                            alertDialog.dismiss()
                        }
                    } else{
                        val dialogBuilder = AlertDialog.Builder(requireContext())
                        val layoutView = layoutInflater.inflate(R.layout.dialog_failure_layout, null)
                        val dialogButton = layoutView.findViewById(R.id.btnDialogEksiDur) as Button
                        val dialogButton2 = layoutView.findViewById(R.id.btnDialogEksiDevam) as Button
                        dialogBuilder.setView(layoutView)
                        dialogBuilder.setTitle("'$control' ${getString(R.string.alrt_checkedAs)}")
                        val alertDialog = dialogBuilder.create()
                        alertDialog.setCanceledOnTouchOutside(false)

                        alertDialog.show()
                        dialogButton.setOnClickListener { alertDialog.dismiss() }
                        dialogButton2.setOnClickListener {
                            alertDialog.dismiss()
                            getSpeechInput()
                        }
                    }
                }
            }
        } else{
            Toast.makeText(requireContext(),R.string.tst_firstAdd,Toast.LENGTH_LONG).show()
        }
    }
    //set speech language
    private fun getSpeechInput() {
        if(!SpeechRecognizer.isRecognitionAvailable(requireContext())){
             Toast.makeText(requireContext(), R.string.tst_notsupp,Toast.LENGTH_SHORT).show()}
        else{
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        when(whchLang){
            CustomEnums.LANG_TR.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            CustomEnums.LANG_EN.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            CustomEnums.LANG_GE.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            CustomEnums.LANG_FR.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            CustomEnums.LANG_IT.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
            CustomEnums.LANG_ES.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            CustomEnums.LANG_AR.value-> intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
        }
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.promt_Memo))
        startActivityForResult(intent,20)}
    }

}