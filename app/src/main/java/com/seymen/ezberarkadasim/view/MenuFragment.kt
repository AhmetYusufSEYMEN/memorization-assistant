package com.seymen.ezberarkadasim.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.seymen.ezberarkadasim.Helpers.CustomEnums
import com.seymen.ezberarkadasim.Helpers.SQLiteHelper
import com.seymen.ezberarkadasim.R
import com.seymen.ezberarkadasim.databinding.FragmentMenuBinding
import com.seymen.ezberarkadasim.viewmodel.MenuViewModel
import kotlinx.android.synthetic.main.bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog.view.*
import java.util.*

class MenuFragment : Fragment(),TextToSpeech.OnInitListener{
    //Variables defined
    private lateinit var binding : FragmentMenuBinding
    private val arrayLang = arrayOf("  Türkçe  ","  English  ", "  Deutsch  ", " Français ", " Italiano ", " Español ", " Arabian ")
    private var whichLang : Int? = null
    private val bundle = Bundle()
    private lateinit var viewModel: MenuViewModel
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var tts : TextToSpeech
    private lateinit var sqliteHelper : SQLiteHelper
    private lateinit var mAdView : AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       binding = FragmentMenuBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Defined variables are initialized
        viewModel = ViewModelProvider(this)[MenuViewModel::class.java]
        activity?.let { viewModel.checkNetAndClose(requireContext(), it) }

        tts = TextToSpeech(requireContext(), this)

        sqliteHelper = SQLiteHelper(requireContext())

        sharedPreferences = requireContext().getSharedPreferences("com.seymen.ezberarkadasim", Context.MODE_PRIVATE)

        init()

    }

    private fun init() {

        binding.apply {
            mAdView = binding.adView
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)

            //set last clicked spinner row
            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_list_design,arrayLang)
            binding.spinnerLang.adapter = arrayAdapter
            val spinrow = sharedPreferences.getInt("spinnerrow",0)
            binding.spinnerLang.setSelection(spinrow)
            binding.spinnerLang.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{

                //if spinner item is changed
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    whichLang = position
                    bundle.putInt("LANGKEY" , whichLang!!)
                    sharedPreferences.edit().putInt("spinnerrow",position).apply()
                    if (position != spinrow){
                        Toast.makeText(requireContext(), R.string.tst_plsRestart, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?){}
            }
            val slide : Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.cwanimation)
            binding.cwMenu.startAnimation(slide)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            binding.ekleMCardView.setOnClickListener {
                Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_ekleKaydetFragment,bundle)
            }
            binding.dinleMCardView.setOnClickListener {
                showBottomSheetDialog()
            }
            binding.ezberleMCardView.setOnClickListener {
                Navigation.findNavController(it).navigate(R.id.action_menuFragment_to_okutEzberleFragment,bundle)
            }
        }
    }

    //method of show bottom sheet dialog
    private fun showBottomSheetDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_dialog)

        val dinleLayout : LinearLayout = dialog.dinleLayout
        dinleLayout.btnDinle.setOnClickListener {
            speakOut()
        }
        val durdurLayout : LinearLayout = dialog.durdurLayout
        durdurLayout.btnDinlemeyiDurdur.setOnClickListener {
            tts.stop()
        }
        val infoLayout : LinearLayout = dialog.infoLayout
        infoLayout.btnInfo.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.alrt_checklisten)
            builder.setCancelable(true)
            builder.setTitle(R.string.alrt_warn)
            builder.setIcon(R.drawable.warning)
            builder.setPositiveButton(R.string.alrt_ok){ dialog, _->
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }

        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes?.windowAnimations= R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()
        dialog.setOnDismissListener {
            tts.stop()
        }
    }
    //method of listening to words
    private fun speakOut() {
        val okumaList : ArrayList<String> = sqliteHelper.getOnlyWord()
        if (tts.isSpeaking){
            Toast.makeText(requireContext(),R.string.tst_stillListn,Toast.LENGTH_LONG).show()
        }
        else {
            if(okumaList.isEmpty()){
                Toast.makeText(requireContext(),R.string.tst_emptytoRead, Toast.LENGTH_LONG).show()
            } else {
                for (pos in okumaList) {
                    tts.speak(pos, TextToSpeech.QUEUE_ADD, null, "")
                }
                okumaList.clear()
            }
        }
    }
    //selection of listening language with data from spinner
    override fun onInit(status: Int) {
         if (status == TextToSpeech.SUCCESS) {
           when (whichLang) {
               CustomEnums.LANG_TR.value -> tts.language =  Locale("tr", "TR")
               CustomEnums.LANG_EN.value -> tts.language =  Locale("en", "US")
               CustomEnums.LANG_GE.value -> tts.language =  Locale("de", "DE")
               CustomEnums.LANG_FR.value -> tts.language =  Locale("fr", "FR")
               CustomEnums.LANG_IT.value -> tts.language =  Locale("it", "IT")
               CustomEnums.LANG_ES.value -> tts.language =  Locale("es", "ES")
               CustomEnums.LANG_AR.value -> tts.language =  Locale("ar", "SA")
           }
       } else if (status == TextToSpeech.ERROR) {
           Toast.makeText(requireContext(), R.string.tst_readFailed, Toast.LENGTH_LONG).show()
       }
    }



}