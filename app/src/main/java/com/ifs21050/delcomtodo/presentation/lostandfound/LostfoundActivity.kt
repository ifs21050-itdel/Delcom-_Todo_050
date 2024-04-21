package com.ifs21050.delcomtodo.presentation.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21050.delcomtodo.R
import com.ifs21050.delcomtodo.ViewModelFactory
import com.ifs21050.delcomtodo.adapter.LosfoundAdapter
import com.ifs21050.delcomtodo.data.remote.MyResult
import com.ifs21050.delcomtodo.data.remote.response.DelcomLostFoundsResponse
import com.ifs21050.delcomtodo.data.remote.response.LostFoundsItemResponse
import com.ifs21050.delcomtodo.databinding.ActivityLostfoundBinding
import com.ifs21050.delcomtodo.helper.Utils.Companion.observeOnce
import com.ifs21050.delcomtodo.presentation.todo.TodoDetailActivity
import com.ifs21050.delcomtodo.presentation.todo.TodoManageActivity

class LostfoundActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostfoundBinding
    private val viewModel by viewModels<LostfoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == TodoManageActivity.RESULT_CODE) {
            recreate()
        }
        if (result.resultCode == TodoDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    TodoDetailActivity.KEY_IS_CHANGED,
                    false
                )
                if (isChanged) {
                    recreate()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostfoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }
    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetTodos(null, null, "lost")
    }
    private fun setupAction() {
        binding.fabMainAddLostfound.setOnClickListener {
            openAddTodoActivity()
        }
    }
    private fun observeGetTodos(
        isCompleted: Int?,
        userId: Int?,
        status: String
    ) {
        // Pastikan `status` bisa "lost" atau "found".
        if (status != "lost" && status != "found") {
            throw IllegalArgumentException("Status harus 'lost' atau 'found'.")
        }

        // Lanjutkan dengan mendapatkan data.
        viewModel.getLostfounds(isCompleted, userId, status)
            .observe(this) { result ->
                when (result) {
                    is MyResult.Loading -> showLoading(true)
                    is MyResult.Success -> {
                        showLoading(false)
                        loadTodosToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
    }

    private fun loadTodosToLayout(response: DelcomLostFoundsResponse) {
        val lostfounds = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostfounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostfounds.addItemDecoration(itemDecoration)
        if (lostfounds.isEmpty()) {
            showEmptyError(true)
            binding.rvMainLostfounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = LosfoundAdapter()
            adapter.submitOriginalList(lostfounds)
            binding.rvMainLostfounds.adapter = adapter
            adapter.setOnItemClickCallback(object : LosfoundAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostfounds: LostFoundsItemResponse,
                    isChecked: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())
                    viewModel.putLostfound(
                        lostfounds.id,
                        lostfounds.title,
                        lostfounds.description,
                        lostfounds.status,
                        isChecked
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostfoundActivity,
                                        "Gagal menyelesaikan todo: " + lostfounds.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostfoundActivity,
                                        "Gagal batal menyelesaikan todo: " + lostfounds.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            is MyResult.Success -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostfoundActivity,
                                        "Berhasil menyelesaikan todo: " + lostfounds.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostfoundActivity,
                                        "Berhasil batal menyelesaikan todo: " + lostfounds.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else -> {}
                        }
                    }
                }
                override fun onClickDetailListener(lostFoundId: Int) {
                    val intent = Intent(
                        this@LostfoundActivity,
                        LostfoundDetailActivity::class.java
                    )
                    intent.putExtra(LostfoundDetailActivity.KEY_LOSTFOUND_ID, lostFoundId)
                    launcher.launch(intent)
                }
            })
            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainLostfounds.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.pbMainLF.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvMainLostfounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }
    private fun showEmptyError(isError: Boolean) {
        binding.tvMainLFEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    // Setelah menambahkan entri baru, panggil metode untuk memperbarui data
    private fun openAddTodoActivity() {
        val intent = Intent(this@LostfoundActivity, LostfoundManageActivity::class.java)
        // Memberikan informasi bahwa aktivitas adalah untuk membuat entri baru.
        intent.putExtra(LostfoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }

    // Memperbarui data setelah menambahkan entri baru
    private fun observePostLostfound(title: String, description: String, status: String) {
        viewModel.postLostfound(title, description, status).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    // Memuat ulang data setelah menambahkan entri baru
                    observeGetTodos(null, null, "lost")
                }
                is MyResult.Error -> {
                    showLoading(false)
                    showEmptyError(true)
                    Toast.makeText(this@LostfoundActivity, "Gagal menambahkan entri baru: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}