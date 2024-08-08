package com.ocean.pdfcreateviewshareapp

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.ocean.pdfcreateviewshareapp.itextpdf.AddDataItemAction
import com.ocean.pdfcreateviewshareapp.itextpdf.AddDataItemActionAlternateRowColor
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

//        bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
//        scaleBmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)

        if (checkPermission()){
            Toast.makeText(this, "Permission Granted...", Toast.LENGTH_SHORT).show()
        }else{
            requestPermission()
        }

        binding.idBtnGeneratePdf.setOnClickListener {
//            generatePdf()
            PdfUtil.createAndDisplayPdf(this, "pdf", this)
        }
        binding.idBtnSharePdf.setOnClickListener {
//            generatePdf()
            PdfUtil.createAndDisplayPdf(this, "share", this)
        }
    }

    private fun generatePdf() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val title = Paint()
        val myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val myPage : PdfDocument.Page = pdfDocument.startPage(myPageInfo)
        val canvas: Canvas = myPage.canvas

        canvas.drawBitmap(scaleBmp, 56F, 40F, paint)
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        title.textSize = 15F
        title.setColor(ContextCompat.getColor(this, R.color.purple))

        canvas.drawText("A portal for IT professionals.", 209F, 100F, title)
        canvas.drawText("Geeks for Geeks", 209F, 80F, title)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        title.setColor(ContextCompat.getColor(this, R.color.purple))
        title.textSize = 15F

        title.textAlign = Paint.Align.CENTER
        canvas.drawText("This is a sample document which we have created.", 396F, 560F, title)
        pdfDocument.finishPage(myPage)

        val file: File = File(Environment.getExternalStorageDirectory(), "GFG.pdf")

        try {

        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext, "Fail to generate PDF file...", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()

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
            AddTittleTextActionBgColor("Aadhaar Enabled Payment System", 25.0f, headerFont),
            AddParagraphAction(),
//            AddTittleTextActionBgColor("Transaction Details", 30.0f, headerFont, Font.BOLD, mColorAccent),
            AddParagraphAction(),
            AddLineSeparatorAction(lineSeparator),
            AddDataItemAction("Status", "Success", mOrderIdFont),
            AddDataItemAction("Transaction Amount", "Rs. 2500.00", mOrderIdFont),
            AddDataItemAction("Bank Name", "SBI", mOrderIdFont),
            AddDataItemActionAlternateRowColor("Status", "Success", mOrderIdFont,0),
            AddDataItemActionAlternateRowColor("Transaction Amount", "Rs. 2500.00", mOrderIdFont,1),
            AddDataItemActionAlternateRowColor("Bank Name", "SBI", mOrderIdFont,2),
            AddParagraphAction(),
            AddTittleTextAction(
                "Thank You",
                22.0f,
                headerFont,
                Font.NORMAL,
                mColorAccent,
                Element.ALIGN_RIGHT
            ),
            AddTittleTextAction(
                "SOME-COMPANY",
                24.0f,
                headerFont,
                Font.NORMAL,
                BaseColor.BLACK,
                Element.ALIGN_RIGHT
            )
        )
    }
}