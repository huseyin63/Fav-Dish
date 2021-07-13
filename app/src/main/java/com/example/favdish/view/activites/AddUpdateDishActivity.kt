package com.example.favdish.view.activites

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.App
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapter.CustomListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class AddUpdateDishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUpdateDishBinding

    private lateinit var cameraPhoto: TextView
    private lateinit var galleryPhoto: TextView
    private lateinit var dialog: Dialog
    private lateinit var customListDialog: Dialog
    private lateinit var view: View
    private val requestCamera = 1
    private val requestMedia = 200
    private lateinit var photoFile: File
    private val imageDirectory = "FavDishImages"
    private lateinit var imagePath: String


    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as App).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        binding.btnAddDish.setOnClickListener {
            closeKeyBoard(it)
            trimView()
        }

        binding.ivAddDishImage.setOnClickListener {
            imageSelectedDialog()
        }
        binding.etType.setOnClickListener {
            customItemsListDialog(
                resources.getString(R.string.title_select_dish_type),
                Constants.dishTypes(),
                Constants.DISH_TYPE
            )
        }
        binding.etCategory.setOnClickListener {
            customItemsListDialog(
                resources.getString(R.string.title_select_dish_category),
                Constants.dishCategory(),
                Constants.DISH_CATEGORY
            )
        }
        binding.etCookingTime.setOnClickListener {
            customItemsListDialog(
                resources.getString(R.string.title_select_dish_cooking_time),
                Constants.dishCookingTime(),
                Constants.DISH_COOKING_TIME
            )
        }

    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        customListDialog = Dialog(this@AddUpdateDishActivity)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        customListDialog.setContentView(binding.root)

        binding.tvTitle.text = title
        val adapter = CustomListItemAdapter(this, itemsList, selection)
        binding.recyclerview.adapter = adapter
        customListDialog.show()
    }

    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                customListDialog.dismiss()
                binding.etType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                customListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME -> {
                customListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(imageDirectory, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == requestCamera) {
            val image = BitmapFactory.decodeFile(photoFile.absolutePath)
            val imageNew = bitmapDownSizing(image, 600)
            imagePath = saveImageToInternalStorage(imageNew)
            binding.ivDishImage.setImageBitmap(imageNew)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == requestMedia) {
            data?.let {
                val bitmap = BitmapFactory.decodeFileDescriptor(
                    contentResolver.openFileDescriptor(
                        it.data ?: Uri.EMPTY, "r"
                    )?.fileDescriptor
                )
                val reducedImage = bitmapDownSizing(bitmap, 600)
                println(reducedImage.byteCount)
                Glide.with(this).load(reducedImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("TAG", "Error")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val bitmapImage = resource?.toBitmap()
                            imagePath = saveImageToInternalStorage(bitmapImage!!)
                            return false
                        }
                    })
                    .into(binding.ivDishImage)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        openCamera()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(this@AddUpdateDishActivity, "Error occurred!", Toast.LENGTH_SHORT)
                    .show()
            }.onSameThread()
            .check()
        dialog.dismiss()
    }

    private fun dispatchTakePictureGalleryIntent() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    openGallery()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@AddUpdateDishActivity,
                        "You have denied the storage permission to select image.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(this@AddUpdateDishActivity, "Error occurred!", Toast.LENGTH_SHORT)
                    .show()
            }.onSameThread()
            .check()
        dialog.dismiss()
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile("IMAGE_")
        val fileProvider =
            FileProvider.getUriForFile(this, "com.example.favdish.view.fileprovider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        startActivityForResult(intent, requestCamera)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestMedia)
    }

    private fun imageSelectedDialog() {
        dialog = Dialog(this)
        view = layoutInflater.inflate(R.layout.dialog_custom, null)
        dialog.setContentView(view)
        dialogSetView()
        dialog.show()
        dialogSetListener()
    }

    private fun dialogSetView() {
        cameraPhoto = view.findViewById(R.id.tv_camera)
        galleryPhoto = view.findViewById(R.id.tv_gallery)
    }

    private fun dialogSetListener() {
        cameraPhoto.setOnClickListener {
            dispatchTakePictureIntent()
            dialog.dismiss()
        }

        galleryPhoto.setOnClickListener {
            dispatchTakePictureGalleryIntent()
            dialog.dismiss()
        }
    }

    private fun bitmapDownSizing(fileBitmap: Bitmap, maxSize: Int): Bitmap {
        var width = fileBitmap.width
        var height = fileBitmap.height

        val bitmapRate: Double = width.toDouble() / height.toDouble()
        if (bitmapRate > 1) {
            width = maxSize
            val reducedSize = width / bitmapRate
            height = reducedSize.toInt()
        } else {
            height = maxSize
            val shortedSize = height * bitmapRate
            width = shortedSize.toInt()
        }
        return Bitmap.createScaledBitmap(fileBitmap, width, height, true)
    }

    private fun trimView() {
        val title = binding.etTitle.text.toString().trim { it <= ' ' }
        val type = binding.etType.text.toString().trim { it <= ' ' }
        val category = binding.etCategory.text.toString().trim { it <= ' ' }
        val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
        val cookingTime = binding.etCookingTime.text.toString().trim { it <= ' ' }
        val directionCook = binding.etDirectionToCook.text.toString().trim { it <= ' ' }
        val dishImage = binding.ivDishImage.drawable

        if (dishImage == null || title.isEmpty() || type.isEmpty() || category.isEmpty() || ingredients.isEmpty() || cookingTime.isEmpty() || directionCook.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.error_message), Toast.LENGTH_SHORT)
                .show()
        } else {
            val favDishDetail = FavDish(
                imagePath,
                Constants.DISH_IMAGE_SOURCE_LOCAL,
                title,
                type,
                category,
                ingredients,
                cookingTime,
                directionCook,
                false
            )

            favDishViewModel.insert(favDishDetail)
            Toast.makeText(
                this,
                "You successfully added your favorite dish details",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }


    private fun closeKeyBoard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}


