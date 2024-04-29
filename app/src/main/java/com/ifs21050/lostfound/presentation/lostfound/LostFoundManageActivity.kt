package com.ifs21050.lostfound.presentation.lostfound

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifs21050.lostfound.R
import com.ifs21050.lostfound.data.model.DelcomLostFound
import com.ifs21050.lostfound.data.remote.MyResult
import com.ifs21050.lostfound.databinding.ActivityLostFoundManageBinding
import com.ifs21050.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21050.lostfound.helper.getImageUri
import com.ifs21050.lostfound.helper.reduceFileImage
import com.ifs21050.lostfound.helper.uriToFile
import com.ifs21050.lostfound.presentation.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class LostFoundManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundManageBinding
    private var currentImageUri: Uri? = null

    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostFoundManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAtion()
    }

    private fun setupView() {
        showLoading(false)
    }

    private fun setupAtion() {
        val isAddLostFound = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddLostFound) {
            manageAddLostFound()
        } else {

            val delcomTodo = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_TODO, DelcomLostFound::class.java)
                }

                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomLostFound>(KEY_TODO)
                }
            }
            if (delcomTodo == null) {
                finishAfterTransition()
                return
            }
            manageEditLostFound(delcomTodo)
        }

        binding.appbarLostFoundManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddLostFound() {
        binding.apply {
            appbarLostFoundManage.title = "Tambah Lost Found"
            btnLostFoundManageSave.setOnClickListener {
                val title = etLostFoundManageTitle.text.toString()
                val description = etLostFoundManageDesc.text.toString()
                val status = etLostFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }
                observePostLostFound(title, description, status)
            }
            btnTodoManageCamera.setOnClickListener {
                startCamera()
            }
            btnTodoManageGallery.setOnClickListener {
                startGallery()
            }
        }
    }

    private fun observePostLostFound(title: String, description: String, status : String,) {
        viewModel.postLostFound(title, description,status,).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
//                is MyResult.Success -> {
////                    showLoading(false)
////                    val resultIntent = Intent()
////                    setResult(RESULT_CODE, resultIntent)
////                    finishAfterTransition()
////                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverLostFound(result.data.lostFoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()           }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun manageEditLostFound(lostFound: DelcomLostFound) {
        binding.apply {
            appbarLostFoundManage.title = "Ubah Barang"

            etLostFoundManageTitle.setText(lostFound.title)
            etLostFoundManageDesc.setText(lostFound.description)

            val statusArray = resources.getStringArray(R.array.status)
            val statusIndex = statusArray.indexOf(lostFound.status)
            etLostFoundManageStatus.setSelection(statusIndex)

            if (lostFound.cover != null) {
                Glide.with(this@LostFoundManageActivity)
                    .load(lostFound.cover)
                    .placeholder(R.drawable.ic_image_24)
                    .into(ivTodoManageCover)
            }

            btnLostFoundManageSave.setOnClickListener {
                val title = etLostFoundManageTitle.text.toString()
                val description = etLostFoundManageDesc.text.toString()
                val status = etLostFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                observePutLostFound(lostFound.id, title, description, status, lostFound.isCompleted)

            }
            btnTodoManageCamera.setOnClickListener {
                startCamera()
            }
            btnTodoManageGallery.setOnClickListener {
                startGallery()
            }
        }
    }
    private fun startGallery() {
        launcherGallery.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(
                applicationContext,
                "Tidak ada media yang dipilih!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivTodoManageCover.setImageURI(it)
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun observePutLostFound(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ) {
        viewModel.putLostFound(
            lostfoundId,
            title,
            description,
            status,
            isCompleted,
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
//                is MyResult.Success -> {
//                    showLoading(false)
//                    val resultIntent = Intent()
//                    setResult(RESULT_CODE, resultIntent)
//                    finishAfterTransition()
//                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverLostFound(lostfoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()
                    }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun observeAddCoverLostFound(
        lostfoundId: Int
    ) {
        val imageFile =
            uriToFile(currentImageUri!!, this).reduceFileImage()
        val requestImageFile =
            imageFile.asRequestBody("image/jpeg".toMediaType())
        val reqPhoto =
            MultipartBody.Part.createFormData(
                "cover",
                imageFile.name,
                requestImageFile
            )
        viewModel.addCoverLostFound(
            lostfoundId,
            reqPhoto
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> { showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    showLoading(false)
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ ->
                            val resultIntent = Intent()
                            setResult(RESULT_CODE, resultIntent)
                            finishAfterTransition()
                        }
                        setCancelable(false)
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundManage.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        binding.btnLostFoundManageSave.isActivated = !isLoading
        binding.btnLostFoundManageSave.text =
            if (isLoading) "" else "Simpan"
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_TODO = "todo"
        const val RESULT_CODE = 1002
    }
}
