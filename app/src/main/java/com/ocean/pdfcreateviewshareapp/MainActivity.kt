package com.ocean.pdfcreateviewshareapp

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.draw.LineSeparator
import com.ocean.pdfcreateviewshareapp.databinding.ActivityMainBinding
import com.ocean.pdfcreateviewshareapp.itextpdf.AddDataItemActionAlternateRowColor
import com.ocean.pdfcreateviewshareapp.itextpdf.AddImageAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddImagesInRowAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddLineSeparatorAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddParagraphAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddTittleTextAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddTittleTextActionBgColor
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfAction
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfContentProvider
import com.ocean.pdfcreateviewshareapp.itextpdf.PdfUtil
import java.io.File

class MainActivity : AppCompatActivity(), PdfContentProvider {

    private lateinit var binding : ActivityMainBinding
    private var pageHeight = 1120
    private var pageWidth = 792

    private lateinit var bmp: Bitmap
    private lateinit var scaleBmp: Bitmap

    private var PERMISSION_CODE = 101

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (checkPermission()){
            Toast.makeText(this, "Permission Granted...", Toast.LENGTH_SHORT).show()
        }else{
            requestPermission()
        }

        binding.idBtnGeneratePdf.setOnClickListener {
            PdfUtil.createAndDisplayPdf(this, "pdf", this)
        }
        binding.idBtnSharePdf.setOnClickListener {
            PdfUtil.createAndDisplayPdf(this, "share", this)
        }
    }
    private fun checkPermission(): Boolean {

        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

    }

    override fun providePdfActions(): List<PdfAction?> {
        val mColorAccent = BaseColor(0, 153, 204, 255)
        val headerFont =
            BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED)
        val fontBody = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED)
        val lineSeparator = LineSeparator().apply { lineColor = BaseColor(0, 0, 0, 68) }
        val mOrderIdFont = Font(fontBody, 18.0f, Font.NORMAL, BaseColor.BLACK)

        return listOf(
            AddImagesInRowAction(this, R.drawable.ic_android_black_24dp, R.drawable.android_whole_icon, 100, 75),
            AddImageAction(this, R.drawable.baseline_bakery_dining_24, 100, 75),
            AddTittleTextActionBgColor(this,"Something-- Receipt", 25.0f, headerFont),
            AddLineSeparatorAction(lineSeparator),
            AddDataItemActionAlternateRowColor(this,"Status", "Success", mOrderIdFont,3),
            AddDataItemActionAlternateRowColor(this, "Amount", "Rs. 2500.00", mOrderIdFont,4),
            AddDataItemActionAlternateRowColor(this,"Bank Name", "SBI", mOrderIdFont,5),
            AddDataItemActionAlternateRowColor(this,"Status", "Success", mOrderIdFont,0),
            AddDataItemActionAlternateRowColor(this,"Amount", "Rs. 2500.00", mOrderIdFont,1),
            AddDataItemActionAlternateRowColor(this,"Bank Name", "SBI", mOrderIdFont,2),
            AddParagraphAction()
        )
    }
}