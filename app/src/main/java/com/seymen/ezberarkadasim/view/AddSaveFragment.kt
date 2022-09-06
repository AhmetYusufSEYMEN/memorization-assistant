package com.seymen.ezberarkadasim.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.seymen.ezberarkadasim.Adapters.RecyclerViewAdapter
import com.seymen.ezberarkadasim.Helpers.CustomEnums
import com.seymen.ezberarkadasim.Helpers.SQLiteHelper
import com.seymen.ezberarkadasim.R
import com.seymen.ezberarkadasim.databinding.FragmentAddSaveBinding
import com.seymen.ezberarkadasim.model.WordsModel
import com.seymen.ezberarkadasim.viewmodel.AddSaveViewModel
import kotlinx.android.synthetic.main.fragment_add_save.*
import java.util.*

class AddSaveFragment : Fragment(){
    //Variables defined
    private lateinit var sqliteHelper : SQLiteHelper
    private var adapter : RecyclerViewAdapter? = null
    private lateinit var id : ArrayList<Int>
    private lateinit var sharedPreferencesID : SharedPreferences
    private lateinit var viewModel: AddSaveViewModel
    private lateinit var binding : FragmentAddSaveBinding
    private lateinit var inflaterD : LayoutInflater
    private var whchLang : Int? = null
    private lateinit var mAdView : AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apply {
            whchLang = arguments?.getInt("LANGKEY")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        inflaterD = inflater
        binding = FragmentAddSaveBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Defined variables are initialized
        viewModel = ViewModelProvider(this)[AddSaveViewModel::class.java]
        activity?.let { viewModel.checkNetAndClose(requireContext(), it) }

        sqliteHelper = SQLiteHelper(requireContext())

        sharedPreferencesID = requireContext().getSharedPreferences("com.seymen.ezberarkadasim",MODE_PRIVATE)

        init()

    }

    private fun init() {

        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        id = sqliteHelper.getOnlyID()

        btnSave.setOnClickListener {
            addWord()
        }

        btnClear.setOnClickListener {
            deleteAllWords()
        }

        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        btnWriteWSpeech.setOnClickListener {
            //check permission
            if(ContextCompat. checkSelfPermission (requireContext(), Manifest.permission. RECORD_AUDIO ) != PackageManager. PERMISSION_GRANTED){
                viewModel.checkPermission(requireActivity(),requireContext())
            } else{
                getSpeechInput()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecyclerViewAdapter()
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        getWords()

        adapter?.setOnClickDeleteItem {
            deleteSelectedWord(it.idModel)
        }    }

    //method of show data from database in recyclerview
    private fun getWords() {
        val klmList = sqliteHelper.getAllWord()
        adapter?.addItems(klmList)
    }
    //delete selected word method
    private fun deleteSelectedWord(id : Int){

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.alrt_warn)
        builder.setIcon(R.drawable.warning)
        builder.setMessage(R.string.alrt_sureDeleteOne)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.alrt_yes){ dialog, _->
            sqliteHelper.deleteWordByID(id)
            getWords()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.alrt_no){ dialog, _->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }
    // method of listening to words
    private fun deleteAllWords(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.alrt_sureDeleteAll)
        builder.setCancelable(true)
        builder.setTitle(R.string.alrt_warn)
        builder.setIcon(R.drawable.warning)
        builder.setPositiveButton(R.string.alrt_yes){ dialog, _->
            id = sqliteHelper.getOnlyID()
            for (pos1 in id) {
                sqliteHelper.deleteWordByID(pos1)
            }
            sharedPreferencesID.edit().putInt("idKontrol",0).apply()
            getWords()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.alrt_no){ dialog, _->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }
    //Add word database
    private fun addWord(){
        var word = binding.kaydedilecekText.text.toString().lowercase().trim().replace("\\s+".toRegex(), " ")
        val re = Regex("[^a-zA-Z0-9ığüşöçİĞÜŞÖÇ \\p{InArabic}]")
        word = re.replace(word, "")
        if(word.isEmpty()){
            Toast.makeText(requireContext(),R.string.tst_plsAddWord, Toast.LENGTH_LONG).show()
        } else  {
            var alinanID = sharedPreferencesID.getInt("idKontrol",0)

            if(alinanID == 0){
                alinanID += 1
                sharedPreferencesID.edit().putInt("idKontrol",alinanID).apply()
            }else{
                alinanID += 1
                sharedPreferencesID.edit().putInt("idKontrol",alinanID).apply()
            }
            val klm = WordsModel(idModel = alinanID,wordModel = word)
            val status = sqliteHelper.addWord(klm)
           //save check
            if(status > -1){
                binding.kaydedilecekText.setText("")
                binding.kaydedilecekText.requestFocus()
                getWords()
            } else{
                Toast.makeText(requireContext(),R.string.tst_saveFailed,Toast.LENGTH_LONG).show()
            }
        }
    }

    // Speech to Text
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            20 -> if (resultCode == RESULT_OK && resultCode == RESULT_OK)
            {
                val result =data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                kaydedilecekText.setText(result!![0])
            }
        }
    }
        // Speech to Text
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
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.promt_Add))
            startActivityForResult(intent,20)}
    }
}


