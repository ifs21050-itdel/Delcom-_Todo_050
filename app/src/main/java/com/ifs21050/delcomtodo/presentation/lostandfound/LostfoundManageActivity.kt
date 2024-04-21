package com.ifs21050.delcomtodo.presentation.lostandfound

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ifs21050.delcomtodo.R
import com.ifs21050.delcomtodo.ViewModelFactory
import com.ifs21050.delcomtodo.data.model.DelcomLost
import com.ifs21050.delcomtodo.data.remote.MyResult
import com.ifs21050.delcomtodo.databinding.ActivityLostfoundManageBinding
import com.ifs21050.delcomtodo.helper.Utils.Companion.observeOnce

class LostfoundManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostfoundManageBinding
    private val viewModel by viewModels<LostfoundViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostfoundManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView() {
        showLoading(false)
    }

    private fun setupAction() {
        val isAddLostfound = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddLostfound) {
            manageAddLostfound()
        } else {
            val delcomLosts = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_LOST, DelcomLost::class.java)
                }
                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomLost>(KEY_LOST)
                }
            }
            if (delcomLosts == null) {
                finishAfterTransition()
                return
            }
            manageEditLostfound(delcomLosts)
        }
        binding.appbarLFManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddLostfound() {
        binding.apply {
            btnLFSave.setOnClickListener {
                val title = etLFTitle.text.toString().trim()
                val description = etLFDesc.text.toString().trim()
                val status = etStatus.text.toString().trim()

                // Validasi input
                if (title.isEmpty() || description.isEmpty() || !(status == "lost" || status == "found")) {
                    showAlertDialog("Input Error", "Judul, deskripsi, dan status tidak boleh kosong. Status harus 'lost' atau 'found'.")
                    return@setOnClickListener
                }

                observePostLostfound(title, description, status)
            }
        }
    }

    private fun observePostLostfound(title: String, description: String, status: String) {
        viewModel.postLostfound(title, description, status).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    setResult(RESULT_CODE)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    showAlertDialog("Error", "Terjadi kesalahan: ${result.error}")
                    showLoading(false)
                }
            }
        }
    }


    private fun manageEditLostfound(lostfound: DelcomLost) {
        binding.apply {
            appbarLFManage.title = "Ubah Lost n Found"
            etLFTitle.setText(lostfound.title)
            etLFDesc.setText(lostfound.description)
            etStatus.setText(lostfound.status)

            btnLFSave.setOnClickListener {
                val title = etLFTitle.text.toString().trim()
                val description = etLFDesc.text.toString().trim()
                val status = etStatus.text.toString().trim()

                // Validasi input
                if (title.isEmpty() || description.isEmpty() || !(status == "lost" || status == "found")) {
                    showAlertDialog("Input Error", "Judul, deskripsi, dan status tidak boleh kosong. Status harus 'lost' atau 'found'.")
                    return@setOnClickListener
                }

                // Ubah status kebalikannya
                val newStatus = if (status == "lost") "found" else "lost"
                observePutLostfound(lostfound.id, title, description, newStatus, lostfound.isComplete)
            }
        }
    }

    private fun observePutLostfound(lostFoundId: Int, title: String, description: String, status: String, isCompleted: Boolean) {
        val newStatus = if (status == "lost") "found" else "lost"
        viewModel.putLostfound(lostFoundId, title, description, newStatus, isCompleted).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    setResult(RESULT_CODE)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    showAlertDialog("Error", getString(R.string.terjadi_kesalahan, result.error))
                    showLoading(false)
                }
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.pbLF.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLFSave.isEnabled = !isLoading
        binding.btnLFSave.text = if (isLoading) "Menyimpan..." else "Simpan"
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()
            .show()
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_LOST = "lost"
        const val KEY_FOUND = "found"
        const val RESULT_CODE = 1002
    }
}
